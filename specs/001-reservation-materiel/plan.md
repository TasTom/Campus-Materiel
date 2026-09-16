# Plan technique : Réservation de matériel pédagogique

**Fonctionnalité** : `001-reservation-materiel` | **Date** : 2026-09-16 | **Spécification** : [spec.md](./spec.md)

**Entrée** : spécification fonctionnelle de `specs/001-reservation-materiel/spec.md`

> Ce plan décrit **la manière de construire** ce que la spécification décrit. Les seuls choix
> techniques autorisés sont ceux de la constitution (section « Contraintes techniques »).

## Résumé

Construire une application Web locale en Java 25 et Spring Boot 4.1.1, avec des pages Thymeleaf
rendues côté serveur, une persistance H2 en mode fichier et des règles métier centralisées dans
un service unique.

L'approche technique repose sur quatre décisions structurantes :

1. **Les règles métier vivent dans `ReservationService`**, jamais dans les contrôleurs ni dans
   les templates. C'est le seul point du code qui décide si une réservation est acceptable.
2. **La date courante est fournie par une `Clock` injectable**, ce qui rend testables les règles
   sur les dates passées et futures sans dépendre du jour d'exécution.
3. **La protection contre les doubles réservations repose sur une contrainte de base de données**
   en plus de la vérification applicative, car une vérification seule est vulnérable aux
   demandes simultanées.
4. **L'identité de l'étudiant courant est portée par la session HTTP**, ce qui évite qu'un
   formulaire puisse désigner arbitrairement le propriétaire d'une annulation.

## Contexte technique

**Langage et version** : Java 25

**Dépendances principales** : Spring Boot 4.1.1 (Spring Web MVC, Thymeleaf, Spring Data JPA,
Jakarta Validation, H2, Spring Boot H2 Console). Aucune dépendance supplémentaire n'est ajoutée :
les besoins de sélection de données, de validation et de rendu sont couverts par ces briques.

**Stockage** : H2 en mode fichier (`jdbc:h2:file:./data/campus-materiel`) pour l'application ;
H2 en mémoire pour les tests. Le dossier `data/` est exclu du dépôt Git.

**Tests** : JUnit Jupiter et les outils de test Spring fournis avec le `pom.xml`
(`spring-boot-starter-*-test`), plus les tests d'intégration HTTP sur les routes.

**Plateforme cible** : application locale, un seul poste, accessible uniquement sur
`127.0.0.1:8080`. Aucun déploiement distant.

**Type de projet** : application Web de type « serveur + vues rendues côté serveur »
(single project, un seul artefact Maven).

**Objectifs de performance** : hors périmètre. L'énoncé indique un usage pédagogique local à
faible charge. **Aucun objectif chiffré de débit ou de latence n'est retenu** : en fixer un
serait une invention non demandée.

**Contraintes** :

- fonctionnement entièrement local, sans accès réseau externe ;
- données exclusivement fictives ;
- code lisible et explicable, sans Lombok ni génération de code implicite (principe I) ;
- Maven Wrapper exclusivement (principe VI) ;
- messages utilisateur en français.

**Échelle et périmètre** : 3 étudiants fictifs, 5 équipements, 4 pages, 6 routes, 3 entités.
Application mono-utilisateur.

## Contrôle de constitution

*Point de passage : doit réussir avant la phase 0, puis être revérifié après la phase 1.*

| Principe | Comment le plan le respecte | Statut |
|---|---|---|
| I. Lisibilité du code | Aucune dépendance de génération de code ; entités, service et contrôleurs écrits explicitement ; noms métier français | Conforme |
| II. Séparation des responsabilités | Paquets `controller` / `service` / `repository` / `domain` / `dto` / `config` ; aucune règle métier dans un contrôleur ou un template | Conforme |
| III. Validation côté serveur | Contraintes Jakarta Validation sur le formulaire **et** contrôles contextuels dans `ReservationService` ; l'annulation ne prend jamais le propriétaire depuis le formulaire | Conforme |
| IV. Traçabilité des règles métier | Chaque FR-xxx est couverte par au moins un test ; la matrice de traçabilité relie RG, FR, tâche et test | Conforme |
| V. Persistance et maîtrise du temps | H2 fichier pour l'application, H2 mémoire pour les tests, `Clock` injectable, date de recette fixée au 10 mars 2030 | Conforme |
| VI. Périmètre et dépendances maîtrisés | Aucune dépendance ajoutée ; aucune fonctionnalité hors périmètre ; l'état « annulation administrative » est modélisé mais **aucun traitement n'est implémenté** | Conforme, sous réserve du suivi ci-dessous |

### Point de suivi issu du contrôle

L'état `ANNULEE_ADMINISTRATIVE` (décision de clarification CA-02) est une donnée **non
atteignable** par l'application. Le plan le borne explicitement :

- aucune route, aucun formulaire, aucune méthode de service d'annulation administrative ;
- l'exigence FR-027 est vérifiée par un test qui garantit que l'application ne produit jamais cet
  état ;
- ce point est consigné dans « Suivi de la complexité » ci-dessous.

Aucune autre violation n'est à justifier : le plan n'introduit pas de couche, de patron de
conception ni de dépendance supplémentaire.

## Structure du projet

### Documentation de la fonctionnalité

```text
specs/001-reservation-materiel/
├── plan.md              # Ce fichier
├── spec.md              # Spécification fonctionnelle (clarifiée)
├── research.md          # Phase 0 : décisions techniques et alternatives écartées
├── data-model.md        # Phase 1 : schéma de données
├── quickstart.md        # Phase 1 : guide de validation exécutable
├── contracts/           # Phase 1 : contrat des routes et des messages
├── checklists/
│   └── requirements.md  # Checklist qualité des exigences
└── tasks.md             # Phase 2 : tâches (produit par /speckit-tasks)
```

### Code source

```text
src/main/java/com/campus/campusmateriel/
├── CampusMaterielApplication.java
├── config/
│   ├── ClockConfiguration.java        # Fournit la Clock système (remplaçable en test)
│   └── DonneesDemoInitialiseur.java   # Jeu de données fictif, sans doublon au redémarrage
├── controller/
│   ├── EtudiantController.java        # Sélection de l'étudiant courant (session)
│   ├── MaterielController.java        # Consultation des disponibilités
│   └── ReservationController.java     # Création, consultation, annulation
├── domain/
│   ├── Etudiant.java
│   ├── Materiel.java
│   ├── Reservation.java
│   └── StatutReservation.java         # ACTIVE, ANNULEE_PAR_ETUDIANT, ANNULEE_ADMINISTRATIVE
├── dto/
│   └── ReservationForm.java           # Formulaire de réservation validé
├── repository/
│   ├── EtudiantRepository.java
│   ├── MaterielRepository.java
│   └── ReservationRepository.java
├── service/
│   ├── ReservationService.java        # Règles RG-01 à RG-09
│   └── EtudiantCourantService.java    # Lecture/écriture de l'étudiant en session
└── session/
    └── CleSession.java                # Constante de la clé de session

src/main/resources/
├── application.properties
├── templates/                         # Pages Thymeleaf
└── static/                            # Feuille de style

src/test/java/com/campus/campusmateriel/
├── service/                           # Tests des règles métier (RG-01 à RG-09)
├── controller/                        # Tests des routes, y compris requêtes forgées (T08)
└── persistence/                       # Test de persistance (T09)
```

**Décision de structure** : un seul projet Maven, une seule application Spring Boot. Il n'y a ni
module `frontend` séparé (les vues sont rendues côté serveur) ni module `api` séparé (il n'y a
pas de client distinct). La séparation se fait par paquet, conformément à la constitution.

## Suivi de la complexité

| Élément | Pourquoi il est nécessaire | Alternative plus simple écartée parce que |
|---|---|---|
| État `ANNULEE_ADMINISTRATIVE` non atteignable (décision CA-02) | Décision explicite du binôme : le modèle d'état doit distinguer l'origine de l'annulation pour ne pas devoir être modifié plus tard. Le principe VI autorise un ajout hors périmètre sur décision consignée. | Un modèle à deux états (`ACTIVE`, `ANNULEE`) aurait suffi à toutes les règles RG-01 à RG-09 et aurait évité du code non atteignable. Le binôme assume ce choix, borné par FR-027 et couvert par un test. |
| Contrainte d'unicité en base sur (matériel, date, réservation active) | Une vérification applicative seule ne protège pas contre deux demandes simultanées (CL-07, T12). | S'en remettre au seul contrôle applicatif laisserait passer des doublons sous concurrence, ce que l'énoncé cherche précisément à éliminer. |
| `EtudiantCourantService` distinct du `ReservationService` | La lecture de l'étudiant courant depuis la session concerne la couche Web, pas les règles métier. | Mélanger la session HTTP dans `ReservationService` rendrait les tests de règles métier dépendants d'un contexte Web. |

## Phase 0 — Recherche

Voir [research.md](./research.md). Les points suivants ont été tranchés :

1. empêcher les doubles réservations sous concurrence (CL-07, T12) ;
2. rendre la date courante testable (FR-024) ;
3. distinguer les messages de refus (FR-006 et FR-025) ;
4. conserver l'annulation administrative sans élargir le périmètre (CA-02, FR-027) ;
5. initialiser les données fictives sans doublon au redémarrage (H-04, FR-015).

## Phase 1 — Conception

- **Modèle de données** : [data-model.md](./data-model.md)
- **Contrat des routes et des messages** : [contracts/routes.md](./contracts/routes.md)
- **Guide de validation** : [quickstart.md](./quickstart.md)

### Pages et actions

| Page | Route | Action |
|---|---|---|
| Sélection de l'étudiant | `GET /etudiants/selection` | Afficher les trois étudiants fictifs et l'étudiant courant |
| Sélection de l'étudiant | `POST /etudiants/selection` | Mémoriser l'étudiant courant, puis rediriger |
| Disponibilités | `GET /materiels?date=2030-03-12` | Afficher les cinq équipements et leur disponibilité à cette date |
| Réservation | `POST /reservations` | Créer une réservation pour l'étudiant courant |
| Mes réservations | `GET /reservations` | Afficher les réservations de l'étudiant courant |
| Annulation | `POST /reservations/{id}/annulation` | Annuler une réservation de l'étudiant courant |

Toute action qui modifie les données utilise `POST` puis redirige (`POST` → redirection → `GET`)
afin qu'un rafraîchissement du navigateur ne rejoue pas l'opération.

### Emplacement des règles métier

Toutes les règles sont implémentées dans `ReservationService`, à un seul endroit :

| Règle | Méthode porteuse | Contrôle effectué |
|---|---|---|
| RG-01 | `reserver` | Un étudiant, un matériel et une date non nuls sont réunis |
| RG-02 | `reserver` | Aucune autre réservation active pour ce matériel à cette date |
| RG-03 | `reserver` | `dateReservation` n'est pas antérieure à `LocalDate.now(clock)` |
| RG-04 | `reserver` | Aucune limite au nombre de réservations d'un étudiant pour un jour |
| RG-05 | `annuler` | L'étudiant passé en paramètre est le propriétaire de la réservation |
| RG-06 | `annuler` | `LocalDate.now(clock)` n'est pas postérieure à la date réservée |
| RG-07 | `annuler` | L'enregistrement est conservé et son état devient annulé |
| RG-08 | — | Assurée par la configuration de H2 en mode fichier |
| RG-09 | `reserver`, `annuler` | Toute entrée invalide produit une erreur métier, sans écriture |

Le service ne connaît ni `HttpServletRequest` ni la session : il reçoit l'identifiant de
l'étudiant courant en paramètre explicite. Il est donc testable sans conteneur Web.

### Mécanisme empêchant une double réservation

Le mécanisme est **double**, car un contrôle unique est insuffisant :

1. **Contrôle applicatif dans `ReservationService.reserver`** : avant l'enregistrement, le
   service interroge le repository pour savoir si une réservation active existe déjà pour le
   même matériel et la même date. Si c'est le cas, il lève une erreur métier et n'écrit rien.
   Ce contrôle produit le message compréhensible attendu par RG-09 et distingue, pour FR-025, le
   cas où l'étudiant courant est déjà propriétaire de cette réservation.
2. **Contrainte de base de données** : une contrainte d'unicité sur (matériel, date) pour les
   seules réservations actives garantit qu'aucune seconde ligne active ne peut être écrite, même
   si deux demandes arrivent au même instant et passent toutes les deux le contrôle applicatif.

**Pourquoi le contrôle applicatif seul ne suffit pas :** entre le moment où le service lit
« aucune réservation n'existe » et le moment où il écrit, une autre transaction peut insérer sa
propre réservation. C'est un cas de concurrence classique. Le scénario T12 le met en évidence :
deux demandes successives sont arrêtées par le contrôle applicatif, mais deux demandes
réellement simultanées ne le seraient pas. La contrainte de base est l'arbitre final : la
seconde transaction échoue, l'erreur est traduite en message métier et aucune donnée
incohérente n'est enregistrée.

**Pourquoi le contrôle uniquement dans le formulaire ne suffit pas :** masquer une option ou
désactiver un bouton dans la page ne change rien à ce que le serveur accepte. Une requête peut
être adressée directement au serveur en contournant la page. C'est exactement ce que vérifie le
scénario T08 pour l'annulation : le contrôle doit être refait côté serveur, à chaque requête.

### Traduction des erreurs en messages

Les erreurs métier sont portées par une exception dédiée, `RegleMetierException`, qui transporte
un message en français destiné à l'utilisateur. Le contrôleur l'attrape, place le message dans le
modèle et réaffiche la page. Message et statut HTTP sont documentés dans
[contracts/routes.md](./contracts/routes.md).

## Revue post-conception

Après la phase 1, le contrôle de constitution est revérifié :

- le modèle de données ne comporte aucune entité hors périmètre ;
- les routes correspondent exactement aux quatre pages de la spécification ;
- chaque FR-001 à FR-027 est rattachée à une méthode du service ou à une route du contrat ;
- aucun contrôle de règles métier ne figure dans un contrôleur ou un template ;
- la contrainte de base empêchant les doublons est décrite dans le modèle de données.

**Résultat : conforme.** Aucun écart nécessitant une justification supplémentaire.

