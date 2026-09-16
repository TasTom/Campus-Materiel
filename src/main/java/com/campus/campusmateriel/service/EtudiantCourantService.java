package com.campus.campusmateriel.service;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.repository.EtudiantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Suit l'identite simulee de l'etudiant courant pendant une session HTTP.
 *
 * <p><strong>Ce n'est pas une authentification.</strong> N'importe quel utilisateur peut
 * changer d'etudiant : c'est une simulation d'identite imposee par le perimetre du TP
 * (hypothese H-03). Les regles FR-009 et FR-010 limitent les actions au nom de l'etudiant
 * courant, mais ne protegent pas contre un utilisateur qui change volontairement
 * d'identite.</p>
 *
 * <p>Ce service est le seul composant metier qui connaisse la session : il fait le lien
 * entre le monde web et le reste de l'application. {@link ReservationService}, lui,
 * recoit l'identifiant en parametre et ignore tout de la requete.</p>
 */
@Service
public class EtudiantCourantService {

    /** Cle sous laquelle l'identifiant de l'etudiant courant est conserve en session. */
    public static final String CLE_SESSION = "etudiantCourantId";

    private final EtudiantRepository etudiantRepository;

    public EtudiantCourantService(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    /** Tous les etudiants fictifs, ordonnes par nom. */
    public List<Etudiant> tousLesEtudiants() {
        return etudiantRepository.findAllByOrderByNomAsc();
    }

    /**
     * Enregistre l'etudiant courant pour la session.
     *
     * @param session    session HTTP courante
     * @param etudiantId identifiant choisi
     * @throws RegleMetierException si l'identifiant ne correspond a aucun etudiant
     */
    public void definir(HttpSession session, Long etudiantId) {
        if (etudiantId == null) {
            throw new RegleMetierException(MotifRefus.ETUDIANT_INCONNU);
        }
        if (etudiantRepository.findById(etudiantId).isEmpty()) {
            throw new RegleMetierException(MotifRefus.ETUDIANT_INCONNU);
        }
        session.setAttribute(CLE_SESSION, etudiantId);
    }

    /**
     * Identifiant de l'etudiant courant, ou vide si aucun n'a ete choisi.
     */
    public Optional<Long> identifiantCourant(HttpSession session) {
        Object valeur = session.getAttribute(CLE_SESSION);
        return (valeur instanceof Long identifiant) ? Optional.of(identifiant) : Optional.empty();
    }

    /**
     * Etudiant courant, ou vide si aucun n'a ete choisi ou s'il n'existe plus.
     */
    public Optional<Etudiant> etudiantCourant(HttpSession session) {
        return identifiantCourant(session).flatMap(etudiantRepository::findById);
    }
}
