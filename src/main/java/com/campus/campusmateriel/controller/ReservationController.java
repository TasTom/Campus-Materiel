package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.dto.ReservationForm;
import com.campus.campusmateriel.service.EtudiantCourantService;
import com.campus.campusmateriel.service.MessagesRefusService;
import com.campus.campusmateriel.service.MotifRefus;
import com.campus.campusmateriel.service.RegleMetierException;
import com.campus.campusmateriel.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Creation, consultation et annulation des reservations (HU3, HU4, HU5).
 *
 * <p>Ce controleur ne porte aucune regle metier : il traduit les requetes HTTP en appels
 * a {@link ReservationService}, choisit le message a afficher et redirige apres toute
 * operation reussie.</p>
 */
@Controller
public class ReservationController {

    /** Format d'affichage des dates aux utilisateurs (jour mois annee). */
    private static final DateTimeFormatter AFFICHAGE_DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    private final ReservationService reservationService;
    private final EtudiantCourantService etudiantCourantService;
    private final MessagesRefusService messagesRefusService;

    public ReservationController(ReservationService reservationService,
                                 EtudiantCourantService etudiantCourantService,
                                 MessagesRefusService messagesRefusService) {
        this.reservationService = reservationService;
        this.etudiantCourantService = etudiantCourantService;
        this.messagesRefusService = messagesRefusService;
    }

    // ------------------------------------------------------------------
    // Creation d'une reservation (HU3)
    // ------------------------------------------------------------------

    /**
     * Cree une reservation pour l'etudiant courant.
     *
     * <p>L'identifiant de l'etudiant n'est <strong>jamais</strong> lu dans le formulaire :
     * il provient de la session. Un parametre {@code etudiantId} envoye par un client est
     * donc ignore, ce qui rend le scenario T08 concluant meme avec une requete forgee.</p>
     *
     * <p>En cas de refus, aucune donnee n'est ecrite (RG-09) : le message est place en
     * attribut de redirection et la page des disponibilites est reaffichee.</p>
     */
    @PostMapping("/reservations")
    public String creer(@Validated @ModelAttribute("reservationForm") ReservationForm formulaire,
                        BindingResult resultatValidation,
                        HttpSession session,
                        RedirectAttributes attributsRedirection) {

        if (resultatValidation.hasErrors()) {
            attributsRedirection.addFlashAttribute("messageErreur",
                    messageDeValidation(resultatValidation));
            return redirectionVersDisponibilites(formulaire.getDateReservation());
        }

        Long etudiantId = etudiantCourantService.identifiantCourant(session).orElse(null);
        try {
            Reservation reservation = reservationService.reserver(
                    formulaire.getMaterielId(), formulaire.getDateReservation(), etudiantId);
            attributsRedirection.addFlashAttribute("messageSucces",
                    messagesRefusService.confirmationReservation(
                            reservation.getDateReservation().format(AFFICHAGE_DATE)));
            return "redirect:/reservations";
        } catch (RegleMetierException refus) {
            attributsRedirection.addFlashAttribute("messageErreur",
                    messagesRefusService.messagePour(refus.getMotif()));
            return redirectionVersDisponibilites(formulaire.getDateReservation());
        }
    }

    /**
     * Choisit le message a afficher pour une erreur de formulaire.
     *
     * <p>Une date illisible produit une erreur de conversion, et non une erreur de
     * contrainte : elle est traduite par le motif metier {@code DATE_INVALIDE} afin que
     * l'utilisateur recoive une explication comprehensible plutot qu'un message technique.</p>
     */
    private String messageDeValidation(BindingResult resultatValidation) {
        boolean dateIllisible = resultatValidation.getFieldErrors("dateReservation").stream()
                .anyMatch(erreur -> "typeMismatch".equals(erreur.getCode()));
        if (dateIllisible) {
            return messagesRefusService.messagePour(MotifRefus.DATE_INVALIDE);
        }
        FieldError premiereErreur = resultatValidation.getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        if (premiereErreur != null && premiereErreur.getDefaultMessage() != null) {
            return premiereErreur.getDefaultMessage();
        }
        return messagesRefusService.messagePour(MotifRefus.DATE_INVALIDE);
    }

    /** Revient sur la page des disponibilites, si possible a la date consultee. */
    private String redirectionVersDisponibilites(LocalDate date) {
        return (date == null) ? "redirect:/materiels" : "redirect:/materiels?date=" + date;
    }

    // ------------------------------------------------------------------
    // Consultation des reservations de l'etudiant courant (HU4)
    // ------------------------------------------------------------------

    /**
     * Affiche les reservations de l'etudiant courant, annulees comprises (FR-009, FR-013).
     */
    @GetMapping("/reservations")
    public String lister(HttpSession session, Model modele) {
        Long etudiantId = etudiantCourantService.identifiantCourant(session).orElse(null);
        if (etudiantId == null) {
            modele.addAttribute("messageInfo", messagesRefusService.choisirUnEtudiant());
            return "reservations/liste";
        }
        modele.addAttribute("etudiantCourant",
                etudiantCourantService.etudiantCourant(session).orElse(null));
        List<Reservation> reservations = reservationService.reservationsDe(etudiantId);
        modele.addAttribute("reservations", reservations);
        modele.addAttribute("annulationPossible",
                reservations.stream().collect(java.util.stream.Collectors.toMap(
                        Reservation::getId,
                        reservationService::annulationPossible)));
        if (reservations.isEmpty()) {
            modele.addAttribute("messageInfo", messagesRefusService.listeReservationsVide());
        }
        return "reservations/liste";
    }

    // ------------------------------------------------------------------
    // Annulation (HU5)
    // ------------------------------------------------------------------

    /**
     * Annule une reservation appartenant a l'etudiant courant.
     *
     * <p>Aucun identifiant d'etudiant n'est accepte en parametre : le proprietaire est
     * deduit de la session. C'est ce qui rend le refus effectif meme lorsqu'un client
     * adresse directement la requete au serveur (T08, FR-010).</p>
     */
    @PostMapping("/reservations/{id}/annulation")
    public String annuler(@PathVariable("id") Long reservationId,
                          HttpSession session,
                          RedirectAttributes attributsRedirection) {
        Long etudiantId = etudiantCourantService.identifiantCourant(session).orElse(null);
        try {
            reservationService.annuler(reservationId, etudiantId);
            attributsRedirection.addFlashAttribute("messageSucces",
                    messagesRefusService.confirmationAnnulation());
        } catch (RegleMetierException refus) {
            attributsRedirection.addFlashAttribute("messageErreur",
                    messagesRefusService.messagePour(refus.getMotif()));
        }
        return "redirect:/reservations";
    }
}
