package com.campus.campusmateriel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Un etudiant fictif autorise a emprunter du materiel.
 *
 * <p>Le nom est unique : c'est l'identifiant metier utilise par l'initialiseur
 * pour verifier l'existence d'un etudiant avant de l'ajouter, ce qui rend
 * l'initialisation idempotente (voir {@code DonneesDemoInitialiseur}).</p>
 */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    /** Constructeur requis par JPA. */
    protected Etudiant() {
    }

    public Etudiant(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Deux etudiants sont egaux si leurs identifiants sont egaux.
     * Un etudiant sans identifiant n'est egal qu'a lui-meme.
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Etudiant etudiant)) {
            return false;
        }
        return id != null && id.equals(etudiant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nom;
    }
}
