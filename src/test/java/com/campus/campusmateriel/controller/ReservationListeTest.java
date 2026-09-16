package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.config.DonneesDemoInitialiseur;
import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verification de la consultation des reservations de l'etudiant courant
 * (HU4, FR-009, FR-013, FR-017, FR-022).
 */
@DisplayName("Route de consultation de mes réservations")
class ReservationListeTest extends TestIntegrationSupport {

    @Autowired
    private DonneesDemoInitialiseur initialiseur;

    @Test
    @DisplayName("FR-009 : chaque étudiant ne voit que ses propres réservations")
    void chaqueEtudiantNeVoitQueSesReservations() throws Exception {
        mockMvc.perform(post("/reservations")
                .param("materielId", String.valueOf(idMateriel("MAT-001")))
                .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                .session(sessionAvecEtudiant("Alice Martin")));
        mockMvc.perform(post("/reservations")
                .param("materielId", String.valueOf(idMateriel("MAT-002")))
                .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                .session(sessionAvecEtudiant("Alice Martin")));
        mockMvc.perform(post("/reservations")
                .param("materielId", String.valueOf(idMateriel("MAT-003")))
                .param("dateReservation", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                .session(sessionAvecEtudiant("Bilal Dupont")));

        mockMvc.perform(get("/reservations").session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reservations", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(content().string(not(containsString("MAT-003"))));
    }

    @Test
    @DisplayName("FR-013 : une réservation annulée reste visible avec son état")
    void reservationAnnuleeResteVisible() throws Exception {
        MockHttpSession session = sessionAvecEtudiant("Alice Martin");
        Reservation reservation = reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));
        reservationService.annuler(reservation.getId(), idEtudiant("Alice Martin"));

        mockMvc.perform(get("/reservations").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reservations", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(content().string(containsString("Annulée")));
    }

    @Test
    @DisplayName("FR-022 : une liste vide produit un message explicite, sans erreur technique")
    void listeVideProduitUnMessage() throws Exception {
        mockMvc.perform(get("/reservations").session(sessionAvecEtudiant("Chloé Bernard")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("messageInfo"))
                .andExpect(content().string(containsString("aucune réservation")));
    }

    @Test
    @DisplayName("FR-022 (CL-05) : aucun matériel enregistré produit un message explicite")
    void aucunMaterielProduitUnMessage() throws Exception {
        // Le jeu de donnees fictif contient toujours cinq materiaux : pour atteindre le cas
        // « liste sans resultat » exige par FR-022, il faut vider la table. Sans ce test, le
        // message ne serait jamais exerce et l'exigence ne serait pas verifiee.
        //
        // La table est restauree dans le bloc finally : tous les tests partagent la meme base
        // en memoire, et l'initialiseur ne s'execute qu'au demarrage du contexte Spring.
        // Sans cette restauration, les tests suivants echouent en cascade.
        reservationRepository.deleteAll();
        materielRepository.deleteAll();
        try {
            // L'assertion porte sur un fragment sans apostrophe : Thymeleaf echappe les
            // apostrophes en HTML (&#39;), une comparaison sur le texte complet echouerait
            // pour une raison sans rapport avec l'exigence verifiee.
            mockMvc.perform(get("/materiels"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("messageInfo"))
                    .andExpect(content().string(containsString("Aucun matériel")))
                    .andExpect(content().string(not(containsString("Exception"))));
        } finally {
            initialiseur.run(mock(ApplicationArguments.class));
        }
    }

    @Test
    @DisplayName("FR-017 : sans étudiant courant, la page invite à en choisir un")
    void sansEtudiantCourantInviteAChoisir() throws Exception {
        mockMvc.perform(get("/reservations").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("messageInfo"))
                .andExpect(content().string(containsString("Choisissez un étudiant")));
    }

    @Test
    @DisplayName("FR-003 : la page des disponibilités affiche les cinq matériels")
    void pageDesDisponibilitesAfficheCinqMateriels() throws Exception {
        mockMvc.perform(get("/materiels"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("disponibilites", org.hamcrest.Matchers.hasSize(5)))
                .andExpect(content().string(containsString("MAT-005")));
    }

    @Test
    @DisplayName("T10 (FR-019) : une date illisible affiche un message, sans erreur technique")
    void dateIllisibleAfficheUnMessage() throws Exception {
        mockMvc.perform(get("/materiels").param("date", "pas-une-date"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("messageErreur"))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    @DisplayName("FR-004 : le matériel réservé apparaît comme « Réservé »")
    void materielReserveApparaitReserve() throws Exception {
        reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        mockMvc.perform(get("/materiels")
                        .param("date", ConfigurationHorlogeTest.DATE_FUTURE.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Réservé")));
    }

    @Test
    @DisplayName("FR-009 : le nom d'un autre étudiant n'est jamais affiché")
    void nomDAutreEtudiantJamaisAffiche() throws Exception {
        reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Bilal Dupont"));

        mockMvc.perform(get("/materiels")
                        .param("date", ConfigurationHorlogeTest.DATE_FUTURE.toString())
                        .session(sessionAvecEtudiant("Alice Martin")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Bilal Dupont"))));
    }
}
