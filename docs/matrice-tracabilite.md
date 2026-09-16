# Matrice de traçabilité — Campus Matériel

**Version** : commit `2f29171` | **Date** : 16 septembre 2026

Cette matrice relie chaque règle métier et chaque exigence à la tâche qui l'implémente et à la
vérification qui atteste son comportement.

**Lecture** : aucune ligne ne doit rester sans vérification. Aucun scénario T01 à T12 ne doit être
orphelin.

---

## 1. Règles métier RG-01 à RG-09

| Règle | Exigences | Tâche | Implémentation | Vérification | Scénarios |
|---|---|---|---|---|---|
| RG-01 — Réservation = étudiant + équipement + date | FR-005, FR-023 | T006, T020 | `Reservation` (`@ManyToOne` obligatoires, `LocalDate`) | `ReservationServiceReservationTest`, `DonneesDemoTest.chaqueExemplaireAUnCodeDistinct` | T01, T05 |
| RG-02 — Une seule réservation active par équipement et par date | FR-004, FR-006, FR-021, FR-025 | T006, T015, T020 | `ReservationService.refuserSiDejaReserve`, colonne `cle_active` unique | `ReservationServiceReservationTest.t02…`, `ConflitConcurrentTest` | T02, T07, T12 |
| RG-03 — Aujourd'hui ou futur, jamais passé | FR-007 | T020 | `ReservationService.reserver` (`date.isBefore(aujourdHui())`) | `ReservationServiceReservationTest.t04DatePasseeRefusee`, `ReservationCreationTest.t04…` | T04 |
| RG-04 — Au plus deux équipements le même jour | FR-008, FR-028 | T020, T044 | `ReservationService.refuserSiLimiteDuJourAtteinte`, verrou d'écriture sur l'étudiant | `LimiteReservationsJourTest` (8 tests) | T05, T13, T14, T15 |
| RG-05 — Annulation de ses propres réservations | FR-009, FR-010 | T011, T028, T029 | `ReservationService.annuler` (contrôle du propriétaire), `EtudiantCourantService` | `ReservationServiceAnnulationTest.t08…`, `AnnulationNonProprietaireTest` (requête directe) | T06, T08 |
| RG-06 — Annulation jusqu'au jour réservé inclus | FR-011, FR-012 | T028 | `ReservationService.annuler` (`isBefore(aujourdHui())`) | `ReservationServiceAnnulationTest.annulationLeJourReserveInclus`, `annulationJourPasseRefusee` | T06, T11 |
| RG-07 — L'annulation conserve l'historique et libère l'équipement | FR-013, FR-014, FR-026 | T006, T028 | `Reservation.annulerParEtudiant` (statut conservé, `cleActive = null`) | `ReservationServiceAnnulationTest.t06…`, `t07…`, `ConflitConcurrentTest.deuxReservationsAnnuleesRestentPossibles` | T06, T07 |
| RG-08 — Persistance après redémarrage | FR-015 | T009, T034 | H2 en mode fichier, initialiseur idempotent | `DonneesDemoTest.initialiseurIdempotent`, `reservationSurvitALInitialiseur`, procédure manuelle T09 | T09 |
| RG-09 — Entrée invalide : message compréhensible, aucune modification | FR-016 à FR-022 | T010, T016, T020, T021, T025, T028, T029, T032 | `RegleMetierException`, `MessagesRefusService`, ordre contractuel des contrôles | `ReservationServiceReservationTest` (6 cas de refus), `ReservationListeTest.dateIllisibleAfficheUnMessage`, `ReservationCreationTest` | T10, T11 |

**Aucune règle n'est sans vérification.**

---

## 2. Exigences fonctionnelles FR-001 à FR-027

| Exigence | Tâche | Implémentation | Vérification |
|---|---|---|---|
| FR-001 — Proposer trois étudiants et en désigner un | T011, T012, T013 | `EtudiantController`, `etudiants/selection.html` | `EtudiantControllerTest.laPageProposeTroisEtudiants` |
| FR-002 — Mémoriser l'étudiant courant | T011 | `EtudiantCourantService.definir` (session HTTP) | `EtudiantControllerTest.choisirMemoriseEnSession`, `changerEtudiantRemplaceLeChoix` |
| FR-003 — Afficher les équipements et leur disponibilité | T016, T017 | `MaterielController`, `ReservationService.disponibilites` | `ReservationServiceDisponibiliteTest.sansReservationToutEstDisponible`, `ReservationListeTest.pageDesDisponibilitesAfficheCinqMateriels` |
| FR-004 — Indisponible si réservation active sur le couple | T015 | `ReservationService.estDisponible` | `ReservationServiceDisponibiliteTest.materielReserveEstIndisponible` |
| FR-005 — Réserver pour une journée | T020, T021 | `ReservationService.reserver` | `ReservationServiceReservationTest.t01ReservationValide`, `ReservationCreationTest.t01…` |
| FR-006 — Refus de la double réservation | T020 | `ReservationService.refuserSiDejaReserve` | `t02DoubleReservationRefusee`, `ReservationCreationTest.t02…` |
| FR-007 — Date égale ou postérieure seulement | T020 | `ReservationService.reserver` | `t04DatePasseeRefusee`, `reserverLeJourMemeEstAutorise` |
| FR-008 — Plusieurs réservations à la même date | T020 | `ReservationService.reserver` | `t05DeuxMaterielsLeMemeJour` |
| FR-009 — Consulter uniquement ses réservations | T024, T025 | `ReservationService.reservationsDe`, `ReservationController.lister` | `ReservationListeTest.chaqueEtudiantNeVoitQueSesReservations` |
| FR-010 — Refus si non-propriétaire | T028, T029, T031 | `ReservationService.annuler` | `t08AnnulationParUnAutreRefusee`, `AnnulationNonProprietaireTest` (**T08**) |
| FR-011 — Annulation acceptée jusqu'au jour inclus | T028 | `ReservationService.annuler` | `annulationLeJourReserveInclus` |
| FR-012 — Annulation refusée si jour passé | T028 | `ReservationService.annuler` | `annulationJourPasseRefusee` |
| FR-013 — Conserver l'enregistrement annulé | T006, T028 | `Reservation.annulerParEtudiant` | `t06AnnulationAutorisee`, `ReservationListeTest.reservationAnnuleeResteVisible` |
| FR-014 — Libérer l'équipement après annulation | T028 | Remise à `null` de `cleActive` | `t07ReservationPossibleApresAnnulation`, `apresAnnulationLeMaterielEstDisponible` |
| FR-015 — Conserver après redémarrage | T009, T034 | `application.properties` (H2 fichier), `DonneesDemoInitialiseur` | `DonneesDemoTest.initialiseurIdempotent`, `reservationSurvitALInitialiseur`, T09 manuel |
| FR-016 — Message compréhensible, aucune écriture | T010, T032 | `MessagesRefusService` | Tous les tests de refus (comptage des lignes) |
| FR-017 — Refus sans étudiant courant | T011, T020, T021, T025, T028 | `MotifRefus.ETUDIANT_NON_SELECTIONNE` | `sansEtudiantCourantRefuse`, `sansEtudiantCourantRefuse` (HTTP), `ReservationListeTest.sansEtudiantCourantInviteAChoisir` |
| FR-018 — Refus d'un matériel inconnu | T020 | `MotifRefus.MATERIEL_INCONNU` | `materielInconnuRefuse` (service et HTTP) |
| FR-019 — Refus d'une date absente ou mal formée | T019, T020, T016 | `ReservationForm`, `MaterielController` | `t10DateAbsenteRefusee`, `t10DateIllisibleRefusee`, `dateIllisibleAfficheUnMessage` |
| FR-020 — Refus d'une réservation inexistante | T028 | `MotifRefus.RESERVATION_INTROUVABLE` | `reservationInexistanteRefusee` (service et HTTP) |
| FR-021 — Au plus une réservation active sous concurrence | T006, T020, T033 | `cle_active` unique + `DataIntegrityViolationException` traduite | `ConflitConcurrentTest` (5 tests, dont concurrence réelle) |
| FR-022 — Message explicite si liste vide | T016, T025, T032 | `MessagesRefusService.listeMaterielsVide`, `listeReservationsVide` | `ReservationListeTest.listeVideProduitUnMessage` |
| FR-023 — Un identifiant par exemplaire physique | T004, T009 | `Materiel.code` unique | `DonneesDemoTest.chaqueExemplaireAUnCodeDistinct` |
| FR-024 — Date courante fixable | T008, T018 | `ClockConfiguration`, `Clock` injectée | `ReservationServiceDisponibiliteTest.dateCouranteVientDeLHorloge` |
| FR-025 — Message distinct si déjà réservé par soi-même | T020, T021 | `MotifRefus.DEJA_RESERVE_PAR_VOUS` | `dejaReserveParSoiMemeMotifDistinct` |
| FR-026 — Conserver l'origine de l'annulation | T005, T028 | `StatutReservation` (3 états) | `t06AnnulationAutorisee` (`ANNULEE_PAR_ETUDIANT`) |
| FR-027 — Ne jamais attribuer `ANNULEE_ADMINISTRATIVE` | T005, T035 | `Reservation.annulerParEtudiant` (aucun chemin administratif) | `ReservationServiceAnnulationTest.jamaisAnnuleeAdministrative` |
| FR-028 — Refus de la troisième réservation du jour | T044 | `ReservationService.refuserSiLimiteDuJourAtteinte` | `LimiteReservationsJourTest.t13…`, `t13DeuxDemandesConcurrentes…` |

**Aucune exigence n'est sans vérification.**

---

## 3. Scénarios de recette T01 à T12

| Scénario | Exigences couvertes | Test automatisé | Résultat |
|---|---|---|---|
| T01 | FR-005 | `ReservationServiceReservationTest.t01ReservationValide` | Conforme |
| T02 | FR-004, FR-006, FR-021 | `t02DoubleReservationRefusee`, `ReservationCreationTest.t02…` | Conforme |
| T03 | FR-004, FR-008 | `t03AutreDateAcceptee`, `reservationNAffectePasLesAutresDates` | Conforme |
| T04 | FR-007 | `t04DatePasseeRefusee`, `ReservationCreationTest.t04…` | Conforme |
| T05 | FR-005, FR-008 | `t05DeuxMaterielsLeMemeJour` | Conforme |
| T06 | FR-011, FR-013, FR-026 | `t06AnnulationAutorisee`, `AnnulationNonProprietaireTest.proprietairePeutAnnuler` | Conforme |
| T07 | FR-014 | `t07ReservationPossibleApresAnnulation` | Conforme |
| T08 | FR-010 | `t08AnnulationParUnAutreRefusee`, `AnnulationNonProprietaireTest` (requête directe) | Conforme |
| T09 | FR-015 | **Manuel** — `docs/compte-rendu-recette.md` §2 T09 | Conforme |
| T10 | FR-016, FR-019 | `t10DateAbsenteRefusee`, `t10DateIllisibleRefusee` | Conforme |
| T11 | FR-013, FR-016, FR-026 | `t11DoubleAnnulationSansEffet`, `doubleAnnulationSansEffet` | Conforme |
| T12 | FR-021 | `t12DeuxDemandesSuccessives`, `ConflitConcurrentTest.deuxDemandesConcurrentes` | Conforme |
| T13 | FR-008, FR-028 | `LimiteReservationsJourTest.t13TroisiemeReservationRefusee`, `t13DeuxDemandesConcurrentesNeDepassentPasLaLimite` | Conforme |
| T14 | FR-008, FR-014, FR-028 | `LimiteReservationsJourTest.t14ApresAnnulationReservationPossible` | Conforme |
| T15 | FR-008 | `LimiteReservationsJourTest.t15AutreJourToujoursPossible` | Conforme |

**Aucun scénario n'est orphelin.**

---

## 4. Cas limites CL-01 à CL-09

| Cas limite | Traitement | Vérification |
|---|---|---|
| CL-01 — Annulation déjà annulée | `MotifRefus.DEJA_ANNULEE`, aucune écriture | `t11DoubleAnnulationSansEffet` |
| CL-02 — Matériel inconnu | `MotifRefus.MATERIEL_INCONNU`, aucune écriture | `materielInconnuRefuse` |
| CL-03 — Date absente ou mal formée | `MotifRefus.DATE_INVALIDE` | `t10DateAbsenteRefusee`, `dateIllisibleAfficheUnMessage` |
| CL-04 — Annulation d'une réservation passée | `MotifRefus.JOUR_RESERVE_PASSE` | `annulationJourPasseRefusee` |
| CL-05 — Liste vide | `messageInfo` explicite, sans erreur technique | `listeVideProduitUnMessage` |
| CL-06 — Aucun étudiant courant | `MotifRefus.ETUDIANT_NON_SELECTIONNE` | `sansEtudiantCourantRefuse`, `sansEtudiantCourantInviteAChoisir` |
| CL-07 — Demandes simultanées | `cle_active` unique + `@Transactional` | `ConflitConcurrentTest.deuxDemandesConcurrentes` |
| CL-08 — Annulation forgée | Identité issue de la session uniquement | `AnnulationNonProprietaireTest.identifiantForgéNeContournePasLeRefus` |
| CL-09 — Même matériel, deux dates | Aucune contrainte sur la date seule | `memeMaterielDeuxDatesDifferentes` |
| CL-10 — Troisième réservation du jour sur un matériel disponible | `MotifRefus.LIMITE_RESERVATIONS_JOUR` | `t13TroisiemeReservationRefusee` |
| CL-11 — Limite atteinte **et** matériel déjà occupé | Indisponibilité annoncée en priorité | `indisponibiliteAvantLimite` |

---

## 5. Critères de succès SC-001 à SC-007

| Critère | Vérification | Résultat |
|---|---|---|
| SC-001 | `ReservationListeTest.pageDesDisponibilitesAfficheCinqMateriels` | Atteint |
| SC-002 | `t12DeuxDemandesSuccessives`, `ConflitConcurrentTest.laBaseRefuseDeuxReservationsActives` | Atteint |
| SC-003 | Procédure manuelle T09 | Atteint |
| SC-004 | Recette T01 à T12 (`docs/compte-rendu-recette.md`) | Atteint |
| SC-005 | Cette matrice (aucune ligne sans vérification) | Atteint |
| SC-006 | `t04…`, `t10…`, `t11…` : message + comptage des lignes | Atteint |
| SC-007 | `t06…` puis `t07…` | Atteint |
| SC-008 | `LimiteReservationsJourTest` (8 tests, dont concurrence) | Atteint |

---

## 6. Couverture des décisions de clarification

| Décision | Exigences produites | Vérification | Statut |
|---|---|---|---|
| CA-01 — Message distinct si déjà réservé par soi-même | FR-025 | `dejaReserveParSoiMemeMotifDistinct` (le motif diffère de `MATERIEL_INDISPONIBLE`) | Couverte |
| CA-02 — Distinguer l'origine de l'annulation | FR-026, FR-027 | `t06AnnulationAutorisee`, `jamaisAnnuleeAdministrative` | Couverte |
| CA-03 — Limite de deux réservations hors périmètre | — (exclusion) | Aucune exigence, aucune tâche, aucun test : conforme à la décision | Respectée |

---

## 7. Contrôle de complétude

| Contrôle | Résultat |
|---|---|
| Règles RG-01 à RG-09 sans vérification | **Aucune** |
| Exigences FR-001 à FR-028 sans tâche | **Aucune** |
| Exigences FR-001 à FR-028 sans vérification | **Aucune** |
| Scénarios T01 à T15 orphelins | **Aucun** |
| Cas limites CL-01 à CL-11 sans traitement | **Aucun** |
| Décisions CA-01 à CA-03 sans répercussion | **Aucune** |
| Fonctionnalité hors périmètre implémentée | **Aucune** (l'état `ANNULEE_ADMINISTRATIVE` est modélisé mais non atteignable, conformément à FR-027) |
