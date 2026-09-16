package com.campus.campusmateriel.repository;

import com.campus.campusmateriel.domain.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acces aux donnees des etudiants.
 */
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    /** Recherche par nom exact, utilise par l'initialiseur pour rester idempotent. */
    Optional<Etudiant> findByNom(String nom);

    /** Liste des etudiants, ordonnee par nom. */
    List<Etudiant> findAllByOrderByNomAsc();
}
