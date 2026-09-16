# Compte rendu de recette — Campus Matériel

**Date d'exécution** : 16 septembre 2026
**Version évaluée** : commit `2f29171` (implémentation des règles RG-01 à RG-09)
**Environnement** : Java 25.0.4.1 (Microsoft OpenJDK), Spring Boot 4.1.1, H2 2.4.240, Windows 11
**Date courante de test** : 10 mars 2030, fixée par `ConfigurationHorlogeTest` (FR-024)

Ce compte rendu ne rapporte que des résultats **réellement observés**. Aucun test n'est déclaré
réussi sur la seule affirmation d'un outil ou d'un assistant.

---

## 1. Résultat de la suite automatisée

Commande exécutée : `.\mvnw.cmd test`

```
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Statut : conforme.** 67 tests, aucun échec.

| Classe de test | Tests | Statut |
|---|---:|---|
| `CampusMaterielApplicationTests` | 1 | Conforme |
| `ReservationServiceDisponibiliteTest` | 6 | Conforme |
| `ReservationServiceReservationTest` | 13 | Conforme |
| `ReservationServiceAnnulationTest` | 10 | Conforme |
| `EtudiantControllerTest` | 5 | Conforme |
| `ReservationCreationTest` | 9 | Conforme |
| `ReservationListeTest` | 8 | Conforme |
| `AnnulationNonProprietaireTest` | 6 | Conforme |
| `ConflitConcurrentTest` | 5 | Conforme |
| `DonneesDemoTest` | 4 | Conforme |

---

## 2. Résultats détaillés des scénarios T01 à T12

### T01 — Alice réserve MAT-001 pour le 12 mars

- **Mode** : automatisé (`ReservationServiceReservationTest.t01ReservationValide`) + interface
- **Résultat obtenu** : réservation créée, état `ACTIVE`, message « Réservation enregistrée pour le
  20 septembre 2026. » lors de la vérification manuelle.
- **Statut** : **conforme**
- **Anomalie** : aucune

### T02 — MAT-001 déjà réservé le 12 ; Bilal le demande pour le 12

- **Mode** : automatisé (`t02DoubleReservationRefusee`, `ReservationCreationTest.t02DoubleReservationRefusee`) + requête HTTP directe
- **Résultat obtenu** : refus, motif `MATERIEL_INDISPONIBLE`, message observé dans l'interface :
  « Ce matériel est déjà réservé à cette date. » Nombre de réservations actives sur le couple
  après le refus : **1** (aucun doublon).
- **Statut** : **conforme**
- **Anomalie** : aucune

### T03 — MAT-001 réservé le 12 ; Bilal le demande pour le 13

- **Mode** : automatisé (`t03AutreDateAcceptee`, `reservationNAffectePasLesAutresDates`)
- **Résultat obtenu** : réservation acceptée ; le lendemain, MAT-001 est de nouveau « Disponible ».
- **Statut** : **conforme**
- **Anomalie** : aucune

### T04 — Alice demande MAT-002 pour le 9 mars

- **Mode** : automatisé (`t04DatePasseeRefusee`) + requête HTTP directe avec la date `2020-01-01`
- **Résultat obtenu** : refus, motif `DATE_PASSEE`, message observé :
  « La date choisie est déjà passée. Choisissez aujourd'hui ou une date future. »
  Aucune ligne créée (`reservationRepository.count() == 0`).
- **Statut** : **conforme**
- **Anomalie** : aucune

### T05 — Alice réserve MAT-001 et MAT-002 pour le 12

- **Mode** : automatisé (`t05DeuxMaterielsLeMemeJour`)
- **Résultat obtenu** : deux réservations actives pour la même étudiante à la même date.
- **Statut** : **conforme**
- **Anomalie** : aucune

### T06 — Alice annule sa réservation du 12

- **Mode** : automatisé (`t06AnnulationAutorisee`) + interface
- **Résultat obtenu** : état `ANNULEE_PAR_ETUDIANT`, enregistrement **conservé** (compte de lignes
  inchangé), clé technique libérée. Message observé : « Réservation annulée. Le matériel est de
  nouveau disponible à cette date. » La réservation annulée reste affichée avec l'état
  « Annulée ».
- **Statut** : **conforme**
- **Anomalie** : aucune

### T07 — Après cette annulation, Bilal réserve le même matériel le 12

- **Mode** : automatisé (`t07ReservationPossibleApresAnnulation`) + interface
- **Résultat obtenu** : réservation acceptée ; MAT-001 repasse à « Disponible » puis « Réservé ».
- **Statut** : **conforme**
- **Anomalie** : aucune

### T08 — Bilal tente d'annuler une réservation d'Alice

- **Mode** : automatisé (`t08AnnulationParUnAutreRefusee`, `AnnulationNonProprietaireTest`) + **requête directe au serveur** via l'interface de développement du navigateur
- **Résultat obtenu** : refus, motif `PAS_PROPRIETAIRE`, message observé :
  « Vous ne pouvez annuler que vos propres réservations. » La réservation d'Alice est restée
  `ACTIVE` et sa clé technique intacte.
- **Vérification renforcée** : une seconde requête a été envoyée en ajoutant les paramètres
  `etudiantId` et `proprietaireId` forgés. Le refus a été identique : le propriétaire provient de
  la session et aucun paramètre client ne peut le remplacer.
- **Statut** : **conforme**
- **Anomalie** : aucune

### T09 — Une réservation existe ; l'application redémarre

- **Mode** : **manuel**, procédure reproductible (l'énoncé autorise une vérification manuelle)
- **Procédure suivie** :
  1. dossier `data` supprimé, application lancée ;
  2. Alice Martin sélectionnée ;
  3. MAT-003 réservé pour le 25 septembre 2026, puis MAT-001 réservé pour le 20 septembre et annulé ;
  4. application arrêtée (fin du processus Maven) ;
  5. application relancée ;
  6. Alice Martin re-sélectionnée (la session HTTP est perdue au redémarrage, ce qui est attendu) ;
  7. page « Mes réservations » consultée.
- **Résultat obtenu** :
  `MAT-003 — Vidéoprojecteur A — 25 septembre 2026 — Active`
  `MAT-001 — Ordinateur portable A — 20 septembre 2026 — Annulée`
  Les deux réservations sont présentes : la réservation active a survécu, et l'annulation a été
  conservée.
- **Contrôle complémentaire** : aucun doublon d'étudiant ni de matériel n'est apparu après le
  redémarrage (l'initialiseur est idempotent). La page des disponibilités affiche toujours
  exactement cinq matériels.
- **Statut** : **conforme**
- **Anomalie** : aucune

### T10 — La date est absente ou invalide

- **Mode** : automatisé (`t10DateAbsenteRefusee`, `t10DateIllisibleRefusee`, `dateIllisibleAfficheUnMessage`) + requête HTTP directe
- **Résultat obtenu** : refus avec message « La date saisie est invalide. Utilisez le format
  année-mois-jour. » Aucune ligne créée. **Aucune trace d'exécution ni nom de classe** n'apparaît
  dans la page renvoyée (vérifié par recherche de motif dans le HTML).
- **Statut** : **conforme**
- **Anomalie** : aucune

### T11 — Alice annule une réservation déjà annulée

- **Mode** : automatisé (`t11DoubleAnnulationSansEffet`, `doubleAnnulationSansEffet`)
- **Résultat obtenu** : refus, motif `DEJA_ANNULEE`, aucune modification : l'état reste
  `ANNULEE_PAR_ETUDIANT` et la clé technique reste nulle. Le nombre de lignes ne change pas.
- **Statut** : **conforme**
- **Anomalie** : aucune

### T12 — Deux demandes visent le même matériel et la même date

- **Mode** : automatisé, **deux demandes successives** et **deux demandes concurrentes**
- **Résultat obtenu** :
  - demandes successives : la seconde est refusée, une seule réservation active subsiste ;
  - demandes concurrentes (deux fils d'exécution libérés par un signal commun) : au plus une
    réservation active en base ;
  - écriture directe de deux réservations actives en contournant le service : la base **refuse**
    la seconde (`DataIntegrityViolationException`), ce qui prouve que la protection ne dépend pas
    du code applicatif seul.
- **Statut** : **conforme**
- **Anomalie** : aucune

---

## 3. Vérifications complémentaires menées dans l'interface

| Vérification | Résultat observé | Statut |
|---|---|---|
| Les cinq matériels s'affichent avec leur catégorie | MAT-001 à MAT-005, catégories « Informatique », « Projection », « Électronique » | Conforme |
| Les accents du jeu de données sont corrects | « Vidéoprojecteur A », « Électronique », « Chloé Bernard » | Conforme |
| Les dates s'affichent en français | « 20 septembre 2026 » | Conforme |
| Un matériel réservé n'expose pas de bouton de réservation | MAT-001 affiché « Réservé » avec « — » à la place du bouton | Conforme |
| Le nom d'un autre étudiant n'est jamais affiché | Bilal n'apparaît pas sur la page d'Alice (vérifié par recherche dans le HTML) | Conforme |
| Le bouton d'annulation disparaît quand l'annulation est impossible | Réservation passée et réservation annulée : aucune action proposée | Conforme |
| Aucune page d'administration n'existe | Aucune route ni lien d'administration | Conforme |

---

## 4. Anomalies rencontrées et corrigées pendant le développement

Ces défauts ont été **détectés par les tests**, et non après coup. Ils sont consignés parce qu'ils
montrent l'utilité de la vérification automatisée.

| # | Anomalie | Détection | Correction | Statut |
|---|---|---|---|---|
| A-1 | Expression de fragment Thymeleaf invalide : guillemets doubles imbriqués et apostrophe échappée, rendant la page de sélection inutilisable | `EtudiantControllerTest` (3 erreurs) | Titre transmis au fragment sans apostrophe, avec un commentaire expliquant la contrainte | Corrigée |
| A-2 | `LazyInitializationException` sur `reservation.materiel` lors du rendu de la liste | `ReservationListeTest` | Ajout d'un `@EntityGraph` sur la requête, `open-in-view` restant désactivé | Corrigée |
| A-3 | Erreur d'évaluation lorsque la liste des disponibilités est absente du modèle | `ReservationListeTest` | Vérification de nullité ajoutée dans le gabarit | Corrigée |
| A-4 | Le paquet `AutoConfigureMockMvc` a changé en Spring Boot 4 | Compilation des tests | Import corrigé vers `org.springframework.boot.webmvc.test.autoconfigure` | Corrigée |

**Anomalies non corrigées restantes : aucune.**

---

## 5. Couverture des critères de succès

| Référence | Critère | Résultat observé | Statut |
|---|---|---|---|
| SC-001 | Consulter les disponibilités d'une date en une action, sans saisie de texte | Sélection d'une date dans un champ `date`, affichage immédiat | Atteint |
| SC-002 | Jamais plus d'une réservation active par couple (matériel, date) | Vérifié par T02, T12 et le test de contrainte en base | Atteint |
| SC-003 | Les réservations survivent au redémarrage | Procédure T09 : 2 réservations présentes après redémarrage | Atteint |
| SC-004 | Les 12 scénarios produisent le résultat attendu | T01 à T12 : conformes | Atteint |
| SC-005 | Chaque exigence est couverte et tracée | Voir `docs/matrice-tracabilite.md` | Atteint |
| SC-006 | Toute saisie invalide produit un message compréhensible, sans écriture | T04, T10, T11 : messages conformes, aucune écriture | Atteint |
| SC-007 | Un équipement libéré est de nouveau réservable | T06 puis T07 : conforme | Atteint |

---

## 6. Limites connues de cette version

- **Aucune authentification.** L'étudiant courant est choisi librement. Les contrôles de
  propriété (FR-010) limitent les actions au nom de l'étudiant courant, mais ne protègent pas
  contre un utilisateur qui change volontairement d'identité. C'est une limite imposée par le
  périmètre du TP (hypothèse H-03).
- **L'état `ANNULEE_ADMINISTRATIVE` n'est jamais produit.** Il est défini dans le modèle sur
  décision CA-02 et son absence d'utilisation est garantie par un test (FR-027).
- **La limite de deux réservations actives par jour n'est pas implémentée.** Elle relève de
  l'étape d'évolution du TP.
- **La vérification T09 reste manuelle.** Un test automatisé qui redémarrerait réellement
  l'application n'a pas été mis en place ; l'énoncé l'autorise explicitement.

---

## 7. Conclusion

**Recette : conforme.** Les 12 scénarios produisent le résultat attendu, les 67 tests automatisés
passent, les 7 critères de succès sont atteints, et aucune anomalie ne reste ouverte.

Les quatre anomalies rencontrées ont toutes été détectées par les tests avant la recette, ce qui
confirme l'intérêt du principe IV de la constitution (« chaque règle métier possède une
vérification exécutable »).
