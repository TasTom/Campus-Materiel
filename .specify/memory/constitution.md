<!--
Sync Impact Report
==================
Version : (aucune) → 1.0.0
Type de changement : MAJOR — ratification initiale du document.
Principes ajoutés (tous) : I. Lisibilité ; II. Séparation des responsabilités ;
  III. Validation côté serveur ; IV. Traçabilité des règles métier ;
  V. Persistance et maîtrise du temps ; VI. Périmètre et dépendances maîtrisés.
Sections ajoutées : Contraintes techniques ; Workflow de développement ; Gouvernance.
Sections supprimées : aucune.
Éléments différés : aucun. Tous les jetons du template ont été remplacés.
Note de relecture : ce rapport est un commentaire temporaire, destiné à la relecture
par le binôme. Il peut être supprimé une fois la constitution validée.
-->

# Constitution du projet Campus Matériel

## Principes fondamentaux

### I. Lisibilité et explicabilité du code

Tout code livré **DOIT** pouvoir être expliqué ligne à ligne par un étudiant de BUT Informatique
2ᵉ année, sans recours à l'assistant.

- Les identifiants (classes, méthodes, variables) sont explicites et en français métier
  (`ReservationService`, `dateReservation`, `estActive`).
- Les constructions implicites et la génération de code sont interdites lorsqu'elles masquent le
  comportement : pas de Lombok, pas de méthode « magique ». Les constructeurs et les accesseurs
  réellement utilisés sont écrits explicitement.
- Chaque incrément livré doit pouvoir être expliqué par le membre du binôme qui l'a relu.

*Justification :* l'évaluation du TP porte sur la compréhension, pas sur la quantité de code
produit. Un code que l'on ne peut pas expliquer ne peut pas être défendu en recette.

### II. Séparation des responsabilités

L'application **DOIT** respecter trois couches aux rôles disjoints :

- les **contrôleurs** gèrent les échanges HTTP (routes, paramètres, session, redirections) ;
- les **services** portent les règles métier RG-01 à RG-09 ;
- les **repositories** gèrent l'accès aux données.

**Aucune règle métier ne peut être implémentée dans un contrôleur ou dans un template
Thymeleaf.** Un template uniquement destiné à l'affichage ne décide jamais de la validité d'une
opération.

*Justification :* une règle métier logée dans une vue est invérifiable par les tests et
contournable par une requête directe au serveur (voir scénario T08).

### III. Validation côté serveur

Toute entrée utilisateur **DOIT** être validée côté serveur, jamais uniquement dans le
formulaire HTML.

- Les contraintes Jakarta Validation couvrent les champs obligatoires, les formats et les
  bornes.
- Les règles qui dépendent du contexte (date passée, matériel inconnu, conflit de réservation)
  sont vérifiées dans les services, car elles dépendent de l'état des données.
- En cas d'entrée invalide, l'application produit un message compréhensible en français et
  **n'écrit aucune donnée** (RG-09).
- Masquer un bouton ou désactiver un attribut HTML ne constitue pas une protection.

*Justification :* un client peut toujours envoyer une requête forgée. Seule la validation côté
serveur garantit l'intégrité des données.

### IV. Traçabilité des règles métier

Chaque règle métier **DOIT** être associée à au moins une vérification exécutable.

- Chaque règle RG-xx de `docs/enonce.md` est couverte par au moins un test automatisé.
- Les scénarios de recette T01 à T08 et T10 à T12 sont automatisés ; T09 (persistance après
  redémarrage) fait l'objet d'une procédure manuelle reproductible et documentée.
- Une matrice de traçabilité relie exigence, tâche et test.
- **Aucun test ne peut être déclaré réussi sans avoir été exécuté**, et le résultat consigné
  indique la sortie réellement observée.

*Justification :* une règle non testée est une règle non vérifiée. L'affirmation d'un assistant
ou d'un relecteur ne remplace pas un test exécuté.

### V. Persistance et maîtrise du temps

- Les réservations **DOIVENT** être conservées après redémarrage de l'application (RG-08) :
  la base de développement est une base H2 en mode fichier, jamais une base en mémoire.
- Les tests **DOIVENT** utiliser une base en mémoire, isolée de la base locale, afin que
  chaque exécution reparte d'un état connu.
- La date courante **DOIT** être fournie par une `Clock` injectable
  (`LocalDate.now(clock)`), jamais par un appel direct à `LocalDate.now()`.
- Les tests fixent la date courante afin d'être reproductibles, indépendamment du jour
  d'exécution (date de recette : 10 mars 2030).
- Les données des étudiants et du matériel **DOIVENT** être fictives et réinitialisables sans
  perte de réservation lors d'un simple redémarrage.

*Justification :* sans horloge injectable, les scénarios portant sur les dates passées et
futures (RG-03, T04) dépendraient du jour où le test est lancé, et deviendraient faux avec le
temps.

### VI. Périmètre et dépendances maîtrisés

- **Aucune fonctionnalité hors périmètre** ne peut être ajoutée sans décision explicite et
  consignée du binôme. La liste des exclusions figure dans `docs/enonce.md`.
- La pile technique est imposée : Java 25, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA,
  H2 et Jakarta Validation. Elle n'est pas négociable dans ce TP.
- **Aucune dépendance nouvelle** ne peut être ajoutée sans justification écrite dans
  `docs/journal-decisions.md` (besoin couvert, alternative écartée, conséquence sur le périmètre).
- Le Maven Wrapper (`.\mvnw.cmd`) est utilisé exclusivement ; aucun Maven global.
- L'interface utilise des libellés français et des formulaires utilisables au clavier.

*Justification :* la demande d'évolution de l'étape 10 du TP montre qu'un ajout de règle mal
encadré produit des documents contradictoires. Le périmètre est un choix, pas un défaut.

## Contraintes techniques

| Élément | Valeur imposée |
|---|---|
| Langage et exécution | Java 25 |
| Framework | Spring Boot 4.1.1 |
| Vues | Thymeleaf, rendu côté serveur |
| Accès aux données | Spring Data JPA |
| Base de développement | H2 en mode fichier (`./data`) |
| Base de test | H2 en mémoire |
| Validation | Jakarta Validation |
| Build | Maven Wrapper (`.\mvnw.cmd`) |
| Tests | JUnit Jupiter et outils de test Spring |

Structure de paquets : `config`, `controller`, `domain`, `dto`, `repository`, `service`, sous
`com.campus.campusmateriel`.

L'identité de l'étudiant courant est une **simulation pédagogique** conservée en session HTTP.
Elle ne constitue pas une authentification et ne doit pas être présentée comme telle.

## Workflow de développement

- Le travail procède **par incréments** : chaque incrément est relu, lancé et testé avant le
  suivant. L'ordre prévu est : consultation du matériel, création d'une réservation,
  consultation des réservations personnelles, annulation, traitement complet des erreurs et
  vérification de la persistance.
- Les sources de vérité sont, dans l'ordre : `docs/enonce.md` et les documents Spec Kit. En cas
  de contradiction entre une conversation et ces documents, **les documents font foi** et la
  contradiction est signalée au binôme au lieu d'être tranchée silencieusement.
- Toute exigence découverte en cours de route **DOIT** d'abord être reportée dans les documents
  concernés (spécification, plan, tâches) avant toute modification du code. Une conversation
  n'est jamais le seul endroit où une décision existe.
- Les décisions importantes et les propositions de l'IA corrigées, refusées ou précisées sont
  consignées dans `docs/journal-decisions.md`.
- Les documents, les messages utilisateur et les commentaires de code sont rédigés **en
  français**.
- Les actions qui modifient les données utilisent `POST` et redirigent après succès, afin qu'un
  rafraîchissement ne rejoue pas l'opération.

## Gouvernance

- La présente constitution **prime** sur les habitudes et sur les propositions de l'assistant.
  En cas de conflit entre elle et une demande, la constitution est suivie et le conflit est
  signalé.
- **Amendement :** toute modification d'un principe exige l'accord explicite des deux membres du
  binôme, un motif écrit dans `docs/journal-decisions.md` et une mise à jour du numéro de
  version.
- **Versionnement :** la version suit SemVer. MAJOR pour un retrait ou une redéfinition
  incompatible d'un principe ; MINOR pour un principe ajouté ou une règle étendue ; PATCH pour
  une clarification rédactionnelle.
- **Contrôle de conformité :** chaque incrément livré est confronté à ces principes. Un
  manquement doit être corrigé ou explicitement justifié avant de passer à l'incrément suivant.
  L'étape `/speckit-analyze` sert de point de contrôle formel avant le développement.
- **Responsabilité :** tout ce qui est livré, y compris le code produit par l'assistant, engage
  le binôme.

**Version**: 1.0.0 | **Ratified**: 2026-09-16 | **Last Amended**: 2026-09-16
