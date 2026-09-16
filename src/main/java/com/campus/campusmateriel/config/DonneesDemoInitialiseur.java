package com.campus.campusmateriel.config;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.domain.Materiel;
import com.campus.campusmateriel.repository.EtudiantRepository;
import com.campus.campusmateriel.repository.MaterielRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Insere le jeu de donnees fictif au demarrage, de facon <strong>idempotente</strong>.
 *
 * <p>Chaque enregistrement est verifie par son identifiant metier (nom de l'etudiant,
 * code du materiel) avant d'etre ajoute. Consequences :</p>
 * <ul>
 *   <li>lancer l'application plusieurs fois ne cree aucun doublon (FR-023) ;</li>
 *   <li>les reservations existantes ne sont <strong>jamais</strong> supprimees ni
 *       recreees, ce qui est la condition du scenario T09 (persistance apres
 *       redemarrage, RG-08, FR-015).</li>
 * </ul>
 *
 * <p>Volontairement, ce composant ne vide aucune table. Un initialiseur qui recree
 * les donnees a chaque lancement ferait echouer T09.</p>
 */
@Component
public class DonneesDemoInitialiseur implements ApplicationRunner {

    private static final List<String> NOMS_ETUDIANTS = List.of(
            "Alice Martin",
            "Bilal Dupont",
            "Chloé Bernard");

    /**
     * Le materiel fictif. Chaque ligne represente un exemplaire physique unique :
     * deux ordinateurs identiques portent deux codes differents (RG-01).
     */
    private static final List<MaterielFictif> MATERIELS = List.of(
            new MaterielFictif("MAT-001", "Ordinateur portable A", "Informatique"),
            new MaterielFictif("MAT-002", "Ordinateur portable B", "Informatique"),
            new MaterielFictif("MAT-003", "Vidéoprojecteur A", "Projection"),
            new MaterielFictif("MAT-004", "Kit Arduino A", "Électronique"),
            new MaterielFictif("MAT-005", "Kit Arduino B", "Électronique"));

    private final EtudiantRepository etudiantRepository;
    private final MaterielRepository materielRepository;

    public DonneesDemoInitialiseur(EtudiantRepository etudiantRepository,
                                   MaterielRepository materielRepository) {
        this.etudiantRepository = etudiantRepository;
        this.materielRepository = materielRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initialiserEtudiants();
        initialiserMateriels();
    }

    private void initialiserEtudiants() {
        for (String nom : NOMS_ETUDIANTS) {
            if (etudiantRepository.findByNom(nom).isEmpty()) {
                etudiantRepository.save(new Etudiant(nom));
            }
        }
    }

    private void initialiserMateriels() {
        for (MaterielFictif fictif : MATERIELS) {
            if (materielRepository.findByCode(fictif.code()).isEmpty()) {
                materielRepository.save(
                        new Materiel(fictif.code(), fictif.nom(), fictif.categorie()));
            }
        }
    }

    /** Description d'un materiel du jeu de donnees fictif. */
    private record MaterielFictif(String code, String nom, String categorie) {
    }
}
