package com.campus.campusmateriel.repository;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acces aux donnees des reservations.
 *
 * <p>Toutes les methodes de recherche de conflit filtrent sur le statut : seules les
 * reservations actives rendent un materiel indisponible (RG-02). Les reservations
 * annulees ne comptent jamais dans la detection d'un conflit, ce qui est la
 * condition pour que le scenario T07 (reserver apres une annulation) fonctionne.</p>
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Vrai s'il existe une reservation du statut demande pour ce materiel a cette date.
     * Sert a detecter un conflit (FR-004, FR-021).
     */
    boolean existsByMaterielIdAndDateReservationAndStatut(
            Long materielId, LocalDate dateReservation, StatutReservation statut);

    /**
     * Retrouve la reservation d'un etudiant pour un couple (materiel, date) et un statut.
     * Sert a distinguer « vous avez deja reserve » de « materiel indisponible » (FR-025).
     */
    Optional<Reservation> findByMaterielIdAndDateReservationAndStatutAndEtudiantId(
            Long materielId, LocalDate dateReservation, StatutReservation statut, Long etudiantId);

    /**
     * Liste les reservations d'un etudiant, de la plus recente a la plus ancienne (FR-009).
     *
     * <p>Le materiel et l'etudiant sont charges avec la reservation : la page les affiche
     * et {@code spring.jpa.open-in-view} est desactive, ce qui interdit tout chargement
     * paresseux pendant le rendu de la vue.</p>
     */
    @EntityGraph(attributePaths = {"materiel", "etudiant"})
    List<Reservation> findByEtudiantIdOrderByDateReservationDesc(Long etudiantId);

    /**
     * Compte les reservations d'un statut donne pour un couple (materiel, date).
     *
     * <p>Sert aux verifications : apres un refus, ce compte doit valoir 0 ou rester a 1,
     * jamais davantage (RG-02, scenarios T02 et T12).</p>
     */
    long countByMaterielIdAndDateReservationAndStatut(
            Long materielId, LocalDate dateReservation, StatutReservation statut);

    /**
     * Compte les reservations actives d'un etudiant a une date.
     *
     * <p>Porte la limite de deux reservations actives par journee (FR-008, FR-028) :
     * {@code ReservationService.refuserSiLimiteDuJourAtteinte} compare ce compte a la limite
     * avant toute ecriture. Seules les reservations actives sont comptees, afin qu'une
     * annulation rende immediatement une possibilite (RG-07).</p>
     */
    long countByEtudiantIdAndDateReservationAndStatut(
            Long etudiantId, LocalDate dateReservation, StatutReservation statut);
}
