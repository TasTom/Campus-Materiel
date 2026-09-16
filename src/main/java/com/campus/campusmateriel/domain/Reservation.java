package com.campus.campusmateriel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * L'occupation d'un materiel par un etudiant a une date donnee.
 *
 * <p>La reservation porte sur une <strong>journee entiere</strong> : la date est un
 * {@link LocalDate}, pas un horaire (hypothese H-01 de la specification).</p>
 *
 * <p>Une reservation annulee n'est jamais supprimee ni reactiver : conserver
 * l'enregistrement est ce qui permet de satisfaire RG-07 (« conserver l'historique »)
 * et T07 (« le materiel redevient reservable ») en meme temps. Une nouvelle
 * reservation cree un nouvel enregistrement.</p>
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutReservation statut;

    /**
     * Colonne technique garantissant qu'une seule reservation active existe par
     * couple (materiel, date).
     *
     * <p>Valeur : {@code <code du materiel>|<date>} lorsque la reservation est
     * active, {@code null} lorsqu'elle est annulee. Un index unique autorise
     * plusieurs valeurs nulles, ce qui permet de conserver plusieurs reservations
     * annulees pour un meme couple, tout en interdisant deux reservations actives.</p>
     *
     * <p>Une contrainte d'unicite simple sur (materiel, date) serait incorrecte :
     * elle empecherait toute nouvelle reservation apres une annulation, ce qui
     * violerait RG-07 et ferait echouer le scenario T07.</p>
     *
     * <p>Cette colonne n'est jamais affichee ni lue pour decider d'une regle :
     * elle n'existe que pour rendre la garantie declarative en base.</p>
     */
    @Column(name = "cle_active", unique = true, length = 40)
    private String cleActive;

    /** Constructeur requis par JPA. */
    protected Reservation() {
    }

    /**
     * Cree une reservation active, dont la cle technique est derivee du materiel
     * et de la date.
     */
    public Reservation(Etudiant etudiant, Materiel materiel, LocalDate dateReservation) {
        this.etudiant = etudiant;
        this.materiel = materiel;
        this.dateReservation = dateReservation;
        this.statut = StatutReservation.ACTIVE;
        this.cleActive = construireCleActive(materiel, dateReservation);
    }

    /**
     * Construit la valeur de la cle technique pour un couple (materiel, date).
     *
     * @return la cle, ou {@code null} si l'un des elements est absent
     */
    public static String construireCleActive(Materiel materiel, LocalDate date) {
        if (materiel == null || materiel.getCode() == null || date == null) {
            return null;
        }
        return materiel.getCode() + "|" + date;
    }

    public Long getId() {
        return id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    /** Valeur technique, exposee uniquement pour les tests et le diagnostic. */
    public String getCleActive() {
        return cleActive;
    }

    /** Vrai si la reservation occupe encore le materiel a sa date. */
    public boolean estActive() {
        return statut != null && statut.estActive();
    }

    /**
     * Annule la reservation demandee par l'etudiant et libere le materiel.
     *
     * <p>L'enregistrement est conserve : l'historique n'est jamais detruit (RG-07).
     * La cle technique repasse a {@code null}, ce qui autorise une nouvelle
     * reservation sur le meme couple (FR-014, T07).</p>
     *
     * <p>Cette methode attribue toujours {@link StatutReservation#ANNULEE_PAR_ETUDIANT}.
     * L'etat {@code ANNULEE_ADMINISTRATIVE} n'est jamais produit par l'application
     * (FR-027).</p>
     */
    public void annulerParEtudiant() {
        this.statut = StatutReservation.ANNULEE_PAR_ETUDIANT;
        this.cleActive = null;
    }

    /**
     * Deux reservations sont egales si leurs identifiants sont egaux.
     * Une reservation sans identifiant n'est egale qu'a elle-meme.
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Reservation reservation)) {
            return false;
        }
        return id != null && id.equals(reservation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id
                + ", materiel=" + (materiel == null ? "?" : materiel.getCode())
                + ", dateReservation=" + dateReservation
                + ", statut=" + statut + '}';
    }
}
