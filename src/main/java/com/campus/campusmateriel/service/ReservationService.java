package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.domain.Materiel;
import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.repository.EtudiantRepository;
import com.campus.campusmateriel.repository.MaterielRepository;
import com.campus.campusmateriel.repository.ReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regles metier de la reservation de materiel (RG-01 a RG-09).
 *
 * <p>Ce service est le <strong>seul endroit</strong> ou l'on decide si une operation est
 * acceptable. Les controleurs ne font que traduire des requetes HTTP en appels a ce
 * service, et les pages Thymeleaf ne decident rien.</p>
 *
 * <p>Le service ne connait ni la session ni la requete HTTP : l'identifiant de
 * l'etudiant courant lui est transmis en parametre explicite. C'est ce qui le rend
 * testable sans conteneur Web, et c'est aussi ce qui garantit qu'un formulaire ne peut
 * pas designer arbitrairement le proprietaire d'une annulation.</p>
 *
 * <p>La date courante provient de la {@link Clock} injectee, jamais d'un appel direct a
 * {@code LocalDate.now()} sans argument.</p>
 */
@Service
public class ReservationService {

    /**
     * Nombre maximal de reservations actives qu'un etudiant peut detenir pour une meme journee.
     *
     * <p>Cette limite resulte de l'evolution de RG-04 (FR-008, FR-028). Elle porte sur les
     * reservations <strong>actives</strong> uniquement : une annulation rend immediatement une
     * possibilite de reservation (RG-07, SC-007).</p>
     */
    public static final long LIMITE_RESERVATIONS_ACTIVES_PAR_JOUR = 2L;

    private final ReservationRepository reservationRepository;
    private final MaterielRepository materielRepository;
    private final EtudiantRepository etudiantRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              MaterielRepository materielRepository,
                              EtudiantRepository etudiantRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.materielRepository = materielRepository;
        this.etudiantRepository = etudiantRepository;
        this.clock = clock;
    }

    /** La date du jour, telle que vue par le serveur (hypothese H-02). */
    public LocalDate aujourdHui() {
        return LocalDate.now(clock);
    }

    // ------------------------------------------------------------------
    // Consultation des disponibilites (FR-003, FR-004)
    // ------------------------------------------------------------------

    /**
     * Indique si un materiel est disponible a une date donnee.
     *
     * <p>Un materiel est indisponible <strong>uniquement</strong> s'il existe une
     * reservation dont le statut est {@link StatutReservation#ACTIVE} pour ce couple
     * (materiel, date). Les reservations annulees ne bloquent jamais : c'est la
     * condition pour que le scenario T07 fonctionne (RG-02, RG-07).</p>
     *
     * @param materielId identifiant du materiel
     * @param date       date consultee
     * @return vrai si aucune reservation active n'existe pour ce couple
     */
    public boolean estDisponible(Long materielId, LocalDate date) {
        if (materielId == null || date == null) {
            return false;
        }
        return !reservationRepository.existsByMaterielIdAndDateReservationAndStatut(
                materielId, date, StatutReservation.ACTIVE);
    }

    /**
     * Construit la liste du materiel avec sa disponibilite a une date.
     *
     * @param date date consultee, ou {@code null} pour utiliser la date courante
     * @return la liste du materiel, ordonnee par code
     */
    public List<MaterielDisponibilite> disponibilites(LocalDate date) {
        LocalDate dateConsultee = (date == null) ? aujourdHui() : date;
        List<Materiel> materiels = materielRepository.findAllByOrderByCodeAsc();
        List<MaterielDisponibilite> resultat = new ArrayList<>(materiels.size());
        for (Materiel materiel : materiels) {
            resultat.add(new MaterielDisponibilite(
                    materiel, estDisponible(materiel.getId(), dateConsultee)));
        }
        return resultat;
    }

    // ------------------------------------------------------------------
    // Reservation (FR-005 a FR-008, FR-016 a FR-019, FR-021, FR-025)
    // ------------------------------------------------------------------

    /**
     * Cree une reservation active pour un etudiant.
     *
     * <p><strong>Ordre contractuel des controles</strong> (voir
     * {@code contracts/routes.md}) : la date, puis l'etudiant courant, puis l'existence
     * du materiel, puis le conflit, puis la limite du jour. Cet ordre est ce qui rend le
     * message affiche deterministe lorsqu'une requete cumule plusieurs anomalies.</p>
     *
     * <p>L'ordre place volontairement les controles de date en premier : ils ne
     * dependent d'aucune donnee en base et peuvent donc etre tranches avant toute
     * lecture. Le conflit est examine <strong>avant</strong> la limite : annoncer une limite
     * atteinte alors que le materiel demande est deja occupe inciterait l'etudiant a
     * annuler une reservation sans que cela debloque sa demande.</p>
     *
     * <p>En cas de refus, <strong>aucune ecriture</strong> n'a lieu (RG-09).</p>
     *
     * @param materielId identifiant du materiel a reserver
     * @param date       journee reservee
     * @param etudiantId identifiant de l'etudiant courant, ou {@code null}
     * @return la reservation creee
     * @throws RegleMetierException si l'operation est refusee
     */
    @Transactional
    public Reservation reserver(Long materielId, LocalDate date, Long etudiantId) {
        if (date == null) {
            throw new RegleMetierException(MotifRefus.DATE_INVALIDE);
        }
        if (date.isBefore(aujourdHui())) {
            throw new RegleMetierException(MotifRefus.DATE_PASSEE);
        }
        if (etudiantId == null) {
            throw new RegleMetierException(MotifRefus.ETUDIANT_NON_SELECTIONNE);
        }
        // L'etudiant est charge avec un verrou d'ecriture : deux demandes du meme etudiant
        // visant deux materiels differents le meme jour sont ainsi serialisees, ce qui rend
        // la limite du jour fiable. Sans ce verrou, les deux demandes pourraient compter
        // "une" reservation chacune et passer toutes les deux.
        Etudiant etudiant = etudiantRepository.findByIdForUpdate(etudiantId)
                .orElseThrow(() -> new RegleMetierException(MotifRefus.ETUDIANT_INCONNU));
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RegleMetierException(MotifRefus.MATERIEL_INCONNU));

        refuserSiDejaReserve(materiel, date, etudiantId);
        refuserSiLimiteDuJourAtteinte(date, etudiantId);

        Reservation reservation = new Reservation(etudiant, materiel, date);
        try {
            return reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException conflitConcurrent) {
            // Deux demandes simultanees ont passe le controle applicatif : la contrainte
            // d'unicite sur la cle technique a arbitre. On traduit en refus metier, sans
            // laisser remonter de detail technique a l'utilisateur (RG-09).
            throw new RegleMetierException(MotifRefus.MATERIEL_INDISPONIBLE);
        }
    }

    /**
     * Refuse la creation si l'etudiant detient deja le nombre maximal de reservations actives
     * pour cette journee (FR-008, FR-028).
     *
     * <p>Seules les reservations actives sont comptees : une annulation rend immediatement
     * une possibilite (RG-07).</p>
     */
    private void refuserSiLimiteDuJourAtteinte(LocalDate date, Long etudiantId) {
        long dejaActives = reservationRepository.countByEtudiantIdAndDateReservationAndStatut(
                etudiantId, date, StatutReservation.ACTIVE);
        if (dejaActives >= LIMITE_RESERVATIONS_ACTIVES_PAR_JOUR) {
            throw new RegleMetierException(MotifRefus.LIMITE_RESERVATIONS_JOUR);
        }
    }

    /**
     * Refuse la creation si une reservation active occupe deja le couple (materiel, date),
     * en distinguant le cas ou l'etudiant courant est lui-meme deja proprietaire
     * (FR-006 et FR-025 : deux motifs, donc deux messages).
     */
    private void refuserSiDejaReserve(Materiel materiel, LocalDate date, Long etudiantId) {
        boolean dejaReserve = reservationRepository.existsByMaterielIdAndDateReservationAndStatut(
                materiel.getId(), date, StatutReservation.ACTIVE);
        if (!dejaReserve) {
            return;
        }
        boolean parLuiMeme = reservationRepository
                .findByMaterielIdAndDateReservationAndStatutAndEtudiantId(
                        materiel.getId(), date, StatutReservation.ACTIVE, etudiantId)
                .isPresent();
        throw new RegleMetierException(
                parLuiMeme ? MotifRefus.DEJA_RESERVE_PAR_VOUS : MotifRefus.MATERIEL_INDISPONIBLE);
    }

    // ------------------------------------------------------------------
    // Consultation des reservations d'un etudiant (FR-009)
    // ------------------------------------------------------------------

    /**
     * Liste les reservations d'un etudiant, annulees comprises, de la plus recente a la
     * plus ancienne.
     *
     * <p>Les reservations annulees restent visibles : l'historique est conserve (RG-07).</p>
     *
     * @param etudiantId identifiant de l'etudiant
     * @return la liste de ses reservations
     */
    public List<Reservation> reservationsDe(Long etudiantId) {
        return reservationRepository.findByEtudiantIdOrderByDateReservationDesc(etudiantId);
    }

    // ------------------------------------------------------------------
    // Annulation (FR-010 a FR-014, FR-020, FR-026, FR-027)
    // ------------------------------------------------------------------

    /**
     * Annule une reservation appartenant a l'etudiant courant.
     *
     * <p><strong>Ordre contractuel des controles</strong> : existence, puis proprietaire,
     * puis etat deja annule, puis date. Le controle du proprietaire <strong>precede</strong>
     * celui de l'etat : un etudiant qui vise la reservation d'un autre reçoit un refus pour
     * non-propriete et n'apprend rien sur l'etat de cette reservation.</p>
     *
     * <p>L'annulation est autorisee jusqu'au jour reserve <strong>inclus</strong> (RG-06) et
     * refusee si ce jour est passe.</p>
     *
     * <p>En cas d'annulation reussie, l'enregistrement est conserve, son statut devient
     * {@link StatutReservation#ANNULEE_PAR_ETUDIANT} et la cle technique repasse a
     * {@code null}, ce qui libere le materiel (RG-07, FR-014). Le statut
     * {@code ANNULEE_ADMINISTRATIVE} n'est jamais attribue (FR-027).</p>
     *
     * @param reservationId identifiant de la reservation
     * @param etudiantId    identifiant de l'etudiant courant, ou {@code null}
     * @throws RegleMetierException si l'annulation est refusee
     */
    @Transactional
    public void annuler(Long reservationId, Long etudiantId) {
        if (etudiantId == null) {
            throw new RegleMetierException(MotifRefus.ETUDIANT_NON_SELECTIONNE);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RegleMetierException(MotifRefus.RESERVATION_INTROUVABLE));

        if (!reservation.getEtudiant().getId().equals(etudiantId)) {
            throw new RegleMetierException(MotifRefus.PAS_PROPRIETAIRE);
        }
        if (!reservation.estActive()) {
            throw new RegleMetierException(MotifRefus.DEJA_ANNULEE);
        }
        if (reservation.getDateReservation().isBefore(aujourdHui())) {
            throw new RegleMetierException(MotifRefus.JOUR_RESERVE_PASSE);
        }

        reservation.annulerParEtudiant();
        reservationRepository.save(reservation);
    }

    /** Vrai si l'annulation est possible pour cette reservation, du point de vue de la date. */
    public boolean annulationPossible(Reservation reservation) {
        return reservation != null
                && reservation.estActive()
                && !reservation.getDateReservation().isBefore(aujourdHui());
    }

    /**
     * Disponibilite d'un materiel a une date, telle qu'affichee a l'utilisateur.
     *
     * <p>Des accesseurs de style JavaBean sont declares explicitement en plus des
     * accesseurs du record : les gabarits Thymeleaf y accedent par leur nom de
     * propriete, et cette forme fonctionne quelle que soit la version du moteur
     * d'expressions.</p>
     *
     * @param materiel   le materiel
     * @param disponible vrai si aucune reservation active n'occupe ce couple
     */
    public record MaterielDisponibilite(Materiel materiel, boolean disponible) {

        public Materiel getMateriel() {
            return materiel;
        }

        public boolean isDisponible() {
            return disponible;
        }

        public boolean getDisponible() {
            return disponible;
        }

        /** Libelle affiche. Volontairement neutre : il ne nomme pas l'etudiant qui a reserve. */
        public String getLibelleDisponibilite() {
            return disponible ? "Disponible" : "Réservé";
        }
    }
}
