# Recherche technique : Réservation de matériel pédagogique

**Fonctionnalité** : `001-reservation-materiel` | **Date** : 2026-09-16

Ce document consigne les décisions techniques prises pour lever les points non couverts par la
spécification. Chaque décision indique ce qui a été retenu, pourquoi, et ce qui a été écarté.

---

## R-01 — Empêcher les doubles réservations sous concurrence

**Exigences concernées** : FR-004, FR-006, FR-021 · Cas limite CL-07 · Scénario T12 · RG-02

**Décision** : protection à **deux niveaux**.

1. **Contrôle applicatif** dans `ReservationService.reserver`, à l'intérieur d'une transaction :
   le service vérifie qu'aucune réservation active n'existe pour ce matériel et cette date. Ce
   contrôle produit les messages compréhensibles attendus par FR-006 et FR-025.
2. **Garantie déclarative en base** : une colonne technique `cle_active` porte la valeur
   `<identifiant du matériel>-<date>` lorsque la réservation est active, et `NULL` lorsqu'elle est
   annulée. Une contrainte d'unicité est posée sur cette colonne.

**Pourquoi cette clé et non une contrainte sur (matériel, date)** : une contrainte d'unicité
simple sur le couple (matériel, date) interdirait aussi d'enregistrer une seconde réservation
après une annulation, ce qui violerait RG-07 et ferait échouer le scénario T07. La colonne
`cle_active` résout le problème : les réservations annulées portent `NULL`, et un index unique
autorise plusieurs `NULL` (comportement SQL standard, appliqué par H2). Seule une réservation
**active** par couple (matériel, date) peut donc exister.

**Alternatives écartées** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Contrôle applicatif seul | Une lecture « aucune réservation » suivie d'une écriture n'est pas atomique : entre les deux, une transaction concurrente peut insérer sa propre réservation. Le scénario T12 échouerait avec deux demandes réellement simultanées. |
| Index unique partiel (`WHERE statut = 'ACTIVE'`) | H2 ne prend pas en charge les index partiels. |
| Contrainte d'unicité sur (matériel, date, statut) | Interdit deux réservations annulées pour le même couple, alors que RG-07 impose de conserver l'historique. |
| Verrou pessimiste sur la ligne du matériel | Fonctionnel, mais repose sur un ordre d'opérations délicat à expliquer et sur le niveau d'isolation. La garantie déclarative ne dépend pas de l'ordre des transactions. |
| Verrou optimiste (`@Version` sur la réservation) | Ne protège pas contre l'insertion de deux réservations distinctes : il n'y a pas de ligne commune à verrouiller. |

**Conséquences** :

- La colonne `cle_active` est mise à `NULL` lors de l'annulation, ce qui libère le matériel
  (RG-07, FR-014) et permet à une nouvelle réservation d'être acceptée (T07).
- Une violation de contrainte est traduite en erreur métier, jamais en erreur technique affichée
  (RG-09).
- La colonne est technique : elle est documentée dans `data-model.md` et n'est pas exposée à
  l'interface.

---

## R-02 — Rendre la date courante testable

**Exigences concernées** : FR-024 · RG-03, RG-06 · Scénarios T04, T06, T11

**Décision** : injecter une `java.time.Clock` dans les composants qui ont besoin de la date
courante, et n'obtenir la date que par `LocalDate.now(clock)`.

- `ClockConfiguration` expose un bean `Clock` en production : `Clock.systemDefaultZone()`.
- `ReservationService` reçoit cette horloge par son constructeur et ne fait jamais appel à
  `LocalDate.now()` sans argument.
- En test, une `Clock` fixe est fournie :
  `Clock.fixed(Instant.parse("2030-03-10T12:00:00Z"), ZoneOffset.UTC)`.

**Pourquoi** : la date de recette imposée par l'énoncé est le **10 mars 2030**. Sans horloge
injectable, les scénarios T04 (date passée) et T06 (annulation le jour réservé) changeraient de
résultat selon le jour où les tests sont exécutés, et deviendraient faux avec le temps.

**Alternatives écartées** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Appeler `LocalDate.now()` directement | Rend les règles RG-03 et RG-06 intestables de façon reproductible. Violerait le principe V de la constitution. |
| Modifier la date du système d'exploitation | Non reproductible, non portable, et inacceptable dans un test automatisé. |
| Modifier la règle pour accepter les dates passées en test | Revient à ne plus tester la règle. |

---

## R-03 — Distinguer les messages de refus d'une réservation

**Exigences concernées** : FR-006, FR-025 · Décision de clarification CA-01

**Décision** : porter un **code d'erreur métier** dans l'exception levée par le service, et
choisir le message à partir de ce code.

- `RegleMetierException` transporte un code (`MATERIEL_INDISPONIBLE`, `DEJA_RESERVE_PAR_VOUS`,
  `DATE_PASSEE`, `DATE_INVALIDE`, `MATERIEL_INCONNU`, `ETUDIANT_NON_SELECTIONNE`,
  `RESERVATION_INTROUVABLE`, `PAS_PROPRIETAIRE`, `JOUR_RESERVE_PASSE`, `DEJA_ANNULEE`).
- Le service détermine d'abord si l'étudiant courant est déjà propriétaire d'une réservation
  active pour ce couple (matériel, date) : dans ce cas, le code est `DEJA_RESERVE_PAR_VOUS` ;
  sinon, le code est `MATERIEL_INDISPONIBLE`.
- Un composant unique traduit le code en message français.

**Pourquoi** : comparer des chaînes de caractères de message serait fragile ; le code est stable
et testable. Les deux cas sont bien distincts du point de vue de l'utilisateur, comme demandé par
la décision CA-01, alors que le refus est identique (aucune écriture).

**Alternatives écartées** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Un message unique d'indisponibilité | C'était la proposition initiale de l'assistant. Refusée par le binôme : l'étudiant ne peut pas distinguer une erreur de sa part d'une vraie indisponibilité (décision CA-01). |
| Comparer les messages par leur texte dans les tests | Un test qui dépend de la formulation exacte casse à la moindre reformulation. Le test doit porter sur le code. |

---

## R-04 — Modéliser l'origine de l'annulation sans élargir le périmètre

**Exigences concernées** : FR-026, FR-027 · Décision de clarification CA-02

**Décision** : `StatutReservation` comporte trois valeurs — `ACTIVE`, `ANNULEE_PAR_ETUDIANT`,
`ANNULEE_ADMINISTRATIVE` —, mais **aucun traitement d'annulation administrative n'est
implémenté** : ni route, ni formulaire, ni méthode de service.

- Toute annulation produite par l'application attribue `ANNULEE_PAR_ETUDIANT`.
- `ANNULEE_ADMINISTRATIVE` n'est jamais assignée dans le code de production.
- Un test vérifie qu'après une annulation, l'état obtenu est `ANNULEE_PAR_ETUDIANT` et jamais
  `ANNULEE_ADMINISTRATIVE`.

**Pourquoi** : décision explicite du binôme (CA-02), consignée au journal de décisions. Le
principe VI de la constitution autorise un ajout hors périmètre sur décision consignée.

**Risque assumé** : il s'agit de code non atteignable. Le risque est borné par FR-027, qui
transforme la limite en exigence vérifiable, et par le test associé. Aucune route et aucun
formulaire ne sont ajoutés, ce qui serait une véritable extension de périmètre.

**Alternative écartée** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Deux états `ACTIVE` / `ANNULEE` (solution de référence de `copilot.md`) | Suffisait à toutes les règles RG-01 à RG-09, mais le binôme a tranché en faveur d'un modèle distinguant l'origine, décision consignée en CA-02. |

---

## R-05 — Initialiser les données fictives sans doublon

**Exigences concernées** : FR-015 · Hypothèse H-04 · RG-08 · Scénario T09

**Décision** : `DonneesDemoInitialiseur` s'exécute au démarrage et **vérifie l'existence** de
chaque étudiant et de chaque matériel avant de l'ajouter, en s'appuyant sur leur identifiant
métier (nom de l'étudiant, code du matériel).

- Si l'enregistrement existe déjà, rien n'est écrit.
- **Les réservations existantes ne sont jamais supprimées ni recréées.**
- L'initialiseur utilise les identifiants métier et non un compteur, afin de rester idempotent
  quel que soit l'ordre d'exécution.

**Pourquoi** : le scénario T09 exige qu'une réservation survive à un redémarrage. Un initialiseur
qui vide ou recrée les tables à chaque lancement ferait échouer T09 et violerait RG-08. Un
initialiseur qui ajoute sans vérifier créerait des doublons, donc des identifiants en double
contraires à FR-023.

**Alternatives écartées** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| `spring.jpa.hibernate.ddl-auto=create-drop` avec recréation des données | Efface les réservations au redémarrage : viole RG-08 et fait échouer T09. |
| Insertion inconditionnelle des données au démarrage | Crée des doublons à chaque lancement. |
| Script SQL d'initialisation exécuté systématiquement | Même problème de doublons, sauf à écrire des conditions dans le script. Le code Java porte mieux la règle et reste testable. |

---

## R-06 — Conserver l'identité simulée hors du service métier

**Exigences concernées** : FR-001, FR-002, FR-017 · Scénario T08 · Hypothèse H-03

**Décision** : l'identifiant de l'étudiant courant est stocké en **session HTTP** et lu par un
composant dédié, `EtudiantCourantService`.

- Le formulaire d'annulation ne transporte **jamais** l'identifiant du propriétaire.
- `ReservationService.reserver` et `ReservationService.annuler` reçoivent l'identifiant de
  l'étudiant courant en **paramètre explicite** et ne connaissent ni la session ni la requête.
- Si aucun étudiant courant n'est défini, les actions qui modifient les données sont refusées
  (FR-017).

**Pourquoi** : si un formulaire pouvait choisir le propriétaire, n'importe qui pourrait annuler
la réservation d'un autre en modifiant le HTML envoyé. Le scénario T08 l'exige explicitement :
le refus doit tenir même lorsque la requête est adressée directement au serveur. Par ailleurs,
un service métier qui ignore la session reste testable sans conteneur Web (principe II).

**Alternative écartée** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Lire la session directement dans `ReservationService` | Rendrait les tests de règles métier dépendants d'un contexte Web et mélangerait deux couches. |

---

## R-07 — Traduire les erreurs métier en messages sans exposer de détail technique

**Exigences concernées** : FR-016, FR-022 · RG-09 · Cas limites CL-01 à CL-05

**Décision** : le service lève `RegleMetierException` avec un code ; les contrôleurs l'attrapent,
placent le message français correspondant dans le modèle et réaffichent la page. Aucune trace
d'exécution, aucun nom de table ni message d'exception technique n'est affiché à l'utilisateur.

**Pourquoi** : RG-09 exige un message compréhensible et aucune modification des données ; le cas
limite CL-05 exige l'absence d'erreur technique. L'exception est levée **avant** toute écriture,
ce qui garantit la partie « aucune modification ».

**Alternative écartée** :

| Alternative | Pourquoi elle a été écartée |
|---|---|
| Laisser remonter l'exception jusqu'à la page d'erreur par défaut | Affiche une erreur technique et ne donne pas de message compréhensible : contraire à RG-09 et CL-05. |

---

## Points non retenus, volontairement

| Sujet | Pourquoi il n'est pas traité |
|---|---|
| Objectifs de performance chiffrés | Aucun n'est demandé ; l'usage est local et pédagogique. En inventer serait hors sujet. |
| Migrations de schéma versionnées (Flyway, Liquibase) | Ajouterait une dépendance non justifiée par le besoin. `ddl-auto=update` suffit au périmètre du TP ; le sujet est signalé comme approfondissement possible. |
| Pagination, recherche, tri des listes | Hors périmètre : 5 équipements et 3 étudiants. |
| Journalisation applicative structurée | Non demandée et sans usage dans un TP mono-utilisateur. |
| Sécurité réelle (authentification, autorisation) | Explicitement exclue du périmètre. L'identité est une simulation (H-03). |
