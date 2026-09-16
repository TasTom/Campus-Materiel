# Spécification fonctionnelle : Réservation de matériel pédagogique

**Dossier de spécification** : `specs/001-reservation-materiel`

**Créé le** : 2026-09-16

**Statut** : Clarifiée — en attente de validation (Jalon 1)

**Clarifications** : les ambiguïtés A-01 à A-03 ont été tranchées le 2026-09-16 (voir
« Décisions de clarification » : CA-01, CA-02, CA-03).

**Entrée** : description issue de `docs/enonce.md` — « Décris la fonctionnalité de réservation
de matériel de Campus Matériel. Organise les besoins en histoires utilisateur priorisées.
Attribue un identifiant aux exigences. Définis des critères d'acceptation observables. Couvre
les cas normaux et les principaux cas d'erreur. Signale les ambiguïtés et les hypothèses.
N'introduis pas de fonctionnalité supplémentaire. Ne choisis pas encore de technologie. »

> Cette spécification décrit **le comportement attendu**, pas la manière de le construire.
> Les choix techniques sont traités dans le plan.

## Contexte et objectif

Le département informatique prête du matériel aux étudiants : ordinateurs portables,
vidéoprojecteurs et kits électroniques. Les réservations sont actuellement inscrites dans un
tableau partagé : des doublons apparaissent et certaines annulations ne sont pas prises en
compte.

L'objectif est de remplacer ce tableau par une application unique et fiable, où l'état des
réservations ne dépend plus de la discipline des personnes qui écrivent dedans.

## Utilisateurs concernés

| Acteur | Description | Ce qu'il attend |
|---|---|---|
| Étudiant emprunteur | Étudiant du département souhaitant emprunter un équipement | Savoir ce qui est libre et obtenir une réservation fiable |
| Responsable du prêt (hors application) | Personne qui constate l'état du matériel | Un historique fiable, sans doublon |

Le périmètre de cette version ne comporte **aucun mécanisme d'authentification**. L'étudiant
courant est choisi dans une liste ; il s'agit d'une simulation d'identité destinée au TP.

---

## Scénarios utilisateur et tests

### Histoire 1 — Choisir l'étudiant courant (Priorité : P1)

En tant qu'utilisateur de l'application, je souhaite indiquer quel étudiant je représente afin
que mes actions portent sur le bon compte et que je ne puisse pas agir au nom d'un autre.

**Pourquoi cette priorité** : c'est la condition préalable à toute action. Sans étudiant courant,
impossible de réserver ni d'annuler. Cette histoire ne délivre pas de valeur à elle seule mais
débloque toutes les autres.

**Test indépendant** : sélectionner « Alice Martin » puis vérifier que l'application affiche
Alice comme étudiante courante ; sélectionner ensuite « Bilal Dupont » et vérifier que l'affichage
suit ce changement.

**Scénarios d'acceptation** :

1. **Étant donné** que l'application vient de démarrer et qu'aucun étudiant n'a été choisi,
   **quand** j'ouvre la page de sélection, **alors** les trois étudiants fictifs sont proposés.
2. **Étant donné** que je sélectionne « Alice Martin », **quand** l'application revient à
   l'affichage principal, **alors** l'étudiante courante est Alice Martin.
3. **Étant donné** qu'Alice Martin est l'étudiante courante, **quand** je sélectionne
   « Bilal Dupont », **alors** l'étudiant courant devient Bilal Dupont.

---

### Histoire 2 — Consulter le matériel disponible à une date (Priorité : P1)

En tant qu'étudiante, je souhaite voir les équipements disponibles à une date donnée afin de
choisir un matériel que je pourrai effectivement emprunter.

**Pourquoi cette priorité** : consulter les disponibilités est la raison première d'utiliser
l'application plutôt que le tableau partagé. C'est aussi le socle de l'histoire 3 : on réserve
ce que l'on a vu libre.

**Test indépendant** : demander la liste des matériels pour une date sans aucune réservation,
puis pour une date où un matériel est réservé, et comparer les deux affichages.

**Scénarios d'acceptation** :

1. **Étant donné** qu'aucune réservation n'existe pour le 12 mars 2030, **quand** je consulte
   le matériel à cette date, **alors** les cinq équipements fictifs sont signalés disponibles.
2. **Étant donné** que MAT-001 possède une réservation active au 12 mars 2030, **quand** je
   consulte le matériel à cette date, **alors** MAT-001 est signalé indisponible et les quatre
   autres équipements sont signalés disponibles.
3. **Étant donné** que MAT-001 est réservé au 12 mars 2030, **quand** je consulte le matériel
   au 13 mars 2030, **alors** MAT-001 est de nouveau signalé disponible.
4. **Étant donné** qu'un matériel a été annulé pour le 12 mars 2030, **quand** je consulte le
   matériel à cette date, **alors** ce matériel est signalé disponible.
5. **Étant donné** une date pour laquelle aucun matériel n'est disponible, **quand** je consulte
   la liste, **alors** un message explicite m'indique qu'aucun matériel n'est disponible, sans
   erreur technique affichée.

---

### Histoire 3 — Réserver un équipement pour une journée (Priorité : P1)

En tant qu'étudiant, je souhaite réserver un équipement pour une journée précise afin de
garantir sa disponibilité le jour où j'en ai besoin.

**Pourquoi cette priorité** : c'est la finalité de l'application. Sans cette histoire, les
histoires 2, 4 et 5 n'ont plus d'objet.

**Test indépendant** : réserver un matériel libre pour une date future et vérifier que la
réservation apparaît comme active ; réserver le même matériel à la même date avec un autre
étudiant et vérifier le refus.

**Scénarios d'acceptation** :

1. **Étant donné** que MAT-001 est disponible le 12 mars 2030 et que la date courante est le
   10 mars 2030, **quand** Alice réserve MAT-001 pour le 12 mars 2030, **alors** une réservation
   active est créée et l'application confirme l'opération.
2. **Étant donné** que MAT-003 possède une réservation active pour Alice le 12 mars 2030 et que
   la date courante est le 10 mars 2030, **quand** Bilal demande MAT-003 pour le 12 mars 2030,
   **alors** sa demande est refusée avec un message indiquant l'indisponibilité, **et** aucune
   réservation supplémentaire n'est enregistrée.
3. **Étant donné** que MAT-001 est réservé le 12 mars 2030, **quand** Bilal le demande pour le
   13 mars 2030, **alors** sa réservation est acceptée.
4. **Étant donné** que la date courante est le 10 mars 2030, **quand** Alice demande MAT-002
   pour le 9 mars 2030, **alors** la demande est refusée car la date est passée, **et** aucune
   réservation n'est créée.
5. **Étant donné** que la date courante est le 10 mars 2030, **quand** Alice réserve MAT-001
   et MAT-002 pour le 12 mars 2030, **alors** les deux réservations sont acceptées.
6. **Étant donné** que la date courante est le 10 mars 2030, **quand** Alice réserve un matériel
   pour le 10 mars 2030, **alors** la réservation est acceptée (le jour même est autorisé).
7. **Étant donné** qu'Alice a déjà une réservation active pour MAT-001 le 12 mars 2030, **quand**
   Alice demande de nouveau MAT-001 pour le 12 mars 2030, **alors** la demande est refusée,
   **et** le message indique explicitement qu'Alice a déjà réservé ce matériel pour cette date
   (message distinct de l'indisponibilité), **et** aucune réservation supplémentaire n'est
   créée [décision CA-01].
8. **Étant donné** un identifiant de matériel inexistant, **quand** une réservation est
   demandée, **alors** la demande est refusée avec un message explicite, **et** aucune
   réservation n'est créée.
9. **Étant donné** que la date est absente, vide ou mal formée, **quand** une réservation est
   demandée, **alors** la demande est refusée avec une explication, **et** aucune réservation
   n'est créée.
10. **Étant donné** qu'aucun étudiant courant n'a été sélectionné, **quand** une réservation
    est demandée, **alors** la demande est refusée et l'application invite à choisir un
    étudiant.

---

### Histoire 4 — Consulter les réservations d'un étudiant (Priorité : P2)

En tant qu'étudiant, je souhaite voir la liste de mes réservations afin de savoir ce que j'ai
emprunté, à quelle date et dans quel état.

**Pourquoi cette priorité** : sans cette consultation, l'étudiant ne peut pas retrouver
l'identifiant nécessaire à une annulation. C'est une histoire de lecture, sans risque de
corruption des données, donc moins critique que les histoires 1 à 3.

**Test indépendant** : créer deux réservations pour Alice et une pour Bilal ; vérifier qu'Alice
voit ses deux réservations et pas celle de Bilal.

**Scénarios d'acceptation** :

1. **Étant donné** qu'Alice a deux réservations actives, **quand** elle consulte ses
   réservations, **alors** ces deux réservations sont affichées avec leur matériel, leur date
   et leur état.
2. **Étant donné** qu'une réservation a été annulée, **quand** l'étudiant consulte ses
   réservations, **alors** cette réservation apparaît avec l'état « annulée » et reste
   consultable (l'historique est conservé).
3. **Étant donné** qu'Alice n'a aucune réservation, **quand** elle consulte ses réservations,
   **alors** un message explicite indique qu'elle n'a aucune réservation, sans erreur technique.
4. **Étant donné** que Bilal possède des réservations, **quand** Alice consulte ses propres
   réservations, **alors** aucune réservation de Bilal n'apparaît.

---

### Histoire 5 — Annuler une réservation (Priorité : P2)

En tant qu'étudiant, je souhaite annuler une de mes réservations afin de libérer l'équipement
dont je n'ai plus besoin.

**Pourquoi cette priorité** : l'annulation est ce qui manque le plus au tableau partagé, mais
elle suppose qu'une réservation existe déjà. Elle vient donc après les histoires 1 à 3.

**Test indépendant** : créer une réservation pour Alice, l'annuler, vérifier que son état
devient « annulée » et que le matériel redevient disponible à cette date.

**Scénarios d'acceptation** :

1. **Étant donné** qu'Alice possède une réservation active pour le 12 mars 2030 et que la date
   courante est le 10 mars 2030, **quand** elle annule cette réservation, **alors** son état
   devient « annulée », l'enregistrement est conservé et l'application confirme l'opération.
2. **Étant donné** qu'Alice vient d'annuler sa réservation du 12 mars 2030, **quand** Bilal
   demande le même matériel pour le 12 mars 2030, **alors** sa demande est acceptée : le
   matériel est de nouveau disponible.
3. **Étant donné** qu'Alice possède une réservation active pour le 12 mars 2030 et que la date
   courante est le 12 mars 2030, **quand** elle annule cette réservation, **alors** l'annulation
   est acceptée (le jour réservé est inclus).
4. **Étant donné** qu'Alice possède une réservation pour le 12 mars 2030 et que la date courante
   est le 13 mars 2030, **quand** elle annule cette réservation, **alors** l'annulation est
   refusée car le jour réservé est passé.
5. **Étant donné** qu'Alice possède une réservation active, **quand** Bilal tente de l'annuler,
   **alors** la demande est refusée, la réservation reste inchangée **et ce refus vaut aussi
   lorsque la demande est adressée directement au serveur, sans passer par l'interface**.
6. **Étant donné** qu'Alice possède une réservation déjà annulée, **quand** elle l'annule de
   nouveau, **alors** aucune donnée n'est modifiée (l'état d'annulation et son origine restent
   inchangés) et un message lui indique que la réservation est déjà annulée.
7. **Étant donné** qu'Alice annule une de ses réservations actives, **quand** elle consulte
   ensuite la réservation, **alors** l'état indique qu'elle a été annulée par l'étudiant
   [décision CA-02].
8. **Étant donné** une réservation inexistante, **quand** une annulation est demandée, **alors**
   la demande est refusée avec un message explicite, sans modification des données.

---

### Cas limites

| Référence | Situation | Comportement attendu |
|---|---|---|
| CL-01 | Annulation d'une réservation déjà annulée | Aucune modification ; message indiquant son état |
| CL-02 | Réservation d'un matériel inconnu | Refus ; aucune création |
| CL-03 | Date absente, vide ou mal formée | Refus avec explication ; aucune création |
| CL-04 | Annulation d'une réservation dont le jour réservé est passé | Refus |
| CL-05 | Liste de résultats vide (matériel ou réservations) | Message explicite, sans erreur technique |
| CL-06 | Aucun étudiant courant sélectionné | Refus des actions qui modifient les données, avec invitation à choisir un étudiant |
| CL-07 | Deux demandes de réservation simultanées visant le même matériel à la même date | Au plus une seule réservation active est enregistrée |
| CL-08 | Annulation par un étudiant qui n'est pas le propriétaire, via une requête directe au serveur | Refus ; la réservation reste inchangée |
| CL-09 | Un étudiant réserve le même matériel à deux dates différentes | Les deux réservations sont acceptées |

---

## Exigences

### Exigences fonctionnelles

Chaque exigence est vérifiable indépendamment. La colonne « Règle » indique la règle métier de
`docs/enonce.md` dont elle découle.

| Identifiant | Exigence | Règle |
|---|---|---|
| FR-001 | Le système DOIT proposer une liste de trois étudiants fictifs et permettre d'en désigner un comme étudiant courant. | — |
| FR-002 | Le système DOIT mémoriser l'étudiant courant entre deux actions de l'utilisateur. | — |
| FR-003 | Le système DOIT afficher, pour une date donnée, la liste des équipements avec leur disponibilité. | — |
| FR-004 | Le système DOIT considérer un équipement comme indisponible à une date si une réservation active existe pour cet équipement et cette date. | RG-02 |
| FR-005 | Le système DOIT permettre de réserver un équipement pour une journée, pour l'étudiant courant. | RG-01 |
| FR-006 | Le système DOIT refuser de créer une seconde réservation active pour le même équipement à la même date, avec un message indiquant l'indisponibilité. | RG-02 |
| FR-007 | Le système DOIT accepter une date de réservation égale à la date courante ou postérieure, et refuser toute date antérieure. | RG-03 |
| FR-008 | Le système DOIT permettre à un étudiant de détenir plusieurs réservations actives à la même date, sur des équipements différents. | RG-04 |
| FR-009 | Le système DOIT permettre à l'étudiant courant de consulter uniquement les réservations dont il est le propriétaire. | RG-05 |
| FR-010 | Le système DOIT refuser une annulation demandée par un étudiant autre que le propriétaire de la réservation. | RG-05 |
| FR-011 | Le système DOIT accepter une annulation tant que le jour réservé n'est pas passé, jour réservé inclus. | RG-06 |
| FR-012 | Le système DOIT refuser une annulation lorsque le jour réservé est passé. | RG-06 |
| FR-013 | Le système DOIT conserver l'enregistrement d'une réservation annulée et le signaler comme annulé. | RG-07 |
| FR-014 | Le système DOIT considérer un équipement comme de nouveau disponible après l'annulation de sa réservation. | RG-07 |
| FR-015 | Le système DOIT conserver les réservations après l'arrêt et le redémarrage de l'application. | RG-08 |
| FR-016 | En cas d'entrée invalide, le système DOIT afficher un message compréhensible en français et ne modifier aucune donnée. | RG-09 |
| FR-017 | Le système DOIT refuser toute action modifiant les données lorsqu'aucun étudiant courant n'est désigné. | RG-09 |
| FR-018 | Le système DOIT refuser une réservation visant un identifiant de matériel inexistant. | RG-09 |
| FR-019 | Le système DOIT refuser une date absente, vide ou mal formée. | RG-09 |
| FR-020 | Le système DOIT refuser une annulation visant une réservation inexistante. | RG-09 |
| FR-021 | Le système DOIT n'enregistrer au plus qu'une seule réservation active lorsqu'il reçoit plusieurs demandes visant le même équipement à la même date. | RG-02 |
| FR-022 | Le système DOIT afficher un message explicite, sans erreur technique, lorsqu'une liste ne contient aucun résultat. | RG-09 |
| FR-023 | Le système DOIT distinguer chaque exemplaire physique de matériel par un identifiant distinct. | RG-01 |
| FR-024 | La date courante utilisée par les règles métier DOIT pouvoir être fixée artificiellement afin que les vérifications soient reproductibles. | — |
| FR-025 | Lorsqu'un étudiant demande un matériel qu'il a lui-même déjà réservé à la même date, le système DOIT refuser la demande avec un message qui distingue ce cas de l'indisponibilité générale. | RG-02 |
| FR-026 | Le système DOIT conserver l'origine d'une annulation : annulation demandée par l'étudiant ou annulation administrative. | RG-07 |
| FR-027 | L'application DOIT attribuer l'origine « annulation demandée par l'étudiant » à toute annulation qu'elle réalise, aucune interface d'administration n'existant dans ce périmètre. | RG-07 |

### Entités clés

| Entité | Représente | Informations minimales |
|---|---|---|
| Étudiant | Une personne fictive autorisée à emprunter | Identifiant, nom |
| Matériel | Un exemplaire physique unique d'équipement | Identifiant, nom, catégorie |
| Réservation | L'occupation d'un matériel par un étudiant à une date | Identifiant, étudiant, matériel, date, état |

**État d'une réservation** : une réservation active peut être annulée. Conformément à la
décision de clarification CA-02, l'état **distingue l'origine** de l'annulation :

| État | Signification | Atteignable dans cette version |
|---|---|---|
| active | La réservation est en cours ; elle rend le matériel indisponible à sa date | Oui |
| annulée par l'étudiant | L'étudiant propriétaire a annulé la réservation | Oui |
| annulée par l'administration | Annulation décidée hors application | **Non** — aucune interface d'administration n'est au périmètre (FR-027) |

Les états « annulée » libèrent l'équipement à la date concernée (RG-07) et ne comptent pas dans
la recherche d'un conflit de réservation (RG-02). L'état « annulée par l'administration » n'est
produit par aucune fonctionnalité de cette version : il est défini pour que le modèle d'état
soit complet sans nécessiter de modification lorsqu'un tel traitement sera ajouté.

**Relations** : une réservation concerne exactement un étudiant et exactement un matériel. Un
étudiant peut avoir plusieurs réservations ; un matériel peut avoir plusieurs réservations,
mais au plus une seule active pour une date donnée (les réservations annulées ne comptent pas).

---

## Critères de succès

### Résultats mesurables

- **SC-001** : un étudiant peut consulter les disponibilités d'une date en une seule action,
  sans saisir de texte.
- **SC-002** : sur une série de demandes visant le même matériel à la même date, la base ne
  contient jamais plus d'une réservation active, quelle que soit l'ordre des demandes.
- **SC-003** : après l'arrêt puis le redémarrage de l'application, 100 % des réservations
  enregistrées sont toujours présentes, avec leur état.
- **SC-004** : 100 % des 12 scénarios de recette T01 à T12 de `docs/enonce.md` produisent le
  résultat attendu.
- **SC-005** : 100 % des exigences FR-001 à FR-027 sont couvertes par au moins une vérification
  et figurent dans la matrice de traçabilité.
- **SC-006** : toute saisie invalide produit un message compréhensible en français, et le nombre
  d'enregistrements modifiés est nul.
- **SC-007** : un étudiant constate qu'un équipement libéré par annulation est de nouveau
  réservable à la date concernée, sans intervention manuelle sur les données.

---

## Hypothèses

- **H-01** : la réservation porte sur une **journée entière**, pas sur un créneau horaire. Cette
  hypothèse est imposée par le périmètre de l'énoncé (les créneaux horaires sont exclus).
- **H-02** : « aujourd'hui » désigne la date du serveur local, pas celle du navigateur du
  client. Une date de test pourra être fixée artificiellement (FR-024).
- **H-03** : l'étudiant courant est choisi librement dans une liste. Il n'y a **aucune
  authentification** : n'importe quel utilisateur peut changer d'étudiant. Les règles FR-009 et
  FR-010 limitent les actions au nom de l'étudiant courant, mais ne protègent pas contre un
  utilisateur qui change volontairement d'identité. Cette limite est imposée par l'énoncé.
- **H-04** : les données initiales (trois étudiants, cinq équipements) sont fictives et doivent
  pouvoir être recréées sans être dupliquées lors d'un redémarrage.
- **H-05** : l'application fonctionne localement, pour un usage pédagogique à faible charge. Les
  exigences de disponibilité, de performance et de montée en charge sont hors périmètre.
- **H-06** : l'historique des réservations annulées est conservé indéfiniment dans le cadre du
  TP ; aucune purge ni archivage n'est prévu.
- **H-07** : le matériel et les étudiants ne sont pas modifiables par l'application (pas
  d'interface d'administration), conformément au périmètre exclu.

## Décisions de clarification

Les ambiguïtés de la première version ont été examinées et tranchées explicitement avec le
binôme. Aucune réponse n'a été inventée : chaque décision est consignée ci-dessous et reportée
dans `docs/journal-decisions.md`.

| Référence | Question examinée | Décision retenue | Conséquence sur la spécification |
|---|---|---|---|
| CA-01 | Un étudiant re-réservant un matériel qu'il a lui-même déjà réservé doit-il recevoir un message distinct de l'indisponibilité générale ? | **Oui.** Le message précise que l'étudiant a déjà réservé ce matériel pour cette date. Le refus reste le même : aucune réservation n'est créée. | FR-025 ajoutée ; scénario 7 de l'histoire 3 précisé |
| CA-02 | L'état d'une réservation doit-il distinguer l'origine d'une annulation ? | **Oui.** Trois états : active, annulée par l'étudiant, annulée par l'administration. Cette décision **étend** le modèle minimal de l'énoncé et s'écarte de la solution de référence, qui ne prévoit que deux états. | FR-026 et FR-027 ajoutées ; section « Entités clés » précisée |
| CA-03 | La limite de deux réservations actives par étudiant et par jour doit-elle figurer dans cette version ? | **Non.** Elle reste hors périmètre et sera traitée comme une évolution, en commençant par la reformulation de RG-04. | Déjà présente dans les exclusions du périmètre ; décision tracée au journal (`docs/journal-decisions.md`, D-08) |

### Réponses imposées par l'énoncé (rappel)

| Situation | Décision | Traitement |
|---|---|---|
| Annulation d'une réservation déjà annulée | Aucune modification ; message indiquant son état | CL-01, FR-013, FR-016 |
| Réservation d'un matériel inconnu | Refus ; aucune création | CL-02, FR-018 |
| Date absente ou mal formée | Refus avec explication | CL-03, FR-019 |
| Annulation d'une réservation passée | Refus | CL-04, FR-012 |
| Liste sans résultat | Message explicite, sans erreur technique | CL-05, FR-022 |

**Point de vigilance CA-02** : l'état « annulée par l'administration » n'est produit par aucune
fonctionnalité de cette version. Il ne doit donc donner lieu à **aucun code de traitement
d'annulation administrative**, ni à une route, ni à un formulaire. FR-027 garantit que
l'application n'attribue jamais cet état, et cette garantie doit être vérifiée par un test.
L'ajouter au modèle d'état ne justifie aucune fonctionnalité supplémentaire.

## Exclusions du périmètre

Cette version **ne comporte pas** :

- de comptes, mots de passe ou authentification ;
- de courriels ni de notifications ;
- de gestion des retards et des pénalités ;
- de réservations sur plusieurs jours ;
- de réservations par créneau horaire ;
- d'ajout ou de suppression de matériel par une interface d'administration ;
- de limite au nombre de réservations actives d'un étudiant pour une même journée.

Toute demande relevant de cette liste doit faire l'objet d'une décision explicite du binôme et
d'une mise à jour préalable de la spécification, du plan et des tâches.

## Traçabilité vers les scénarios de recette

| Scénario | Histoire | Exigences couvertes |
|---|---|---|
| T01 | 3 | FR-005 |
| T02 | 3 | FR-004, FR-006, FR-021 |
| T03 | 3 | FR-004, FR-008 |
| T04 | 3 | FR-007 |
| T05 | 3 | FR-005, FR-008 |
| T06 | 5 | FR-011, FR-013 |
| T07 | 5 | FR-014 |
| T08 | 5 | FR-010 |
| T09 | 3, 5 | FR-015 |
| T10 | 3 | FR-019, FR-016 |
| T11 | 5 | FR-013, FR-016, FR-026 |
| T12 | 3 | FR-021 |
