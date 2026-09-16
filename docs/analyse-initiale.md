# Note d'analyse initiale — Campus Matériel

**Travail demandé avant toute utilisation de l'assistant** (énoncé, section 5)
**Rédigé le** : 16 septembre 2026

> ⚠️ **Ce document est un point de départ à relire et à reprendre par le binôme.**
> L'énoncé prévoit ce travail **sans assistant**, en 20 minutes. Ce qui suit a été produit
> avant la rédaction de la spécification, puis conservé : il sert de trace du raisonnement
> initial. Chaque membre du binôme doit pouvoir le défendre et le corriger.

---

## 1. Question de départ (énoncé, section 2)

> Une application qui démarre et affiche une interface répond-elle nécessairement au besoin de
> son utilisateur ?

**Réponse.** Non. Une interface qui s'affiche prouve seulement que l'application démarre et
sait rendre une page : cela ne dit rien de la justesse des règles métier qui décident des
opérations. Le tableau partagé du département, lui aussi, « fonctionne » : il affiche des
lignes et accepte des saisies. Son défaut n'est pas d'être indisponible, mais d'autoriser des
doublons et d'ignorer certaines annulations. Une application qui se contente de remplacer le
tableau par un formulaire reproduirait exactement ces défauts, tout en donnant l'illusion du
progrès. Ce qui répond au besoin, ce n'est donc pas l'écran : c'est la règle qui empêche la
double réservation et qui refuse une annulation non autorisée.

---

## 2. Présentation du problème

Le département informatique prête du matériel pédagogique — ordinateurs portables,
vidéoprojecteurs et kits électroniques — aux étudiants. Les réservations sont aujourd'hui
inscrites dans un tableau partagé, tenu à la main par plusieurs personnes. Ce mode de gestion
produit deux défauts structurels : rien n'empêche deux personnes d'inscrire le même matériel à
la même date, et une annulation peut être oubliée ou effacée, de sorte que le matériel reste
indisponible à tort. Il en résulte des déplacements inutiles et une perte de confiance dans
l'outil : les étudiants finissent par venir vérifier sur place plutôt que de se fier au tableau.
Le besoin n'est donc pas seulement de « numériser le tableau », mais de garantir qu'une
réservation enregistrée est réellement honorée et qu'une annulation libère vraiment le matériel.

---

## 3. Trois besoins utilisateurs

| Réf. | Besoin | Pourquoi il compte |
|---|---|---|
| BU-01 | En tant qu'étudiante, je souhaite **savoir ce qui est réellement disponible à une date donnée** afin de choisir un matériel que je pourrai effectivement emprunter. | C'est la raison d'utiliser l'application plutôt que le tableau. Sans cette information fiable, l'utilisateur revient vérifier physiquement. |
| BU-02 | En tant qu'étudiant, je souhaite **réserver un matériel pour une journée déterminée** afin d'en garantir la disponibilité le jour où j'en ai besoin. | C'est l'engagement qui manque au tableau : une réservation enregistrée ne peut pas être « écrasée » par une autre saisie. |
| BU-03 | En tant qu'étudiant, je souhaite **annuler une réservation que je n'utiliserai plus** afin de libérer le matériel pour quelqu'un d'autre. | Une annulation non prise en compte retire du matériel disponible sans raison. C'est le second défaut constaté sur le tableau. |

*Besoins secondaires déduits :* consulter ses propres réservations (nécessaire pour savoir quoi
annuler) et ne pouvoir annuler que ses propres réservations (nécessaire pour que le point
précédent ne devienne pas une source de désordre).

---

## 4. Cinq questions qu'un développeur poserait au département

| Réf. | Question | Réponse trouvée dans les règles métier |
|---|---|---|
| Q-01 | La réservation porte-t-elle sur une journée ou sur des heures ? | **Répondue sans ambiguïté** : journée entière. Les réservations sur plusieurs jours et par créneau horaire sont explicitement exclues du périmètre. |
| Q-02 | Que se passe-t-il après une annulation : la trace disparaît-elle ou est-elle conservée ? | **Répondue** : RG-07 impose de conserver l'historique et de libérer l'équipement. |
| Q-03 | Deux étudiants peuvent-ils réserver simultanément le même équipement ? | **Répondue sur le résultat** : RG-02 impose au plus une réservation active par équipement et par date. En revanche, l'énoncé ne dit pas **comment** s'en assurer lorsque deux demandes arrivent au même instant. |
| Q-04 | Peut-on annuler une réservation une fois la date passée ? | **Répondue** : RG-06 autorise l'annulation jusqu'au jour réservé inclus, donc refuse au-delà. |
| Q-05 | Que doit-il se passer si l'entrée est invalide ? | **Répondue** : RG-09 impose un message compréhensible et aucune modification des données. |

### Questions restées ouvertes après lecture de l'énoncé

Ces points ne sont tranchés par aucune règle. Ils ont été **signalés puis décidés explicitement
avec le binôme**, jamais résolus en silence :

| Réf. | Question restée ouverte | Traitement |
|---|---|---|
| QO-01 | Quand un étudiant redemande un matériel qu'il a lui-même déjà réservé, doit-il recevoir le même message que pour un conflit avec un autre étudiant ? | Décision **CA-01** : message distinct. Voir `spec.md` et le journal de décisions. |
| QO-02 | L'état d'une réservation doit-il distinguer l'origine d'une annulation ? | Décision **CA-02** : trois états, dont un non atteignable dans ce périmètre. |
| QO-03 | Le département parle plus loin d'une limite de deux réservations par étudiant et par jour : faut-il l'intégrer maintenant ? | Décision **CA-03** : non, c'est une évolution prévue après la recette. |

**Aucune de ces trois questions n'a reçu de réponse inventée.**

---

## 5. Deux risques de mauvaise interprétation

| Réf. | Risque | Conséquence si on se trompe | Mesure retenue |
|---|---|---|---|
| RM-01 | Croire que « empêcher les doubles réservations » se règle en **masquant l'option** dans la page, parce que le bouton disparaît une fois le matériel réservé. | Un utilisateur qui adresse directement la requête au serveur — ou qui recharge une page ouverte avant la réservation — crée un doublon. L'application paraît correcte et le défaut ne se voit que sous concurrence. | Le contrôle est refait **côté serveur** à chaque requête, et le scénario T08 le vérifie par une requête directe. La contrainte de base garantit le résultat même en cas de demandes simultanées. |
| RM-02 | Croire que « consulter le matériel disponible » signifie afficher **tout** le matériel, en comptant sur l'utilisateur pour vérifier lui-même les réservations existantes. | On reproduit le tableau partagé : l'information affichée n'est pas fiable, et le besoin BU-01 n'est pas couvert. | La disponibilité est **calculée** à partir des réservations actives pour la date consultée : le libellé « Réservé » n'est pas une décoration, il résulte d'une règle (FR-004). |

*Risque écarté mais notable :* supposer que la sélection de l'étudiant courant constitue une
sécurité. L'énoncé est explicite — c'est une simulation d'identité, pas une authentification.
La présenter comme une protection serait une erreur d'interprétation, mais l'énoncé la lève lui-même.

---

## 6. Exercice individuel (énoncé, section 12)

> Choisir une fonction contenant une règle métier, expliquer ses entrées, sa sortie, ses cas
> d'erreur et le test qui la vérifie.

**Fonction choisie** : `ReservationService.reserver` (implémente RG-01, RG-02, RG-03, RG-09 et
FR-025).

### Entrées

| Paramètre | Type | Origine |
|---|---|---|
| `materielId` | `Long` | Champ du formulaire, transmis par le contrôleur |
| `date` | `LocalDate` | Champ du formulaire, converti par Spring |
| `etudiantId` | `Long` ou `null` | **Session HTTP**, jamais le formulaire |

### Sortie

La réservation créée, avec son identifiant, son état `ACTIVE` et la clé technique qui occupe le
créneau. En cas de refus, la fonction ne renvoie rien : elle lève une `RegleMetierException`
porteuse d'un `MotifRefus`.

### Cas d'erreur et ordre des contrôles

L'ordre est contractuel : il rend le message affiché déterministe lorsqu'une requête cumule
plusieurs anomalies.

| Ordre | Condition | Motif levé |
|---|---|---|
| 1 | `date` est nulle | `DATE_INVALIDE` |
| 2 | `date` est antérieure à `LocalDate.now(clock)` | `DATE_PASSEE` |
| 3 | `etudiantId` est nul | `ETUDIANT_NON_SELECTIONNE` |
| 4 | `etudiantId` ne correspond à aucun étudiant | `ETUDIANT_INCONNU` |
| 5 | `materielId` ne correspond à aucun matériel | `MATERIEL_INCONNU` |
| 6 | Une réservation active existe sur (matériel, date) — propriétaire = un autre étudiant | `MATERIEL_INDISPONIBLE` |
| 7 | Une réservation active existe sur (matériel, date) — propriétaire = l'étudiant courant | `DEJA_RESERVE_PAR_VOUS` |
| 8 | Violation de la contrainte d'unicité à l'écriture (demande concurrente) | `MATERIEL_INDISPONIBLE` |

**Aucun de ces refus n'écrit en base** : chaque contrôle est effectué avant l'enregistrement
(RG-09).

### Test qui la vérifie

`src/test/java/com/campus/campusmateriel/service/ReservationServiceReservationTest.java`

| Test | Ce qu'il vérifie |
|---|---|
| `t01ReservationValide` | Cas nominal : réservation créée, état `ACTIVE`, clé technique renseignée |
| `t04DatePasseeRefusee` | Contrôle 2, et `reservationRepository.count() == 0` |
| `sansEtudiantCourantRefuse` | Contrôle 3 |
| `materielInconnuRefuse` | Contrôle 5 |
| `t02DoubleReservationRefusee` | Contrôle 6, et une seule réservation active après le refus |
| `dejaReserveParSoiMemeMotifDistinct` | Contrôle 7, et le motif est bien **différent** de `MATERIEL_INDISPONIBLE` |
| `ordreDesControlesDeterministe` | L'ordre : date contrôlée avant matériel |
| `t12DeuxDemandesSuccessives` | Au plus une réservation active sur le couple |

Les tests fixent la date courante au **10 mars 2030** via `ConfigurationHorlogeTest` : le
résultat ne dépend donc pas du jour d'exécution.
