package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.service.EtudiantCourantService;
import com.campus.campusmateriel.service.MessagesRefusService;
import com.campus.campusmateriel.service.MotifRefus;
import com.campus.campusmateriel.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Consultation du materiel et de sa disponibilite (HU2, FR-003, FR-004, FR-022).
 */
@Controller
public class MaterielController {

    private final ReservationService reservationService;
    private final EtudiantCourantService etudiantCourantService;
    private final MessagesRefusService messagesRefusService;

    public MaterielController(ReservationService reservationService,
                             EtudiantCourantService etudiantCourantService,
                             MessagesRefusService messagesRefusService) {
        this.reservationService = reservationService;
        this.etudiantCourantService = etudiantCourantService;
        this.messagesRefusService = messagesRefusService;
    }

    /** Point d'entree : redirige vers les disponibilites du jour. */
    @GetMapping("/")
    public String accueil() {
        return "redirect:/materiels";
    }

    /**
     * Affiche le materiel et sa disponibilite a une date.
     *
     * <p>La date est lue comme texte puis analysee manuellement : c'est ce qui permet de
     * produire un message comprehensible plutot qu'une erreur technique lorsqu'elle est
     * absente ou mal formee (CL-03, T10, RG-09).</p>
     *
     * <p>La page reste consultable sans etudiant courant : seule une action modifiant les
     * donnees est refusee dans ce cas (FR-017).</p>
     */
    @GetMapping("/materiels")
    public String afficherDisponibilites(
            @RequestParam(name = "date", required = false) String date,
            HttpSession session,
            Model modele) {

        LocalDate dateConsultee;
        if (date == null || date.isBlank()) {
            dateConsultee = reservationService.aujourdHui();
        } else {
            try {
                dateConsultee = LocalDate.parse(date);
            } catch (DateTimeParseException dateIllisible) {
                modele.addAttribute("messageErreur",
                        messagesRefusService.messagePour(MotifRefus.DATE_INVALIDE));
                return "materiels/liste";
            }
        }

        modele.addAttribute("dateConsultee", dateConsultee);
        modele.addAttribute("aujourdHui", reservationService.aujourdHui());
        modele.addAttribute("disponibilites", reservationService.disponibilites(dateConsultee));
        modele.addAttribute("etudiantCourant",
                etudiantCourantService.etudiantCourant(session).orElse(null));
        return "materiels/liste";
    }
}
