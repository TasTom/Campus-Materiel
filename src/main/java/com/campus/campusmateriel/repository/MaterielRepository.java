package com.campus.campusmateriel.repository;

import com.campus.campusmateriel.domain.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acces aux donnees du materiel.
 */
public interface MaterielRepository extends JpaRepository<Materiel, Long> {

    /**
     * Recherche par code metier (par exemple {@code MAT-001}).
     * Utilise par l'initialiseur pour rester idempotent.
     */
    Optional<Materiel> findByCode(String code);

    /** Liste du materiel, ordonnee par code, telle qu'affichee a l'utilisateur. */
    List<Materiel> findAllByOrderByCodeAsc();
}
