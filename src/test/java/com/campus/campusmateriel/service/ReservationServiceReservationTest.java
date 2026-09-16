package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Verification de la creation d'une reservation (FR-005 a FR-008, FR-016 a FR-019,
 * FR-021, FR-025).
 *
 * <p>Couvre les regles RG-01 a RG-04 et RG-09, ainsi que les scenarios T01, T02, T03,
 * T04, T05, T10 et T12.</p>
 *
 * <p>Chaque cas de refus verifie <strong>deux</strong> choses : le motif de refus et
 * l'absence d'ecriture (RG-09).</p>
 */
@DisplayName("Création d'une réservation")
class ReservationServiceReservationTest extends TestIntegrationSupport {

    // ------------------------------------------------------------------
    // Cas autorises
    // ------------------------------------------------------------------

    @Test
    @DisplayName("T01 (FR-005, RG-01) : une réservation valide est créée et active")
    void t01ReservationValide() {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ACTIVE);
        assertThat(reservation.getDateReservation()).isEqualTo(ConfigurationHorlogeTest.DATE_FUTURE);
        assertThat(reservation.getCleActive())
                .as("une reservation active doit occuper le creneau")
                .isNotNull();
    }

    @Test
    @DisplayName("FR-007 (RG-03) : réserver le jour même est autorisé")
    void reserverLeJourMemeEstAutorise() {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_RECETTE, idEtudiant("Alice Martin"));

        assertThat(reservation.estActive()).isTrue();
    }

    @Test
    @DisplayName("T05 (FR-008, RG-04) : un étudiant peut réserver deux matériels le même jour")
    void t05DeuxMaterielsLeMemeJour() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.reserver(idMateriel("MAT-002"), date, alice);

        assertThat(reservationService.reservationsDe(alice)).hasSize(2);
    }

    @Test
    @DisplayName("CL-09 : le même matériel peut être réservé à deux dates différentes")
    void memeMaterielDeuxDatesDifferentes() {
        Long alice = idEtudiant("Alice Martin");

        reservationService.reserver(idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, alice);
        reservationService.reserver(idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE_AUTRE, alice);

        assertThat(reservationService.reservationsDe(alice)).hasSize(2);
    }

    // ------------------------------------------------------------------
    // Cas de refus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("T02 (FR-006, RG-02) : une double réservation est refusée sans écriture")
    void t02DoubleReservationRefusee() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        reservationService.reserver(idMateriel("MAT-001"), date, idEtudiant("Alice Martin"));

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-001"), date, idEtudiant("Bilal Dupont")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.MATERIEL_INDISPONIBLE);
        assertThat(nombreDeReservationsActives("MAT-001", date))
                .as("un refus ne doit jamais creer de ligne supplementaire")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("FR-025 (CA-01) : réserver son propre créneau donne un motif distinct")
    void dejaReserveParSoiMemeMotifDistinct() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        reservationService.reserver(idMateriel("MAT-001"), date, alice);

        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-001"), date, alice));

        assertThat(refus.getMotif())
                .as("le message doit distinguer « vous avez deja reserve » de « materiel indisponible »")
                .isEqualTo(MotifRefus.DEJA_RESERVE_PAR_VOUS)
                .isNotEqualTo(MotifRefus.MATERIEL_INDISPONIBLE);
        assertThat(nombreDeReservationsActives("MAT-001", date)).isEqualTo(1);
    }

    @Test
    @DisplayName("T03 : un autre étudiant peut réserver le même matériel un autre jour")
    void t03AutreDateAcceptee() {
        reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        Reservation bilal = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE_AUTRE, idEtudiant("Bilal Dupont"));

        assertThat(bilal.estActive()).isTrue();
    }

    @Test
    @DisplayName("T04 (FR-007, RG-03) : une date passée est refusée sans écriture")
    void t04DatePasseeRefusee() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(
                        idMateriel("MAT-002"), ConfigurationHorlogeTest.DATE_PASSEE,
                        idEtudiant("Alice Martin")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.DATE_PASSEE);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("T10 (FR-019) : une date absente est refusée sans écriture")
    void t10DateAbsenteRefusee() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(idMateriel("MAT-001"), null, idEtudiant("Alice Martin")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.DATE_INVALIDE);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-018 (CL-02) : un matériel inconnu est refusé sans écriture")
    void materielInconnuRefuse() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(
                        999_999L, ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin")));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.MATERIEL_INCONNU);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-017 (CL-06) : sans étudiant courant, la réservation est refusée")
    void sansEtudiantCourantRefuse() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(
                        idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, null));

        assertThat(refus.getMotif()).isEqualTo(MotifRefus.ETUDIANT_NON_SELECTIONNE);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-016 (RG-09) : l'ordre des contrôles est déterministe (date avant matériel)")
    void ordreDesControlesDeterministe() {
        RegleMetierException refus = catchThrowableOfType(
                RegleMetierException.class,
                () -> reservationService.reserver(
                        999_999L, ConfigurationHorlogeTest.DATE_PASSEE, idEtudiant("Alice Martin")));

        assertThat(refus.getMotif())
                .as("la date est controlee avant le materiel : le message doit rester stable")
                .isEqualTo(MotifRefus.DATE_PASSEE);
    }

    @Test
    @DisplayName("T12 (FR-021) : deux demandes successives ne laissent qu'une réservation active")
    void t12DeuxDemandesSuccessives() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;

        reservationService.reserver(idMateriel("MAT-003"), date, idEtudiant("Alice Martin"));
        assertThatThrownBy(() -> reservationService.reserver(
                idMateriel("MAT-003"), date, idEtudiant("Bilal Dupont")))
                .isInstanceOf(RegleMetierException.class);

        assertThat(nombreDeReservationsActives("MAT-003", date))
                .as("au plus une reservation active par couple (materiel, date)")
                .isEqualTo(1);
    }
}
