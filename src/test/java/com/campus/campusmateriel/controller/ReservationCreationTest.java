package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verification de la route de creation d'une reservation (HU3).
 *
 * <p>Couvre FR-005 a FR-008, FR-016, FR-017, FR-019, FR-021, FR-025 et les scenarios
 * T01, T02, T04, T10 et T12 au niveau HTTP.</p>
 *
 * <p>Chaque refus est verifie sur deux plans : la reponse renvoyee <strong>et</strong>
 * l'absence d'ecriture en base (RG-09).</p>
 */
@DisplayName("Route de création d'une réservation")
class ReservationCreationTest extends TestIntegrationSupport {

    @Test
    @DisplayName("T01 : une réservation valide redirige et crée une ligne active")
    void t01ReservationValideRedirige() throws Exception {
        MockHttpSession session = sessionAvecEtudiant("Alice Martin");

        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageSucces"));

        assertThat(nombreDeReservationsActives("MAT-001", ConfigurationHorlogeTest.DATE_FUTURE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("T02 : la double réservation est refusée, sans écriture supplémentaire")
    void t02DoubleReservationRefusee() throws Exception {
        String date = ConfigurationHorlogeTest.DATE_FUTURE.toString();
        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", date)
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", date)
                        .session(sessionAvecEtudiant("Bilal Dupont")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(nombreDeReservationsActives("MAT-001", ConfigurationHorlogeTest.DATE_FUTURE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("T04 : une date passée est refusée avec message et sans écriture")
    void t04DatePasseeRefusee() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-002")))
                        .param("dateReservation", ConfigurationHorlogeTest.DATE_PASSEE.toString())
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("T10 : une date illisible est refusée avec un message, sans écriture")
    void t10DateIllisibleRefusee() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", "pas-une-date")
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("T10 : une date absente est refusée avec un message, sans écriture")
    void t10DateAbsenteRefusee() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-018 : un matériel inconnu est refusé, sans écriture")
    void materielInconnuRefuse() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("materielId", "999999")
                        .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-017 : sans étudiant courant, la réservation est refusée")
    void sansEtudiantCourantRefuse() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                        .session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-010 : un identifiant d'étudiant injecté dans le formulaire est ignoré")
    void identifiantEtudiantInjecteEstIgnore() throws Exception {
        // Bilal est l'etudiant courant, mais le formulaire tente de designer Alice.
        // Le proprietaire doit rester Bilal : le formulaire ne decide pas de l'identite.
        MockHttpSession session = sessionAvecEtudiant("Bilal Dupont");

        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-001")))
                        .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                        .param("etudiantId", String.valueOf(idEtudiant("Alice Martin")))
                        .session(session))
                .andExpect(status().is3xxRedirection());

        Long proprietaire = reservationRepository.findAll().getFirst().getEtudiant().getId();
        assertThat(proprietaire)
                .as("le proprietaire doit venir de la session, jamais du formulaire")
                .isEqualTo(idEtudiant("Bilal Dupont"));
    }

    @Test
    @DisplayName("T12 : deux demandes successives ne laissent qu'une réservation active")
    void t12DeuxDemandesSuccessives() throws Exception {
        String date = ConfigurationHorlogeTest.DATE_FUTURE.toString();

        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-003")))
                        .param("dateReservation", date)
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/reservations")
                        .param("materielId", String.valueOf(idMateriel("MAT-003")))
                        .param("dateReservation", date)
                        .session(sessionAvecEtudiant("Bilal Dupont")))
                .andExpect(status().is3xxRedirection());

        long actives = reservationRepository.countByMaterielIdAndDateReservationAndStatut(
                idMateriel("MAT-003"), ConfigurationHorlogeTest.DATE_FUTURE, StatutReservation.ACTIVE);
        assertThat(actives).isEqualTo(1);
    }
}
