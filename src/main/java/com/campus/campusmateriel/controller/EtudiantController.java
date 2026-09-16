package com.campus.campusmateriel.controller;

import com.campus.campusmateriel.service.EtudiantCourantService;
import com.campus.campusmateriel.service.MessagesRefusService;
import com.campus.campusmateriel.service.RegleMetierException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Selection et affichage de l'etudiant courant (HU1, FR-001, FR-002).
 *
 * <p>Le controleur ne fait que traduire une requete HTTP en appels au service : il ne
 * porte aucune regle metier.</p>
 */
@Controller
@RequestMapping("/etudiants")
public class EtudiantController {

    private final EtudiantCourantService etudiantCourantService;
    private final MessagesRefusService messagesRefusService;

    public EtudiantController(EtudiantCourantService etudiantCourantService,
                              MessagesRefusService messagesRefusService) {
        this.etudiantCourantService = etudiantCourantService;
        this.messagesRefusService = messagesRefusService;
    }

    /** Affiche les trois etudiants fictifs et signale l'etudiant courant. */
    @GetMapping("/selection")
    public String afficherSelection(HttpSession session, Model modele) {
        modele.addAttribute("etudiants", etudiantCourantService.tousLesEtudiants());
        modele.addAttribute("etudiantCourantId",
                etudiantCourantService.identifiantCourant(session).orElse(null));
        return "etudiants/selection";
    }

    /**
     * Memorise l'etudiant courant puis redirige.
     *
     * <p>La redirection apres un {@code POST} evite qu'un rafraichissement du navigateur
     * ne rejoue l'operation.</p>
     */
    @PostMapping("/selection")
    public String enregistrerSelection(@RequestParam(name = "etudiantId", required = false) Long etudiantId,
                                       HttpSession session,
                                       Model modele) {
        try {
            etudiantCourantService.definir(session, etudiantId);
        } catch (RegleMetierException refus) {
            modele.addAttribute("messageErreur", messagesRefusService.messagePour(refus.getMotif()));
            modele.addAttribute("etudiants", etudiantCourantService.tousLesEtudiants());
            modele.addAttribute("etudiantCourantId",
                    etudiantCourantService.identifiantCourant(session).orElse(null));
            return "etudiants/selection";
        }
        return "redirect:/materiels";
    }
}
