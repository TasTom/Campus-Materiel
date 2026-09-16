package com.campus.campusmateriel.persistence;

import com.campus.campusmateriel.domain.Etudiant;
import com.campus.campusmateriel.domain.Materiel;
import com.campus.campusmateriel.domain.Reservation;
import com.campus.campusmateriel.domain.StatutReservation;
import com.campus.campusmateriel.service.MotifRefus;
import com.campus.campusmateriel.service.RegleMetierException;
import com.campus.campusmateriel.support.ConfigurationHorlogeTest;
import com.campus.campusmateriel.support.TestIntegrationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Scenario <strong>T12</strong> et cas limite <strong>CL-07</strong> : deux demandes
 * visent le meme materiel a la meme date.
 *
 * <p>L'enonce demande au minimum deux demandes successives (couvertes ailleurs) et
 * propose, en approfondissement, deux demandes concurrentes. Ce test realise les deux
 * et verifie la protection de second niveau : la contrainte d'unicite en base.</p>
 *
 * <p>La verification porte sur le resultat observe (« au plus une reservation active »),
 * jamais sur le mecanisme employe (FR-021).</p>
 */
@DisplayName("T12 — conflit de réservation sous concurrence")
class ConflitConcurrentTest extends TestIntegrationSupport {

    @Test
    @DisplayName("FR-021 : la base refuse deux réservations actives sur le même couple")
    void laBaseRefuseDeuxReservationsActives() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Materiel materiel = materielRepository.findByCode("MAT-001").orElseThrow();
        Etudiant alice = etudiantRepository.findByNom("Alice Martin").orElseThrow();
        Etudiant bilal = etudiantRepository.findByNom("Bilal Dupont").orElseThrow();

        reservationRepository.saveAndFlush(new Reservation(alice, materiel, date));

        // Ecriture directe, en contournant le service : c'est la base qui doit arbitrer.
        assertThatThrownBy(() -> reservationRepository.saveAndFlush(
                new Reservation(bilal, materiel, date)))
                .as("la contrainte d'unicite doit rendre impossible une seconde reservation active")
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(nombreDeReservationsActives("MAT-001", date)).isEqualTo(1);
    }

    @Test
    @DisplayName("FR-021 (RG-07) : deux réservations annulées sur le même couple restent possibles")
    void deuxReservationsAnnuleesRestentPossibles() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        Reservation premiere = reservationService.reserver(idMateriel("MAT-001"), date, alice);
        reservationService.annuler(premiere.getId(), alice);
        Reservation seconde = reservationService.reserver(idMateriel("MAT-001"), date, bilal);
        reservationService.annuler(seconde.getId(), bilal);

        assertThat(reservationRepository.count())
                .as("l'historique des deux annulations doit etre conserve")
                .isEqualTo(2);
        assertThat(nombreDeReservationsActives("MAT-001", date)).isZero();
    }

    @Test
    @DisplayName("FR-021 : deux demandes concurrentes laissent au plus une réservation active")
    void deuxDemandesConcurrentes() throws Exception {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long materielId = idMateriel("MAT-004");
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        // Les deux threads attendent le meme signal avant d'appeler le service : les deux
        // controles applicatifs ont ainsi une forte chance de s'executer avant les deux
        // ecritures, ce qui met la contrainte de base a l'epreuve.
        CountDownLatch signal = new CountDownLatch(1);
        AtomicInteger refus = new AtomicInteger();
        AtomicInteger succes = new AtomicInteger();

        Callable<Void> demandeAlice = () -> tenterReservation(signal, materielId, date, alice, succes, refus);
        Callable<Void> demandeBilal = () -> tenterReservation(signal, materielId, date, bilal, succes, refus);

        try (ExecutorService executeur = Executors.newFixedThreadPool(2)) {
            Future<Void> premier = executeur.submit(demandeAlice);
            Future<Void> second = executeur.submit(demandeBilal);
            signal.countDown();
            premier.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        }

        assertThat(succes.get())
                .as("une seule demande au plus doit aboutir")
                .isLessThanOrEqualTo(1);
        assertThat(nombreDeReservationsActives("MAT-004", date))
                .as("au plus une reservation active par couple (materiel, date)")
                .isLessThanOrEqualTo(1);
    }

    /**
     * Tente une reservation apres le signal commun et compte le resultat.
     *
     * <p>Tout refus est accepte, y compris une violation de contrainte traduite en motif
     * metier : ce qui est verifie est le resultat final en base, pas le chemin parcouru.</p>
     */
    private Void tenterReservation(CountDownLatch signal, Long materielId, LocalDate date,
                                   Long etudiantId, AtomicInteger succes, AtomicInteger refus)
            throws InterruptedException {
        signal.await(10, TimeUnit.SECONDS);
        try {
            reservationService.reserver(materielId, date, etudiantId);
            succes.incrementAndGet();
        } catch (RegleMetierException refusMetier) {
            refus.incrementAndGet();
            assertThat(refusMetier.getMotif()).isIn(
                    MotifRefus.MATERIEL_INDISPONIBLE, MotifRefus.DEJA_RESERVE_PAR_VOUS);
        } catch (DataIntegrityViolationException conflitBrut) {
            // La contrainte a arbitre mais l'erreur n'a pas ete traduite : le test doit le
            // signaler, car RG-09 interdit de laisser remonter un detail technique.
            refus.incrementAndGet();
            throw new AssertionError(
                    "La violation de contrainte doit etre traduite en refus metier", conflitBrut);
        }
        return null;
    }

    @Test
    @DisplayName("FR-021 : les demandes concurrentes ne bloquent pas les autres matériels")
    void demandesConcurrentesSurAutresMateriels() throws Exception {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");

        try (ExecutorService executeur = Executors.newFixedThreadPool(2)) {
            List<Future<?>> travaux = List.of(
                    executeur.submit(() -> reservationService.reserver(idMateriel("MAT-001"), date, alice)),
                    executeur.submit(() -> reservationService.reserver(idMateriel("MAT-002"), date, alice)));
            for (Future<?> travail : travaux) {
                travail.get(30, TimeUnit.SECONDS);
            }
        }

        assertThat(nombreDeReservationsActives("MAT-001", date)).isEqualTo(1);
        assertThat(nombreDeReservationsActives("MAT-002", date)).isEqualTo(1);
        assertThat(reservationRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("FR-004 : la contrainte d'unicité ne concerne que les réservations actives")
    void contrainteNeConcerneQueLesActives() {
        LocalDate date = ConfigurationHorlogeTest.DATE_FUTURE;
        Long alice = idEtudiant("Alice Martin");
        Long bilal = idEtudiant("Bilal Dupont");

        Reservation ancienne = reservationService.reserver(idMateriel("MAT-005"), date, alice);
        reservationService.annuler(ancienne.getId(), alice);
        Reservation nouvelle = reservationService.reserver(idMateriel("MAT-005"), date, bilal);

        assertThat(reservationRepository.count()).isEqualTo(2);
        assertThat(reservationRepository.findById(nouvelle.getId()).orElseThrow().getStatut())
                .isEqualTo(StatutReservation.ACTIVE);
        assertThat(reservationRepository.findById(ancienne.getId()).orElseThrow().getStatut())
                .isEqualTo(StatutReservation.ANNULEE_PAR_ETUDIANT);
    }
}
