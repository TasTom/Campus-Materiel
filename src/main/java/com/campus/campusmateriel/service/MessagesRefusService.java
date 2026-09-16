package com.campus.campusmateriel.service;

import org.springframework.stereotype.Service;

/**
 * Traduit un {@link MotifRefus} en message affichable.
 *
 * <p>Point unique de traduction : c'est ce qui garantit que chaque refus produit un
 * message francais, sans terme technique (RG-09, CL-05). Aucun message affiche ne doit
 * contenir de nom de table, de classe ni de trace d'execution.</p>
 *
 * <p>Les textes sont decrits dans
 * {@code specs/001-reservation-materiel/contracts/messages.md}.</p>
 */
@Service
public class MessagesRefusService {

    /**
     * Retourne le message francais correspondant au motif de refus.
     *
     * @param motif le motif de refus
     * @return un message comprehensible, sans terme technique
     */
    public String messagePour(MotifRefus motif) {
        return switch (motif) {
            case DATE_INVALIDE ->
                    "La date saisie est invalide. Utilisez le format année-mois-jour.";
            case ETUDIANT_NON_SELECTIONNE ->
                    "Veuillez d'abord choisir un étudiant.";
            case ETUDIANT_INCONNU ->
                    "Cet étudiant n'existe pas.";
            case MATERIEL_INCONNU ->
                    "Ce matériel n'existe pas.";
            case DATE_PASSEE ->
                    "La date choisie est déjà passée. Choisissez aujourd'hui ou une date future.";
            case MATERIEL_INDISPONIBLE ->
                    "Ce matériel est déjà réservé à cette date.";
            case DEJA_RESERVE_PAR_VOUS ->
                    "Vous avez déjà réservé ce matériel pour cette date.";
            case LIMITE_RESERVATIONS_JOUR ->
                    "Vous avez déjà deux réservations actives pour cette date. "
                            + "Annulez-en une pour pouvoir réserver un autre matériel ce jour-là.";
            case RESERVATION_INTROUVABLE ->
                    "Cette réservation n'existe pas.";
            case PAS_PROPRIETAIRE ->
                    "Vous ne pouvez annuler que vos propres réservations.";
            case JOUR_RESERVE_PASSE ->
                    "La date de cette réservation est passée : elle ne peut plus être annulée.";
            case DEJA_ANNULEE ->
                    "Cette réservation est déjà annulée.";
        };
    }

    /** Message de confirmation d'une reservation creee (FR-005). */
    public String confirmationReservation(String dateAffichee) {
        return "Réservation enregistrée pour le " + dateAffichee + ".";
    }

    /** Message de confirmation d'une annulation (FR-013, FR-014). */
    public String confirmationAnnulation() {
        return "Réservation annulée. Le matériel est de nouveau disponible à cette date.";
    }

    /** Message affiche lorsqu'aucun materiel n'est enregistre (FR-022). */
    public String listeMaterielsVide() {
        return "Aucun matériel n'est enregistré.";
    }

    /** Message affiche lorsqu'un etudiant n'a aucune reservation (FR-022). */
    public String listeReservationsVide() {
        return "Vous n'avez aucune réservation pour le moment.";
    }

    /** Message invitant a choisir un etudiant avant de consulter ses reservations (FR-017). */
    public String choisirUnEtudiant() {
        return "Choisissez un étudiant pour consulter ses réservations.";
    }
}
