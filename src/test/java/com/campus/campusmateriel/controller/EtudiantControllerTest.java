package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.service.EtudiantCourantService;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verification de la selection de l'etudiant courant (HU1, FR-001, FR-002, FR-017).
 */
@DisplayName("Route de sélection de l'étudiant")
class EtudiantControllerTest extends TestIntegrationSupport {

    @Test
    @DisplayName("FR-001 : la page propose les trois étudiants fictifs")
    void laPageProposeTroisEtudiants() throws Exception {
        mockMvc.perform(get("/etudiants/selection"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("etudiants"))
                .andExpect(model().attribute("etudiants",
                        org.hamcrest.Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("FR-002 : choisir un étudiant le mémorise en session puis redirige")
    void choisirMemoriseEnSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/etudiants/selection")
                        .param("etudiantId", String.valueOf(idEtudiant("Alice Martin")))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/materiels"));

        assertThat(session.getAttribute(EtudiantCourantService.CLE_SESSION))
                .isEqualTo(idEtudiant("Alice Martin"));
    }

    @Test
    @DisplayName("FR-002 : changer d'étudiant remplace le choix précédent")
    void changerEtudiantRemplaceLeChoix() throws Exception {
        MockHttpSession session = sessionAvecEtudiant("Alice Martin");

        mockMvc.perform(post("/etudiants/selection")
                        .param("etudiantId", String.valueOf(idEtudiant("Bilal Dupont")))
                        .session(session))
                .andExpect(status().is3xxRedirection());

        assertThat(session.getAttribute(EtudiantCourantService.CLE_SESSION))
                .isEqualTo(idEtudiant("Bilal Dupont"));
    }

    @Test
    @DisplayName("FR-001 : un identifiant d'étudiant inconnu est refusé sans redirection")
    void etudiantInconnuRefuse() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/etudiants/selection")
                        .param("etudiantId", "999999")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("messageErreur"));

        assertThat(session.getAttribute(EtudiantCourantService.CLE_SESSION))
                .as("un identifiant inconnu ne doit pas etre memorise")
                .isNull();
    }

    @Test
    @DisplayName("FR-001 : un identifiant absent est refusé sans modification de la session")
    void etudiantAbsentRefuse() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/etudiants/selection").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("messageErreur"));

        assertThat(session.getAttribute(EtudiantCourantService.CLE_SESSION)).isNull();
    }
}
