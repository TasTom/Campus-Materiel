package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Verification de l'evolution de RG-04 : un etudiant ne peut pas detenir plus de deux
 * reservations actives pour une meme journee (FR-008, FR-028).
 *
 * <p>Couvre les scenarios T13, T14 et T15 exiges par l'enonce, ainsi que la protection
 * contre deux demandes concurrentes du meme etudiant.</p>
 *
 * <p><strong>La limite porte sur les reservations actives uniquement.</strong> Une annulation
 * rend immediatement une possibilite de reservation : sans cela, RG-07 et SC-007 seraient
 * contredits.</p>
 */
@DisplayName("Limite de deux réservations actives par jour")
class LimiteReservationsJourTest extends TestIntegrationSupport {

    @Test
    @DisplayName("La limite du service vaut deux (FR-008)")
    void laLimiteVautDeux() {
        assertThat(ReservationService.LIMITE_RESERVATIONS_ACTIVES_PAR_JOUR).isEqualTo(2L);
    }

    @Test
    @DisplayName("T13 (FR-028) : une troisième réservation le même jour est refusée, sans écriture")
    void t13TroisiemeReservationRefusee() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        // Deux reservations actives le meme jour : autorise.
        reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);
        assertThat(reservationService.reservationsDe(alice)).hasSize(2);

        // Troisieme materiel, pourtant disponible : refuse.
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-003"), date, alice));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.LIMITE_RESERVATIONS_JOUR);
        assertThat(reservationRepository.countByEtudiantIdAndDateReservationAndStatut(
                alice, date, StatutReservation.ACTIVE))
                .as("le refus ne doit rien ecrire : le compte reste a deux")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("T14 (FR-028, RG-07) : après une annulation, une nouvelle réservation devient possible")
    void t14ApresAnnulationReservationPossible() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        Reservation premiere = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);

        reservationService.annuler(premiere.getId(), alice);

        Reservation nouvelle = reservationService.reserver(idMateriel("MAT-003"), date, alice);

        assertThat(nouvelle.estActive()).isTrue();
        assertThat(reservationRepository.countByEtudiantIdAndDateReservationAndStatut(
                alice, date, StatutReservation.ACTIVE))
                .as("une reservation annulee ne consomme pas le quota")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("T15 (FR-008) : deux réservations un jour n'empêchent pas une réservation un autre jour")
    void t15AutreJourToujoursPossible() {
        Long alice = idEtudiant("Alice Martin");

        reservationService.reserver(idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, alice);
        reservationService.reserver(idMateriel("MAT-002"), ConfigurationHorlogeTest.DATE_FUTURE, alice);

        Reservation autreJour = reservationService.reserver(
                idMateriel("MAT-003"), ConfigurationHorlogeTest.DATE_FUTURE_AUTRE, alice);

        assertThat(autreJour.estActive()).isTrue();
        assertThat(autreJour.getDateReservation())
                .as("la limite porte sur une journee, pas sur la duree de vie du compte")
                .isEqualTo(ConfigurationHorlogeTest.DATE_FUTURE_AUTRE);
    }

    @Test
    @DisplayName("FR-028 : la limite est propre à chaque étudiant")
    void limitePropreAChaqueEtudiant() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);

        // Bilal n'est pas concerne par la limite atteinte par Alice.
        Reservation bilal1 = reservationService.reserver(idMateriel("MAT-003"), date, bilal);
        Reservation bilal2 = reservationService.reserver(idMateriel("MAT-004"), date, bilal);

        assertThat(bilal1.estActive()).isTrue();
        assertThat(bilal2.estActive()).isTrue();
    }

    @Test
    @DisplayName("FR-028 : l'indisponibilité du matériel est annoncée avant la limite atteinte")
    void indisponibiliteAvantLimite() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        // Bilal occupe MAT-003 pour la journee.
        reservationService.reserver(idMateriel("MAT-003"), date, bilal);
        // Alice atteint sa limite avec deux autres materiels.
        reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);

        // Alice demande un materiel deja occupe : c'est l'indisponibilite qui doit etre annoncee,
        // sinon elle croirait qu'annuler une reservation debloquerait sa demande.
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-003"), date, alice));

        assertThat(refus.getMotif())
                .as("annoncer la limite ici inciterait l'etudiant a annuler pour rien")
                .isEqualTo(MotifRefus.MATERIEL_INDISPONIBLE)
                .isNotEqualTo(MotifRefus.LIMITE_RESERVATIONS_JOUR);
    }

    @Test
    @DisplayName("FR-028 : le conflit avec sa propre réservation est annoncé avant la limite")
    void conflitAvantLimite() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);

        // Alice a atteint sa limite ET redemande un materiel qu'elle a deja reserve.
        // Le motif annonce doit etre le conflit : c'est l'information utile, et elle reste
        // superposable au message « vous avez deja reserve » verifie pour FR-025.
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-001"), date, alice));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.DEJA_RESERVE_PAR_VOUS);
    }

    @Test
    @DisplayName("FR-028 : deux demandes concurrentes du même étudiant ne dépassent pas la limite")
    void t13DeuxDemandesConcurrentesNeDepassentPasLaLimite() throws Exception {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        // Alice detient deja une reservation : il ne reste qu'une place sur les deux.
        reservationService.reserver(idMateriel("MAT-001"), date, alice);

        AtomicInteger succes = new AtomicInteger();
        AtomicInteger refus = new AtomicInteger();

        // Les deux demandes visent des materiels DIFFERENTS : la contrainte d'unicite sur la cle
        // technique ne peut pas les departager. Seul le verrou d'ecriture pose sur l'etudiant
        // serialise les deux demandes et rend la limite fiable.
        try (ExecutorService executeur = Executors.newFixedThreadPool(2)) {
            Future<?> premier = executeur.submit(
                    () -> tenterReservation(idMateriel("MAT-002"), date, alice, succes, refus));
            Future<?> second = executeur.submit(
                    () -> tenterReservation(idMateriel("MAT-003"), date, alice, succes, refus));
            premier.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        }

        assertThat(succes.get())
                .as("une seule des deux demandes doit aboutir : il ne restait qu'une place")
                .isEqualTo(1);
        assertThat(refus.get()).isEqualTo(1);
        assertThat(reservationRepository.countByEtudiantIdAndDateReservationAndStatut(
                alice, date, StatutReservation.ACTIVE))
                .as("la limite de deux ne doit jamais etre depassee")
                .isEqualTo(2);
    }

    /** Tente une reservation et compte le resultat, sans laisser passer d'erreur technique. */
    private void tenterReservation(Long materielId, LocalDate date, Long etudiantId,
                                   AtomicInteger succes, AtomicInteger refus) {
        try {
            reservationService.reserver(materielId, date, etudiantId);
            succes.incrementAndGet();
        } catch (RegleMetierException refusMetier) {
            refus.incrementAndGet();
            assertThat(refusMetier.getMotif()).isEqualTo(MotifRefus.LIMITE_RESERVATIONS_JOUR);
        }
    }
}
