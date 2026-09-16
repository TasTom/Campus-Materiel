# TP Développer une application avec GitHub Spec Kit

**Projet : Campus Matériel — Réserver du matériel pédagogique**

| Élément | Organisation proposée |
|---|---|
| Public | BUT Informatique, deuxième année |
| Durée | 12 heures : quatre séances de 3 heures |
| Organisation | Binômes, avec bilan individuel |
| Prérequis | Programmation, bases du Web, SQL, Git et tests unitaires |
| Production finale | Application locale, spécifications, tests et démonstration |
| Outils | GitHub Spec Kit, GitHub Copilot fourni par l’établissement, Git |

## 1. Objectifs pédagogiques

À la fin de ce TP, vous devrez être capables de :

- transformer un besoin exprimé en langage naturel en exigences vérifiables ;
- distinguer les besoins des utilisateurs des choix techniques ;
- construire un plan de réalisation et des tâches cohérentes ;
- utiliser un assistant d’IA en contrôlant ses propositions ;
- vérifier une application à partir de critères d’acceptation ;
- faire évoluer ensemble les spécifications, le code et les tests.

Ce travail mobilise l’analyse des besoins, le développement, la gestion des données, la conduite de projet et le travail en équipe.

**Votre responsabilité porte sur tout ce que vous livrez, y compris le code généré par l’IA.**

## 2. Comprendre la démarche

Dans l’introduction de l’article proposé, David C. présente les difficultés d’un développement guidé par des demandes successives à une IA : réécritures importantes et décisions conservées uniquement dans les conversations. Cette présentation servira de point de départ à votre réflexion ; son titre constitue une prise de position à discuter. [Article Medium](https://medium.com/@david.j.sea/github-spec-kit-la-r%C3%A9volution-silencieuse-qui-enterre-le-vibe-coding-ac29672364bd).

Le développement piloté par les spécifications, ou *Spec-Driven Development*, consiste à formaliser ce qui doit être construit avant de détailler sa réalisation. Spec Kit organise ce travail en étapes et en documents que l’assistant peut exploiter. [Présentation officielle de la démarche](https://github.github.io/spec-kit/concepts/sdd.html).

Dans ce TP, vous suivrez le parcours suivant :

**Besoin → Spécification → Clarification → Plan → Tâches → Réalisation → Vérification**

Des retours en arrière sont possibles : découvrir une ambiguïté doit conduire à corriger les documents concernés.

**Question de départ :** une application qui démarre et affiche une interface répond-elle nécessairement au besoin de son utilisateur ? Justifiez en cinq lignes.

## 3. Votre mission : Campus Matériel

Le département informatique prête du matériel aux étudiants : ordinateurs portables, vidéoprojecteurs et kits électroniques.

Les réservations sont actuellement inscrites dans un tableau partagé. Des doublons apparaissent et certaines annulations ne sont pas prises en compte.

Vous devez développer une application permettant de :

1. consulter le matériel disponible ;
2. réserver un équipement pour une journée ;
3. consulter les réservations d’un étudiant ;
4. annuler une réservation ;
5. empêcher les doubles réservations.

### Périmètre de la première version

L’application utilise trois étudiants fictifs préenregistrés. Une liste permet de sélectionner l’étudiant courant.

**Cette sélection simule une identité pour le TP : elle ne constitue pas une authentification.** L’application doit fonctionner localement avec des données fictives.

Chaque équipement représente un objet physique unique. Deux ordinateurs identiques doivent donc avoir deux identifiants différents.

Sont exclus de cette version :

- les comptes et mots de passe ;
- les courriels et notifications ;
- la gestion des retards et des pénalités ;
- les réservations sur plusieurs jours ;
- les réservations par créneau horaire ;
- l’ajout ou la suppression de matériel par une interface d’administration.

### Jeu de données initial

| Identifiant | Équipement | Catégorie |
|---|---|---|
| MAT-001 | Ordinateur portable A | Informatique |
| MAT-002 | Ordinateur portable B | Informatique |
| MAT-003 | Vidéoprojecteur A | Projection |
| MAT-004 | Kit Arduino A | Électronique |
| MAT-005 | Kit Arduino B | Électronique |

Étudiants fictifs : **Alice Martin**, **Bilal Dupont** et **Chloé Bernard**.

### Règles métier obligatoires

| Référence | Règle |
|---|---|
| RG-01 | Une réservation concerne un étudiant, un équipement et une date. |
| RG-02 | Un équipement ne peut avoir qu’une réservation active pour une même date. |
| RG-03 | Une réservation peut concerner aujourd’hui ou une date future, jamais une date passée. |
| RG-04 | Un étudiant peut réserver plusieurs équipements pour une même journée. |
| RG-05 | L’étudiant courant ne peut annuler que ses propres réservations. |
| RG-06 | Une réservation active peut être annulée jusqu’au jour réservé inclus. |
| RG-07 | Une annulation conserve l’historique et libère l’équipement pour la date concernée. |
| RG-08 | Les réservations sont conservées après redémarrage de l’application. |
| RG-09 | Une entrée invalide entraîne un message compréhensible et aucune modification des données. |

Dans ce TP, « aujourd’hui » désigne la date du serveur local. Pour les tests automatiques, cette date devra pouvoir être fixée artificiellement.

## 4. Organisation du travail

| Séance | Travail principal | Résultat attendu |
|---|---|---|
| 1 — 3 h | Analyse, installation, principes et spécification | Besoin clarifié et critères d’acceptation |
| 2 — 3 h | Conception, checklist, tâches et analyse | Dossier prêt pour l’implémentation |
| 3 — 3 h | Développement progressif et tests | Première version fonctionnelle |
| 4 — 3 h | Recette, évolution et restitution | Livraison vérifiée et bilan critique |

Alternez les rôles à chaque étape :

- **pilote** : conduit les manipulations et dialogue avec l’assistant ;
- **relecteur** : vérifie les documents, le code et les résultats.

Tenez un journal contenant les décisions importantes et au moins trois propositions de l’IA que vous avez corrigées, refusées ou précisées.

## 5. Étape 1 — Examiner le besoin avant d’utiliser l’IA

**Travail sans assistant : 20 minutes.**

À partir du contexte, rédigez :

1. une présentation du problème en cinq lignes ;
2. trois besoins utilisateurs ;
3. cinq questions qu’un développeur poserait au département ;
4. deux risques de mauvaise interprétation.

Exemple de besoin :

> En tant qu’étudiante, je souhaite voir les équipements disponibles à une date donnée afin de choisir un matériel que je pourrai effectivement emprunter.

Exemples de questions :

- La réservation porte-t-elle sur une journée ou sur des heures ?
- Que se passe-t-il après une annulation ?
- Deux étudiants peuvent-ils réserver simultanément le même équipement ?

Repérez les réponses déjà présentes dans les règles métier. Signalez les questions restantes sans inventer silencieusement une réponse.

**Livrable :** une note d’analyse initiale d’une page maximum.

## 6. Étape 2 — Préparer l’environnement

L’enseignant fournit un accès à GitHub Copilot pour tous les étudiants et vérifie avant la séance les installations de Python 3.11 ou supérieur, `uv`, Git et de l’environnement de développement.

Dans un terminal :

```bash
uv tool install specify-cli
specify version
specify init campus-materiel --integration copilot
cd campus-materiel
```

L’option `--integration copilot` configure Spec Kit pour GitHub Copilot. Notez la version installée dans votre README. Pour une promotion entière, l’enseignant fixe préalablement une version commune. [Guide officiel d’installation](https://github.github.io/spec-kit/installation.html).

Ouvrez ensuite le dossier dans votre environnement de développement, connectez GitHub Copilot au compte fourni par l’établissement et vérifiez que les commandes Spec Kit sont accessibles dans GitHub Copilot.

**Deux espaces de commande sont utilisés :**

- `specify ...` s’exécute dans le terminal ;
- les commandes ci-dessous s’utilisent dans GitHub Copilot.

L’énoncé emploie la notation `/speckit.nom` pour les commandes Spec Kit dans GitHub Copilot. [Guide de démarrage](https://github.github.io/spec-kit/quickstart.html).

Vérifiez également si le dossier est déjà un dépôt Git. Sinon, initialisez-le. Avant le premier commit, configurez les exclusions pour l’environnement Python, les fichiers temporaires et la base locale.

**Validation :** chaque membre du binôme sait retrouver le projet, ouvrir GitHub Copilot et expliquer la différence entre terminal et commande d’agent.

## 7. Étape 3 — Définir les principes du projet

La « constitution » fixe les principes qui guideront le projet. [Référence des commandes](https://github.github.io/spec-kit/reference/agentic-sdd.html).

Soumettez cette instruction à GitHub Copilot :

```text
/speckit.constitution

Établis les principes du projet Campus Matériel :

- Le code doit être compréhensible par des étudiants de BUT Informatique.
- Chaque règle métier doit être associée à une vérification.
- Les entrées doivent être validées côté serveur.
- Les données doivent persister après redémarrage.
- L'interface doit utiliser des libellés français et des formulaires
  utilisables au clavier.
- Les données utilisées sont exclusivement fictives.
- Les dépendances doivent être limitées et justifiées.
- Aucun ajout fonctionnel hors périmètre ne doit être réalisé
  sans décision explicite du binôme.
```

Relisez le résultat et corrigez les principes trop vagues.

Par exemple, « l’application doit être de qualité » ne permet pas de déterminer si le principe est respecté.

**À produire :** les principes validés et deux exemples de décisions qu’ils pourront orienter.

## 8. Étape 4 — Rédiger la spécification fonctionnelle

Utilisez la commande suivante en joignant le contexte, le périmètre, les données et les règles métier de la section 3 :

```text
/speckit.specify

Décris la fonctionnalité de réservation de matériel de Campus Matériel
à partir de l'énoncé joint.

Organise les besoins en histoires utilisateur priorisées.
Attribue un identifiant aux exigences.
Définis des critères d'acceptation observables.
Couvre les cas normaux et les principaux cas d'erreur.
Signale les ambiguïtés et les hypothèses.

N'introduis pas de fonctionnalité supplémentaire.
Ne choisis pas encore de technologie.
```

À cette étape, vous décrivez le comportement attendu. Les choix techniques interviendront dans le plan. [Guide de démarrage](https://github.github.io/spec-kit/quickstart.html).

### Exemple concret de critère d’acceptation

**CA-02 — Refus d’une double réservation**

- **Étant donné** que MAT-003 possède une réservation active pour Alice le 12 mars 2030 ;
- **et** que la date courante de test est le 10 mars 2030 ;
- **quand** Bilal demande MAT-003 pour le 12 mars 2030 ;
- **alors** sa demande est refusée avec un message indiquant l’indisponibilité ;
- **et** aucune réservation supplémentaire n’est enregistrée.

Rédigez au moins **huit critères d’acceptation**, couvrant notamment :

- une réservation autorisée ;
- une date passée ;
- une double réservation ;
- une annulation autorisée ;
- une tentative d’annulation par un autre étudiant ;
- la disponibilité après annulation ;
- la conservation après redémarrage ;
- une entrée invalide.

**Point de contrôle :** un autre binôme doit pouvoir comprendre comment vérifier chaque critère sans vous demander d’explication.

## 9. Étape 5 — Clarifier les ambiguïtés

```text
/speckit.clarify

Examine en priorité les dates, les annulations, les conflits de réservation
et les entrées invalides. Confronte les questions aux règles de l'énoncé.
N'invente pas de nouvelles règles métier.
```

Consignez les décisions prises. Pour les situations suivantes, retenez les réponses imposées :

| Situation | Décision attendue |
|---|---|
| Nouvelle annulation d’une réservation déjà annulée | Aucune modification ; message indiquant son état |
| Réservation d’un matériel inconnu | Refus ; aucune création |
| Date absente ou mal formée | Refus avec explication |
| Annulation d’une réservation passée | Refus |
| Liste sans résultat | Message explicite, sans erreur technique |

Relisez ensuite les critères d’acceptation : sont-ils toujours cohérents ?

**Livrable :** spécification corrigée et tableau des décisions.

**Jalon 1 :** faites valider la spécification par l’enseignant avant de poursuivre.

## 10. Étape 6 — Construire le plan technique

Pour ce TP, la pile technique imposée est :

- Python et Flask ;
- SQLite ;
- HTML et CSS avec rendu côté serveur ;
- pytest pour les tests.

Ce choix pédagogique vise à limiter la complexité d’installation et à rendre visibles les règles métier.

```text
/speckit.plan

Prépare un plan de réalisation utilisant Python, Flask, SQLite,
des pages HTML rendues côté serveur et pytest.

Prévois :
- une séparation entre interface, règles métier et accès aux données ;
- un jeu de données fictives réinitialisable ;
- une validation côté serveur ;
- une protection contre les doubles réservations ;
- des tests pour les règles métier et les principales routes ;
- une date courante remplaçable dans les tests ;
- des instructions reproductibles d'installation et de lancement.

L'application fonctionne localement.
Respecte le périmètre et les décisions validées.
```

### Travail de conception à réaliser

Produisez et expliquez :

1. un schéma de données ;
2. les principales pages et actions ;
3. l’emplacement des règles métier ;
4. le mécanisme empêchant une double réservation.

Votre modèle comportera au minimum :

| Entité | Informations minimales |
|---|---|
| Étudiant | Identifiant, nom |
| Matériel | Identifiant, nom, catégorie |
| Réservation | Identifiant, étudiant, matériel, date, état |

**Question technique :** pourquoi vérifier uniquement la disponibilité dans le formulaire ne suffit-il pas ?

Expliquez comment votre conception évite qu’une seconde demande crée un doublon après l’affichage de la page.

**Livrable :** plan relu, schéma de données et justification des choix essentiels.

## 11. Étape 7 — Vérifier les exigences et préparer les tâches

Commencez par examiner la qualité des exigences :

```text
/speckit.checklist

Vérifie la précision des exigences relatives aux dates,
aux autorisations d'annulation, aux conflits et aux messages d'erreur.
```

Cette checklist porte sur la qualité des exigences. Elle ne remplace pas les tests de l’application. Relisez vous-mêmes les éléments avant de les déclarer satisfaits. [Référence des commandes](https://github.github.io/spec-kit/reference/agentic-sdd.html).

Générez ensuite les tâches :

```text
/speckit.tasks

Inclure explicitement les tests prévus au plan.
Associer les tâches aux exigences et préciser leurs dépendances.
```

Pour chaque tâche, vérifiez qu’il est possible de répondre à trois questions :

- Quel résultat concret doit être obtenu ?
- De quoi dépend sa réalisation ?
- Comment vérifier qu’elle est terminée ?

Exemple de tâche correctement délimitée :

> Implémenter l’annulation d’une réservation appartenant à l’étudiant courant. Vérifier le propriétaire, la date et l’état ; conserver l’enregistrement ; tester les cas autorisés et refusés.

Lancez enfin :

```text
/speckit.analyze
```

Cette analyse recherche les incohérences entre spécification, plan et tâches. Elle ne corrige pas directement les fichiers : vous devez traiter les problèmes signalés puis relancer l’analyse. [Référence des commandes](https://github.github.io/spec-kit/reference/agentic-sdd.html).

**Jalon 2 :** aucune contradiction bloquante ne doit rester sans correction avant le développement.

## 12. Étape 8 — Développer progressivement

Procédez par incréments :

1. consultation du matériel ;
2. création d’une réservation ;
3. consultation des réservations personnelles ;
4. annulation ;
5. traitement complet des erreurs et vérification de la persistance.

Exemple d’instruction :

```text
/speckit.implement

Réalise uniquement les tâches de préparation nécessaires et
l'incrément de consultation du matériel.
Exécute les vérifications correspondantes puis arrête-toi
avant la création des réservations.
```

Adaptez ensuite la consigne à chaque incrément.

Après chaque livraison de GitHub Copilot :

- examinez les modifications ;
- lancez l’application ;
- exécutez les tests concernés ;
- vérifiez un comportement dans l’interface ;
- créez un commit compréhensible.

**Exercice individuel :** choisissez une fonction contenant une règle métier. Expliquez ses entrées, sa sortie, ses cas d’erreur et le test qui la vérifie.

Si vous découvrez une exigence manquante, corrigez d’abord les documents concernés. Une conversation avec l’IA ne doit pas devenir l’unique endroit où cette décision existe.

## 13. Étape 9 — Réaliser la recette

Utilisez une base de test réinitialisée et fixez la date courante au **10 mars 2030**. Les scénarios sont indépendants, sauf lorsqu’une préparation est indiquée.

| Test | Action et préparation | Résultat attendu |
|---|---|---|
| T01 | Alice réserve MAT-001 pour le 12 mars | Réservation active créée |
| T02 | MAT-001 est déjà réservé le 12 ; Bilal le demande pour le 12 | Refus ; aucun doublon |
| T03 | MAT-001 est réservé le 12 ; Bilal le demande pour le 13 | Réservation acceptée |
| T04 | Alice demande MAT-002 pour le 9 mars | Refus ; aucune création |
| T05 | Alice réserve MAT-001 et MAT-002 pour le 12 | Deux réservations acceptées |
| T06 | Alice annule sa réservation du 12 | État annulé ; historique conservé |
| T07 | Après cette annulation, Bilal réserve le même matériel le 12 | Réservation acceptée |
| T08 | Bilal tente d’annuler une réservation d’Alice | Refus ; réservation inchangée |
| T09 | Une réservation existe ; l’application redémarre | Réservation toujours présente |
| T10 | La date est absente ou invalide | Message explicite ; aucune création |
| T11 | Alice annule une réservation déjà annulée | Aucun changement ; message adapté |
| T12 | Deux demandes visent le même matériel et la même date | Au plus une réservation active |

Pour T08, vérifiez également une requête adressée directement au serveur : masquer un bouton ne suffit pas.

Pour T12, réalisez au minimum deux demandes successives au serveur. En approfondissement, testez deux demandes concurrentes.

**Tests automatiques obligatoires :** T01 à T08 et T10 à T12. T09 peut être vérifié manuellement avec une procédure reproductible.

Pour chaque test, indiquez : résultat obtenu, statut et anomalie éventuelle. Ne déclarez pas un test réussi uniquement parce que l’assistant l’affirme.

Après l’implémentation, utilisez également :

```text
/speckit.converge
```

La documentation actuelle prévoit cette commande pour rechercher des écarts restants et ajouter des tâches si nécessaire. Ce contrôle complète votre recette. Si la version fixée par l’enseignant ne la propose pas, effectuez une revue manuelle équivalente. [Guide de démarrage](https://github.github.io/spec-kit/quickstart.html).

## 14. Étape 10 — Traiter une évolution du besoin

Le département formule une nouvelle demande :

> Un étudiant ne peut pas avoir plus de deux réservations actives pour une même journée.

Avant de modifier le code :

1. identifiez la règle existante concernée ;
2. reformulez RG-04 en intégrant cette limite ;
3. ajoutez les nouveaux critères d’acceptation ;
4. actualisez le plan et les tâches concernés ;
5. relancez l’analyse de cohérence ;
6. implémentez puis vérifiez l’évolution.

Ajoutez au minimum ces tests :

- une troisième réservation active le même jour est refusée ;
- après une annulation, une nouvelle réservation devient possible ;
- deux réservations un jour n’empêchent pas d’en créer une un autre jour.

La version finale doit présenter une règle cohérente : ne conservez pas simultanément deux formulations contradictoires.

**Question :** quelle erreur aurait pu apparaître si vous aviez uniquement demandé à l’IA de modifier le code ?

## 15. Livrables attendus

Remettez un dépôt contenant :

1. **Le README** : objectif, versions, installation, lancement, tests, réinitialisation des données et limites.
2. **Les documents Spec Kit** : constitution, spécification, plan, tâches et checklists.
3. **Le code et les données fictives d’initialisation.**
4. **Les tests et le compte rendu de recette.**
5. **Une matrice de traçabilité**, reliant exigences, tâches et tests.
6. **Le journal de décisions**, avec les contributions et corrections humaines.
7. **Un bilan individuel**, de 300 à 500 mots.

Exemple de traçabilité :

| Exigence | Tâche correspondante | Vérification |
|---|---|---|
| RG-02 — Absence de doublon | Protéger la création d’une réservation | T02, T12 |
| RG-05 — Annulation par son propriétaire | Contrôler l’étudiant courant | T08 |
| RG-08 — Persistance | Enregistrer et relire les réservations | T09 |

Le bilan individuel répondra à ces questions :

- Quelle ambiguïté avez-vous identifiée ?
- Quelle proposition de l’IA avez-vous corrigée, et pourquoi ?
- Quelle partie du code pouvez-vous expliquer précisément ?
- Quel travail Spec Kit a-t-il facilité ?
- Quelle difficulté subsiste malgré les spécifications ?
- Dans quelles situations une exploration rapide par prompts vous paraît-elle utile ?

## 16. Évaluation

| Critère | Points |
|---|---:|
| Besoin, exigences et critères d’acceptation | 4 |
| Clarification, principes et cohérence des documents | 3 |
| Conception et découpage du travail | 3 |
| Application conforme aux règles métier | 4 |
| Tests, recette et traçabilité | 3 |
| Évolution correctement répercutée | 1 |
| Explication individuelle et recul critique | 2 |
| **Total** | **20** |

Une interface soignée ne compense pas une règle métier incorrecte. La quantité de code généré et le nombre de prompts ne constituent pas des critères de réussite.

### Démonstration finale — 8 minutes par binôme

- **1 minute** : problème et périmètre ;
- **3 minutes** : réservation, conflit, annulation et nouvelle réservation ;
- **2 minutes** : test automatique et lien avec une exigence ;
- **2 minutes** : évolution et correction d’une proposition de l’IA.

**Critère de réussite final : vous devez pouvoir montrer, pour une fonctionnalité, le besoin qui la justifie, la règle qui l’encadre et la vérification qui atteste son comportement.**