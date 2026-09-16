package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.domain.Materiel;
import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Verification de l'annulation (FR-010 a FR-014, FR-020, FR-026, FR-027).
 *
 * <p>Couvre les regles RG-05, RG-06, RG-07 et RG-09, ainsi que les scenarios T06, T07
 * et T11.</p>
 *
 * <p>Le scenario T08 (annulation par un non-proprietaire) est verifie a deux niveaux :
 * ici sur le service, et dans {@code AnnulationNonProprietaireTest} par une requete HTTP
 * directe, conformement a l'exigence de l'enonce.</p>
 */
@DisplayName("Annulation d'une réservation")
class ReservationServiceAnnulationTest extends TestIntegrationSupport {

    @Test
    @DisplayName("T06 (FR-011, FR-013, RG-06, RG-07) : Alice annule, l'historique est conservé")
    void t06AnnulationAutorisee() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);

        reservationService.annuler(reservation.getId(), alice);

        Reservation rechargee = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(rechargee.getStatut()).isEqualTo(StatutReservation.ANNULEE_PAR_ETUDIANT);
        assertThat(rechargee.estActive()).isFalse();
        assertThat(rechargee.getCleActive())
                .as("la cle technique doit etre liberee pour permettre une nouvelle reservation")
                .isNull();
        assertThat(reservationRepository.count())
                .as("l'enregistrement annule doit etre conserve, pas supprime")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("FR-027 (CA-02) : l'application n'attribue jamais ANNULEE_ADMINISTRATIVE")
    void jamaisAnnuleeAdministrative() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);

        reservationService.annuler(reservation.getId(), alice);

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatut())
                .isNotEqualTo(StatutReservation.ANNULEE_ADMINISTRATIVE)
                .isEqualTo(StatutReservation.ANNULEE_PAR_ETUDIANT);
    }

    @Test
    @DisplayName("T06 (RG-06) : l'annulation est possible le jour réservé inclus")
    void annulationLeJourReserveInclus() {
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_RECETTE, alice);

        reservationService.annuler(reservation.getId(), alice);

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .as("le jour reserve inclus, l'annulation est encore possible")
                .isFalse();
    }

    @Test
    @DisplayName("T07 (FR-014, RG-07) : après annulation, un autre étudiant peut réserver")
    void t07ReservationPossibleApresAnnulation() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.annuler(reservation.getId(), alice);
        Reservation nouvelle = reservationService.reserver(idMateriel("MAT-001"), date, bilal);

        assertThat(nouvelle.estActive()).isTrue();
        assertThat(nouvelle.getId()).isNotEqualTo(reservation.getId());
        assertThat(nombreDeReservationsActives("MAT-001", date)).isEqualTo(1);
    }

    @Test
    @DisplayName("T08 (FR-010, RG-05) : un autre étudiant ne peut pas annuler, la réservation est inchangée")
    void t08AnnulationParUnAutreRefusee() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(reservation.getId(), idEtudiant("Bilal Dupont")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.PAS_PROPRIETAIRE);
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .as("la reservation d'Alice doit rester inchangee")
                .isTrue();
        assertThat(nombreDeReservationsActives("MAT-001", date)).isEqualTo(1);
    }

    @Test
    @DisplayName("T11 (CL-01, FR-013) : annuler une réservation déjà annulée ne change rien")
    void t11DoubleAnnulationSansEffet() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.annuler(reservation.getId(), alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(reservation.getId(), alice));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.DEJA_ANNULEE);
        Reservation rechargee = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(rechargee.getStatut()).isEqualTo(StatutReservation.ANNULEE_PAR_ETUDIANT);
        assertThat(rechargee.getCleActive()).isNull();
    }

    @Test
    @DisplayName("FR-012 (RG-06) : annuler une réservation dont le jour est passé est refusé")
    void annulationJourPasseRefusee() {
        Long alice = idEtudiant("Alice Martin");
        Reservation passee = creerReservationPassee("MAT-001", alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(passee.getId(), alice));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.JOUR_RESERVE_PASSE);
        assertThat(reservationRepository.findById(passee.getId()).orElseThrow().estActive())
                .as("la reservation doit rester active : le refus ne modifie rien")
                .isTrue();
    }

    @Test
    @DisplayName("FR-020 : annuler une réservation inexistante est refusé")
    void reservationInexistanteRefusee() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(999_999L, idEtudiant("Alice Martin")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.RESERVATION_INTROUVABLE);
    }

    @Test
    @DisplayName("FR-017 : annuler sans étudiant courant est refusé")
    void annulationSansEtudiantCourantRefusee() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(reservation.getId(), null));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.ETUDIANT_NON_SELECTIONNE);
    }

    @Test
    @DisplayName("FR-010 : le contrôle du propriétaire précède celui de l'état")
    void proprietaireAvantEtat() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.annuler(reservation.getId(), alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.annuler(reservation.getId(), idEtudiant("Bilal Dupont")));

        assertThat(refus.getMotif())
                .as("un non-proprietaire ne doit rien apprendre sur l'etat de la reservation")
                .isEqualTo(MotifRefus.PAS_PROPRIETAIRE);
    }

    /**
     * Cree une reservation dont le jour est deja passe par rapport a l'horloge de test.
     *
     * <p>Le service refuserait cette creation (RG-03) : l'enregistrement est donc ecrit
     * directement, pour simuler une reservation valide qui est devenue passee. C'est le
     * cas vise par RG-06 et FR-012.</p>
     */
    private Reservation creerReservationPassee(String codeMateriel, Long etudiantId) {
        Materiel materiel = materielRepository.findByCode(codeMateriel).orElseThrow();
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElseThrow();
        LocalDate dateDepassee = ConfigurationHorlogeTest.DATE_RECETTE.minusDays(5);
        return reservationRepository.save(new Reservation(etudiant, materiel, dateDepassee));
    }
}
