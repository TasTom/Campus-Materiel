package com.campus.campusmateriel.support;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.domain.Materiel;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.repository.EtudiantRepository;
import com.campus.campusmateriel.repository.MaterielRepository;
import com.campus.campusmateriel.repository.ReservationRepository;
import com.campus.campusmateriel.service.EtudiantCourantService;
import com.campus.campusmateriel.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

/**
 * Socle commun des tests d'integration.
 *
 * <p>Chaque test part d'un etat connu : les <strong>reservations</strong> sont supprimees
 * avant chaque test, tandis que les etudiants et le materiel du jeu de donnees fictif
 * restent en place (ils sont recrees par l'initialiseur, qui est idempotent).</p>
 *
 * <p>Le nettoyage n'est pas fait dans une transaction de test : les scenarios de
 * concurrence (T12) ont besoin de transactions reellement independantes.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ConfigurationHorlogeTest.class)
public abstract class TestIntegrationSupport {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected MaterielRepository materielRepository;

    @Autowired
    protected EtudiantRepository etudiantRepository;

    @Autowired
    protected ReservationService reservationService;

    @Autowired
    protected EtudiantCourantService etudiantCourantService;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void reinitialiserLesReservations() {
        reservationRepository.deleteAll();
    }

    /** Identifiant du materiel portant ce code, par exemple {@code MAT-001}. */
    protected Long idMateriel(String code) {
        return materielRepository.findByCode(code)
                .map(Materiel::getId)
                .orElseThrow(() -> new IllegalStateException("Materiel absent du jeu de donnees : " + code));
    }

    /** Identifiant de l'etudiant portant ce nom, par exemple {@code Alice Martin}. */
    protected Long idEtudiant(String nom) {
        return etudiantRepository.findByNom(nom)
                .map(Etudiant::getId)
                .orElseThrow(() -> new IllegalStateException("Etudiant absent du jeu de donnees : " + nom));
    }

    /**
     * Nombre de reservations actives sur un couple (materiel, date).
     *
     * <p>Sert a verifier qu'un refus n'a rien ecrit : la valeur attendue est 0 ou 1,
     * jamais davantage (RG-02, T02, T12).</p>
     */
    protected long nombreDeReservationsActives(String codeMateriel, LocalDate date) {
        return reservationRepository.countByMaterielIdAndDateReservationAndStatut(
                idMateriel(codeMateriel), date, StatutReservation.ACTIVE);
    }

    /**
     * Construit une session simulant un etudiant deja selectionne.
     *
     * <p>Permet de tester les routes qui dependent de l'etudiant courant sans passer par
     * la page de selection.</p>
     */
    protected MockHttpSession sessionAvecEtudiant(String nomEtudiant) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(EtudiantCourantService.CLE_SESSION, idEtudiant(nomEtudiant));
        return session;
    }
}
