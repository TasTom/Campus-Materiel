package com.campus.campusmateriel.domain;

/**
 * Etat d'une reservation.
 *
 * <p>L'etat distingue l'origine d'une annulation (decision de clarification CA-02) :
 * une annulation demandee par l'etudiant n'est pas de meme nature qu'une annulation
 * decidee hors application.</p>
 *
 * <p><strong>Attention :</strong> {@link #ANNULEE_ADMINISTRATIVE} n'est produit par
 * aucune fonctionnalite de cette version. Aucune interface d'administration n'est au
 * perimetre. Cette valeur existe pour que le modele d'etat soit complet, et son
 * absence d'utilisation est verifiee par un test (exigence FR-027).</p>
 */
public enum StatutReservation {

    /** La reservation occupe le materiel a sa date. */
    ACTIVE,

    /** L'etudiant proprietaire a annule la reservation. */
    ANNULEE_PAR_ETUDIANT,

    /**
     * La reservation a ete annulee hors application.
     * Non atteignable dans cette version : voir FR-027.
     */
    ANNULEE_ADMINISTRATIVE;

    /** Vrai si la reservation occupe le materiel a sa date. */
    public boolean estActive() {
        return this == ACTIVE;
    }

    /** Vrai si la reservation a ete annulee, quelle qu'en soit l'origine. */
    public boolean estAnnulee() {
        return !estActive();
    }

    /** Libelle affiche a l'utilisateur, en francais. */
    public String libelle() {
        return estActive() ? "Active" : "Annulee";
    }
}
