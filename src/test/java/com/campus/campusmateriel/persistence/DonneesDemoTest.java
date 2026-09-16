package com.campus.campusmateriel.persistence;

import com.campus.campusmateriel.config.DonneesDemoInitialiseur;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verification de l'initialisation des donnees fictives (FR-015, FR-023, hypothese H-04).
 *
 * <p>Verifie que l'initialiseur est <strong>idempotent</strong> : l'executer plusieurs fois
 * ne cree aucun doublon et ne supprime aucune reservation. C'est la condition du scenario
 * T09 (persistance apres redemarrage, RG-08) : un initialiseur qui recreerait les donnees
 * ferait perdre les reservations.</p>
 */
@DisplayName("Initialisation des données fictives")
class DonneesDemoTest extends TestIntegrationSupport {

    @Autowired
    private DonneesDemoInitialiseur initialiseur;

    @Test
    @DisplayName("Le jeu de données contient les trois étudiants et les cinq matériels attendus")
    void jeuDeDonneesConforme() {
        assertThat(etudiantRepository.findAllByOrderByNomAsc())
                .extracting(etudiant -> etudiant.getNom())
                .containsExactly("Alice Martin", "Bilal Dupont", "Chloé Bernard");

        assertThat(materielRepository.findAllByOrderByCodeAsc())
                .extracting(materiel -> materiel.getCode())
                .containsExactly("MAT-001", "MAT-002", "MAT-003", "MAT-004", "MAT-005");
    }

    @Test
    @DisplayName("FR-023 : chaque exemplaire physique possède un code distinct")
    void chaqueExemplaireAUnCodeDistinct() {
        assertThat(materielRepository.findAll())
                .extracting(materiel -> materiel.getCode())
                .doesNotHaveDuplicates()
                .hasSize(5);
    }

    @Test
    @DisplayName("FR-015 (RG-08, H-04) : relancer l'initialiseur ne duplique ni ne supprime rien")
    void initialiseurIdempotent() {
        long etudiantsAvant = etudiantRepository.count();
        long materielsAvant = materielRepository.count();
        reservationService.reserver(
                idMateriel("MAT-001"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));
        long reservationsAvant = reservationRepository.count();

        ApplicationArguments arguments = mock(ApplicationArguments.class);
        initialiseur.run(arguments);
        initialiseur.run(arguments);

        assertThat(etudiantRepository.count())
                .as("aucun doublon d'etudiant ne doit apparaitre")
                .isEqualTo(etudiantsAvant);
        assertThat(materielRepository.count())
                .as("aucun doublon de materiel ne doit apparaitre")
                .isEqualTo(materielsAvant);
        assertThat(reservationRepository.count())
                .as("l'initialiseur ne doit jamais toucher aux reservations (RG-08, T09)")
                .isEqualTo(reservationsAvant);
    }

    @Test
    @DisplayName("FR-015 : une réservation survit à une réexécution de l'initialiseur")
    void reservationSurvitALInitialiseur() {
        var reservation = reservationService.reserver(
                idMateriel("MAT-003"), ConfigurationHorlogeTest.DATE_FUTURE, idEtudiant("Alice Martin"));

        initialiseur.run(mock(ApplicationArguments.class));

        assertThat(reservationRepository.findById(reservation.getId()))
                .as("la reservation doit toujours etre presente apres un redemarrage simule")
                .isPresent();
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().estActive())
                .isTrue();
    }
}
