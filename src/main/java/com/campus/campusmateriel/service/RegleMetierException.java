package com.campus.campusmateriel.service;

/**
 * Erreur metier signalee par le service a l'attention de l'utilisateur.
 *
 * <p>Elle transporte un {@link MotifRefus} et non un message : le texte affiche est
 * choisi a partir du motif par {@link MessagesRefusService}. Cette separation permet
 * de tester le comportement sans dependre de la formulation exacte des messages.</p>
 *
 * <p>Cette exception est levee <strong>avant toute ecriture</strong>, ce qui garantit
 * la partie « aucune modification des donnees » de RG-09.</p>
 */
public class RegleMetierException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient MotifRefus motif;

    public RegleMetierException(MotifRefus motif) {
        super(motif.name());
        this.motif = motif;
    }

    public MotifRefus getMotif() {
        return motif;
    }
}
