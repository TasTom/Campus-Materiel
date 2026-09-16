package com.campus.campusmateriel.service;

/**
 * Motif de refus d'une operation metier.
 *
 * <p>Le service ne renvoie pas de texte libre : il leve une
 * {@link RegleMetierException} portant l'un de ces motifs. Le texte affiche est choisi
 * a partir du motif, en un point unique ({@link MessagesRefusService}).</p>
 *
 * <p>Les tests portent sur le <strong>motif</strong>, pas sur la formulation : une
 * reformulation d'un message ne doit pas casser les tests.</p>
 *
 * <p>Cette enumeration et les messages associes sont decrits dans
 * {@code specs/001-reservation-materiel/contracts/messages.md}.</p>
 */
public enum MotifRefus {

    /** La date saisie est absente, vide ou illisible. */
    DATE_INVALIDE,

    /** Aucun etudiant courant n'a ete selectionne. */
    ETUDIANT_NON_SELECTIONNE,

    /** Aucun etudiant ne correspond a l'identifiant demande. */
    ETUDIANT_INCONNU,

    /** Aucun materiel ne correspond a l'identifiant demande. */
    MATERIEL_INCONNU,

    /** La date demandee est anterieure a la date courante. */
    DATE_PASSEE,

    /** Une reservation active existe deja sur ce couple (materiel, date), par un autre etudiant. */
    MATERIEL_INDISPONIBLE,

    /** L'etudiant courant a deja reserve ce materiel pour cette date (motif distinct). */
    DEJA_RESERVE_PAR_VOUS,

    /** Aucune reservation ne correspond a l'identifiant demande. */
    RESERVATION_INTROUVABLE,

    /** Le demandeur n'est pas le proprietaire de la reservation. */
    PAS_PROPRIETAIRE,

    /** Le jour reserve est passe : l'annulation n'est plus possible. */
    JOUR_RESERVE_PASSE,

    /** La reservation est deja annulee. */
    DEJA_ANNULEE
}
