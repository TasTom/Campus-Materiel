package com.campus.campusmateriel.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulaire de creation d'une reservation.
 *
 * <p><strong>Ce formulaire ne comporte volontairement aucun champ d'identifiant
 * d'etudiant.</strong> Le proprietaire de la reservation est toujours l'etudiant courant,
 * lu depuis la session cote serveur. Si un identifiant d'etudiant etait transporté par le
 * formulaire, il suffirait de modifier la page envoyee pour reserver au nom d'un autre.</p>
 *
 * <p>Les annotations ci-dessous couvrent les controles <em>de forme</em>. Les controles
 * <em>de contexte</em> (date passee, materiel inconnu, conflit de reservation) dependent de
 * l'etat des donnees : ils sont faits par {@code ReservationService}, jamais ici.</p>
 *
 * <p>Une date illisible ne produit pas de message technique : Spring range l'erreur de
 * conversion dans le resultat de validation, que le controleur traduit en motif metier
 * {@code DATE_INVALIDE} (CL-03, T10).</p>
 */
public class ReservationForm {

    @NotNull(message = "Le matériel est obligatoire.")
    private Long materielId;

    @NotNull(message = "La date de réservation est obligatoire.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateReservation;

    public ReservationForm() {
    }

    public ReservationForm(Long materielId, LocalDate dateReservation) {
        this.materielId = materielId;
        this.dateReservation = dateReservation;
    }

    public Long getMaterielId() {
        return materielId;
    }

    public void setMaterielId(Long materielId) {
        this.materielId = materielId;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }
}
