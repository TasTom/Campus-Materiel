package com.campus.campusmateriel.repository;

import com.campus.campusmateriel.domain.Etudiant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Charge un etudiant en posant un verrou d'ecriture jusqu'a la fin de la transaction.
     *
     * <p>Ce verrou sert a rendre fiable la limite de deux reservations actives par jour
     * (FR-008, FR-028) : deux demandes du meme etudiant, visant deux materiels differents le
     * meme jour, sont serialisees. Sans lui, chaque demande compterait « une » reservation
     * avant que l'autre n'ait ecrit, et les deux passeraient.</p>
     *
     * <p>La contrainte d'unicite sur la cle technique reste necessaire pour le conflit entre
     * deux <strong>etudiants differents</strong> visant le meme materiel : le verrou d'etudiant
     * ne les concerne pas.</p>
     *
     * @param id identifiant de l'etudiant
     * @return l'etudiant verrouille, ou vide s'il n'existe pas
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Etudiant e where e.id = :id")
    Optional<Etudiant> findByIdForUpdate(@Param("id") Long id);
}
