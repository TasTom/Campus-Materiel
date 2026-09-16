package com.campus.campusmateriel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Un exemplaire physique unique de materiel.
 *
 * <p>Deux ordinateurs identiques sont deux enregistrements distincts : le code
 * (par exemple {@code MAT-001}) distingue chaque exemplaire. Le code est unique
 * et sert d'identifiant metier a l'initialiseur.</p>
 *
 * <p>Aucune quantite n'est stockee : la disponibilite est deduite des
 * reservations. Stocker un compteur creerait une seconde source de verite
 * susceptible de diverger.</p>
 */
@Entity
@Table(name = "materiel")
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie;

    /** Constructeur requis par JPA. */
    protected Materiel() {
    }

    public Materiel(String code, String nom, String categorie) {
        this.code = code;
        this.nom = nom;
        this.categorie = categorie;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    /**
     * Deux materiels sont egaux si leurs identifiants sont egaux.
     * Un materiel sans identifiant n'est egal qu'a lui-meme.
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Materiel materiel)) {
            return false;
        }
        return id != null && id.equals(materiel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return code + " - " + nom;
    }
}
