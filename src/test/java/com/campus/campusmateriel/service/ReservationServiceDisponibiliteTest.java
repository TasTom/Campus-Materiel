package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification de la disponibilite du materiel (FR-003, FR-004, FR-014).
 *
 * <p>Couvre les regles RG-02 (pas de double reservation) et RG-07 (une annulation
 * libere le materiel) et les scenarios T02, T03 et T07.</p>
 */
@DisplayName("Disponibilité du matériel")
class ReservationServiceDisponibiliteTest extends TestIntegrationSupport {

    @Test
    @DisplayName("FR-003 : sans réservation, les cinq matériels sont disponibles")
    void sansReservationToutEstDisponible() {
        List<ReservationService.MaterielDisponibilite> lignes =
                reservationService.disponibilites(ConfigurationHorlogeTest.DATE_FUTURE);

        assertThat(lignes).hasSize(5);
        assertThat(lignes).allMatch(ReservationService.MaterielDisponibilite::isDisponible);
    }

    @Test
    @DisplayName("FR-004 (RG-02) : un matériel réservé est indisponible, les autres restent disponibles")
    void materielReserveEstIndisponible() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        reservationService.reserver(idMateriel("MAT-001"), date, idEtudiant("Alice Martin"));

        Map<String, Boolean> disponibilites = disponibilitesParCode(date);

        assertThat(disponibilites.get("MAT-001")).isFalse();
        assertThat(disponibilites.get("MAT-002")).isTrue();
        assertThat(disponibilites.get("MAT-003")).isTrue();
        assertThat(disponibilites.get("MAT-004")).isTrue();
        assertThat(disponibilites.get("MAT-005")).isTrue();
    }

    @Test
    @DisplayName("T03 : une réservation n'affecte pas les autres dates")
    void reservationNAffectePasLesAutresDates() {
        reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        assertThat(disponibilitesParCode(ConfigurationHorlogeTest.DATE_FUTURE_AUTRE).get("MAT-001"))
                .as("le lendemain, le materiel doit etre de nouveau disponible")
                .isTrue();
    }

    @Test
    @DisplayName("FR-014 (RG-07, T07) : après annulation, le matériel est de nouveau disponible")
    void apresAnnulationLeMaterielEstDisponible() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(idMateriel("MAT-001"), date, alice);

        reservationService.annuler(reservation.getId(), alice);

        assertThat(disponibilitesParCode(date).get("MAT-001"))
                .as("une reservation annulee ne doit jamais rendre un materiel indisponible")
                .isTrue();
    }

    @Test
    @DisplayName("FR-014 : plusieurs annulations sur le même créneau ne le rebloquent pas")
    void plusieursAnnulationsNeRebloquentPasLeCreneau() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        Reservation premiere = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.annuler(premiere.getId(), alice);
        Reservation seconde = reservationService.reserver(idMateriel("MAT-001"), date, bilal);
        reservationService.annuler(seconde.getId(), bilal);

        assertThat(disponibilitesParCode(date).get("MAT-001")).isTrue();
        assertThat(reservationRepository.countByMaterielIdAndDateReservationAndStatut(
                idMateriel("MAT-001"), date, StatutReservation.ACTIVE))
                .isZero();
    }

    @Test
    @DisplayName("FR-024 : la date courante provient de l'horloge injectée")
    void dateCouranteVientDeLHorloge() {
        assertThat(reservationService.aujourdHui())
                .isEqualTo(ConfigurationHorlogeTest.DATE_RECETTE);
    }

    private Map<String, Boolean> disponibilitesParCode(LocalDate date) {
        Map<String, Boolean> resultat = new LinkedHashMap<>();
        for (ReservationService.MaterielDisponibilite ligne : reservationService.disponibilites(date)) {
            resultat.put(ligne.getMateriel().getCode(), ligne.isDisponible());
        }
        return resultat;
    }
}
