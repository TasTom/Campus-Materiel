# Contrat des routes : Réservation de matériel pédagogique

**Fonctionnalité** : `001-reservation-materiel` | **Date** : 2026-09-16

Ce document décrit ce que l'application expose à l'utilisateur : routes, paramètres, réponses et
cas d'erreur. Il sert de contrat pour l'implémentation et pour les tests.

Toutes les actions qui modifient des données utilisent `POST` puis redirigent (motif
`POST` → redirection → `GET`) afin qu'un rafraîchissement du navigateur ne rejoue pas
l'opération.

---

## Vue d'ensemble

| Méthode | Route | Rôle | Exigences |
|---|---|---|---|
| `GET` | `/` | Rediriger vers les disponibilités du jour | — |
| `GET` | `/etudiants/selection` | Afficher les étudiants et l'étudiant courant | FR-001 |
| `POST` | `/etudiants/selection` | Mémoriser l'étudiant courant | FR-001, FR-002 |
| `GET` | `/materiels?date=AAAA-MM-JJ` | Afficher les disponibilités à une date | FR-003, FR-004, FR-022 |
| `POST` | `/reservations` | Réserver pour l'étudiant courant | FR-005 à FR-008, FR-017 à FR-019 |
| `GET` | `/reservations` | Afficher les réservations de l'étudiant courant | FR-009, FR-022 |
| `POST` | `/reservations/{id}/annulation` | Annuler une réservation de l'étudiant courant | FR-010 à FR-014, FR-020 |

---

## `GET /`

**Rôle** : point d'entrée. Redirige vers `/materiels` avec la date courante.

**Réponse** : `302` vers `/materiels?date=<date courante>`.

**Note** : la date courante provient de la `Clock` injectée, jamais du navigateur. Si l'étudiant
courant n'est pas défini, la page de disponibilités reste consultable ; seule une action
modifiant les données est refusée (FR-017).

---

## `GET /etudiants/selection`

**Rôle** : afficher la liste des étudiants fictifs et signaler l'étudiant courant.

**Paramètres** : aucun.

**Réponse** : `200`, page affichant les trois étudiants, celui qui est courant étant identifié.

**Exigences** : FR-001.

---

## `POST /etudiants/selection`

**Rôle** : mémoriser l'étudiant courant pour la suite de la session.

**Paramètres** :

| Nom | Type | Obligatoire | Description |
|---|---|---|---|
| `etudiantId` | entier | oui | Identifiant de l'étudiant choisi |

**Réponse en cas de succès** : `302` vers `/materiels?date=<date courante>`.

**Cas d'erreur** :

| Condition | Code de refus | Réponse | Écriture |
|---|---|---|---|
| `etudiantId` absent | — | Retour au formulaire avec message | Aucune |
| `etudiantId` ne correspond à aucun étudiant | — | Retour au formulaire avec message explicite | Aucune |

**Exigences** : FR-001, FR-002.

**Note de sécurité (simulation assumée)** : n'importe qui peut changer d'étudiant. C'est une
simulation d'identité imposée par l'énoncé (H-03), pas une authentification.

---

## `GET /materiels?date=AAAA-MM-JJ`

**Rôle** : afficher les cinq équipements et leur disponibilité à la date demandée.

**Paramètres** :

| Nom | Type | Obligatoire | Description |
|---|---|---|---|
| `date` | date `AAAA-MM-JJ` | non | Date consultée. Absente ⇒ date courante |

**Réponse en cas de succès** : `200`, page affichant, pour chaque matériel : code, nom,
catégorie et disponibilité (« disponible » ou « réservé »).

**Cas d'erreur** :

| Condition | Réponse | Écriture |
|---|---|---|
| Date mal formée ou illisible | `200` avec message explicite, liste non affichée | Aucune |
| Aucun matériel dans la liste | `200` avec message « aucun matériel » | Aucune |

**Règle de calcul** : un matériel est **indisponible** à une date s'il existe une réservation dont
le statut est `ACTIVE` pour ce matériel et cette date (FR-004). Les réservations annulées ne
rendent jamais un matériel indisponible (RG-07).

**Exigences** : FR-003, FR-004, FR-022.

---

## `POST /reservations`

**Rôle** : créer une réservation pour l'étudiant courant.

**Paramètres** :

| Nom | Type | Obligatoire | Description |
|---|---|---|---|
| `materielId` | entier | oui | Matériel à réserver |
| `dateReservation` | date `AAAA-MM-JJ` | oui | Journée réservée |

**Paramètre interdit** : l'identifiant de l'étudiant **n'est pas** un paramètre de ce formulaire.
Il est lu depuis la session (R-06). Un paramètre `etudiantId` envoyé par le client est **ignoré**.

**Réponse en cas de succès** : `302` vers `/reservations`.

**Cas d'erreur** — tous produisent un message français et **aucune écriture** (RG-09) :

| Condition | Code de refus | Réponse |
|---|---|---|
| `dateReservation` absente ou illisible | `DATE_INVALIDE` | Retour à la page avec message |
| Aucun étudiant courant défini | `ETUDIANT_NON_SELECTIONNE` | Invitation à choisir un étudiant |
| `materielId` ne correspond à aucun matériel | `MATERIEL_INCONNU` | Retour avec message explicite |
| Date antérieure à la date courante | `DATE_PASSEE` | Retour avec message explicite |
| Réservation active existante sur (matériel, date), propriétaire = un autre étudiant | `MATERIEL_INDISPONIBLE` | Retour avec message d'indisponibilité |
| Réservation active existante sur (matériel, date), propriétaire = l'étudiant courant | `DEJA_RESERVE_PAR_VOUS` | Retour avec message distinct (FR-025) |
| Violation de la contrainte d'unicité lors de l'écriture (demande concurrente) | `MATERIEL_INDISPONIBLE` | Retour avec message d'indisponibilité |

**Ordre d'évaluation des contrôles** : date → étudiant courant → matériel → conflit. Cet ordre est
contractuel : les tests s'y appuient.

**Cas autorisés explicitement** :

- réserver le jour même (date réservée = date courante) ;
- réserver plusieurs matériels différents à la même date pour le même étudiant (RG-04, FR-008) ;
- réserver le même matériel à deux dates différentes.

**Exigences** : FR-005 à FR-008, FR-016 à FR-019, FR-021, FR-025 · RG-01, RG-02, RG-03, RG-04, RG-09.

---

## `GET /reservations`

**Rôle** : afficher les réservations de l'étudiant courant, annulées comprises.

**Paramètres** : aucun. L'identifiant de l'étudiant provient de la session.

**Réponse en cas de succès** : `200`, page listant pour chaque réservation : matériel, date,
état (« active » ou « annulée »), et une action d'annulation lorsque celle-ci est autorisée.

**Cas d'erreur** :

| Condition | Réponse | Écriture |
|---|---|---|
| Aucun étudiant courant défini | Invitation à choisir un étudiant | Aucune |
| Aucune réservation | `200` avec message explicite, sans erreur technique | Aucune |

**Filtrage** : seules les réservations **de l'étudiant courant** apparaissent (FR-009). Les
réservations annulées **restent visibles**, avec leur état (FR-013).

**Note d'interface** : le bouton d'annulation peut être masqué lorsque l'annulation est
impossible (jour passé, déjà annulée). Ce masquage est un confort d'affichage : il **ne remplace
pas** le contrôle serveur, qui est refait à chaque requête (T08).

**Exigences** : FR-009, FR-013, FR-022.

---

## `POST /reservations/{id}/annulation`

**Rôle** : annuler une réservation appartenant à l'étudiant courant.

**Paramètres** :

| Nom | Emplacement | Type | Obligatoire | Description |
|---|---|---|---|---|
| `id` | chemin | entier | oui | Identifiant de la réservation à annuler |

**Paramètre interdit** : le formulaire d'annulation ne transporte **aucun** identifiant
d'étudiant. Le propriétaire est déterminé à partir de la session. C'est ce qui rend le scénario
T08 concluant même lorsqu'une requête est forgée (R-06, FR-010).

**Réponse en cas de succès** : `302` vers `/reservations`.

**Cas d'erreur** — tous produisent un message français et **aucune modification** :

| Condition | Code de refus | Réponse |
|---|---|---|
| Aucun étudiant courant défini | `ETUDIANT_NON_SELECTIONNE` | Invitation à choisir un étudiant |
| Réservation inexistante | `RESERVATION_INTROUVABLE` | Retour avec message explicite |
| Demandeur non propriétaire | `PAS_PROPRIETAIRE` | Retour avec message de refus, **réservation inchangée** |
| Réservation déjà annulée | `DEJA_ANNULEE` | Retour avec message indiquant son état, **aucune modification** |
| Jour réservé déjà passé | `JOUR_RESERVE_PASSE` | Retour avec message de refus |

**Ordre d'évaluation des contrôles** : existence → propriétaire → déjà annulée → date. Le contrôle
du propriétaire **précède** celui de l'état : un étudiant qui vise la réservation d'un autre
n'apprend rien sur son état.

**Annulation autorisée jusqu'au jour réservé inclus** (RG-06) : si la date réservée est égale à la
date courante, l'annulation est acceptée.

**Effet de l'annulation** : le statut devient `ANNULEE_PAR_ETUDIANT`, la colonne technique
`cleActive` passe à `NULL`, et **l'enregistrement est conservé** (RG-07, FR-013, FR-026).
L'application n'attribue jamais `ANNULEE_ADMINISTRATIVE` (FR-027).

**Exigences** : FR-010 à FR-014, FR-016, FR-020, FR-026, FR-027 · RG-05, RG-06, RG-07, RG-09.

---

## Récapitulatif de la traçabilité des scénarios de recette

| Scénario | Route mobilisée | Exigences vérifiées |
|---|---|---|
| T01 | `POST /reservations` | FR-005 |
| T02 | `POST /reservations` | FR-004, FR-006, FR-021 |
| T03 | `POST /reservations` | FR-004, FR-008 |
| T04 | `POST /reservations` | FR-007 |
| T05 | `POST /reservations` | FR-005, FR-008 |
| T06 | `POST /reservations/{id}/annulation` | FR-011, FR-013, FR-026 |
| T07 | `POST /reservations` | FR-014 |
| T08 | `POST /reservations/{id}/annulation` | FR-010 |
| T09 | redémarrage | FR-015 |
| T10 | `POST /reservations` | FR-016, FR-019 |
| T11 | `POST /reservations/{id}/annulation` | FR-013, FR-016 |
| T12 | `POST /reservations` | FR-021 |
