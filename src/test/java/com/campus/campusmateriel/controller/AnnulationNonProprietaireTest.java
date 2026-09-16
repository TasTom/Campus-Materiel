package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario <strong>T08</strong> : un etudiant tente d'annuler la reservation d'un autre.
 *
 * <p>L'enonce precise : « masquer un bouton ne suffit pas ». Ce test adresse donc la
 * requete <strong>directement au serveur</strong>, sans passer par l'interface et sans
 * jamais afficher le bouton d'annulation. Il verifie que le refus tient cote serveur et
 * que la reservation reste inchangee (FR-010, RG-05).</p>
 *
 * <p>Un parametre {@code etudiantId} est volontairement ajoute a la requete : il doit etre
 * ignore, car le proprietaire est deduit de la session, jamais du formulaire.</p>
 */
@DisplayName("T08 — annulation par un non-propriétaire, par requête directe")
class AnnulationNonProprietaireTest extends TestIntegrationSupport {

    @Test
    @DisplayName("T08 (FR-010, RG-05) : Bilal ne peut pas annuler la réservation d'Alice")
    void bilalNePeutPasAnnulerLaReservationDAlice() throws Exception {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId())
                        .session(sessionAvecEtudiant("Bilal Dupont")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"))
                .andExpect(flash().attributeExists("messageErreur"));

        Reservation rechargee = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(rechargee.estActive())
                .as("la reservation d'Alice doit rester active")
                .isTrue();
        assertThat(rechargee.getCleActive())
                .as("le creneau doit rester occupe")
                .isNotNull();
    }

    @Test
    @DisplayName("T08 (FR-010) : un identifiant d'étudiant forgé ne contourne pas le refus")
    void identifiantForgéNeContournePasLeRefus() throws Exception {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        // Bilal est l'etudiant courant et tente de se faire passer pour Alice en
        // ajoutant son identifiant dans la requete.
        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId())
                        .param("etudiantId", String.valueOf(idEtudiant("Alice Martin")))
                        .param("proprietaireId", String.valueOf(idEtudiant("Alice Martin")))
                        .session(sessionAvecEtudiant("Bilal Dupont")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .as("le proprietaire doit venir de la session : aucun parametre ne doit le remplacer")
                .isTrue();
    }

    @Test
    @DisplayName("FR-010 : sans étudiant courant, l'annulation est refusée")
    void sansEtudiantCourantRefusee() throws Exception {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId())
                        .session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .isTrue();
    }

    @Test
    @DisplayName("T06 : le propriétaire peut annuler sa propre réservation")
    void proprietairePeutAnnuler() throws Exception {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId())
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageSucces"));

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .isFalse();
    }

    @Test
    @DisplayName("T11 : annuler deux fois la même réservation ne change rien")
    void doubleAnnulationSansEffet() throws Exception {
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));
        MockHttpSession session = sessionAvecEtudiant("Alice Martin");

        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageSucces"));

        mockMvc.perform(post("/reservations/{id}/annulation", reservation.getId()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));

        assertThat(reservationRepository.count())
                .as("aucune ligne ne doit etre creee ni supprimee")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("FR-020 : annuler une réservation inexistante est refusé, sans erreur technique")
    void reservationInexistanteRefusee() throws Exception {
        mockMvc.perform(post("/reservations/{id}/annulation", 999_999L)
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messageErreur"));
    }
}
