# Contrat des messages : Réservation de matériel pédagogique

**Fonctionnalité** : `001-reservation-materiel` | **Date** : 2026-09-16

Ce document fixe les messages affichés à l'utilisateur. Il rend RG-09 (« message compréhensible »)
vérifiable : un message attendu peut être comparé à un message réellement produit.

**Règle générale** : aucun message affiché à l'utilisateur ne contient de terme technique (nom de
table, nom de classe, trace d'exécution, code d'erreur brut). Les messages sont rédigés en
français, sans jargon.

---

## Codes de refus et messages associés

Le service ne renvoie pas de texte libre : il lève une erreur portant un **code**. Le texte est
choisi à partir de ce code en un point unique de l'application. Les tests portent sur le **code**,
pas sur la formulation, afin qu'une reformulation ne casse pas les tests.

| Code | Message affiché | Écriture | Exigences |
|---|---|---|---|
| `DATE_INVALIDE` | « La date saisie est invalide. Utilisez le format année-mois-jour. » | Aucune | FR-016, FR-019 |
| `ETUDIANT_NON_SELECTIONNE` | « Veuillez d'abord choisir un étudiant. » | Aucune | FR-017 |
| `ETUDIANT_INCONNU` | « Cet étudiant n'existe pas. » | Aucune | FR-001, FR-016 |
| `MATERIEL_INCONNU` | « Ce matériel n'existe pas. » | Aucune | FR-018 |
| `DATE_PASSEE` | « La date choisie est déjà passée. Choisissez aujourd'hui ou une date future. » | Aucune | FR-007, FR-016 |
| `MATERIEL_INDISPONIBLE` | « Ce matériel est déjà réservé à cette date. » | Aucune | FR-006, FR-021 |
| `DEJA_RESERVE_PAR_VOUS` | « Vous avez déjà réservé ce matériel pour cette date. » | Aucune | FR-025 |
| `RESERVATION_INTROUVABLE` | « Cette réservation n'existe pas. » | Aucune | FR-020 |
| `PAS_PROPRIETAIRE` | « Vous ne pouvez annuler que vos propres réservations. » | Aucune | FR-010 |
| `JOUR_RESERVE_PASSE` | « La date de cette réservation est passée : elle ne peut plus être annulée. » | Aucune | FR-012 |
| `DEJA_ANNULEE` | « Cette réservation est déjà annulée. » | Aucune | CL-01, FR-013 |

**Distinction imposée par la décision CA-01** : `MATERIEL_INDISPONIBLE` et
`DEJA_RESERVE_PAR_VOUS` produisent **deux messages différents** pour un même refus. Un test doit
vérifier que les deux codes sont bien distincts selon le propriétaire de la réservation
existante.

---

## Messages de succès

| Situation | Message | Exigences |
|---|---|---|
| Réservation créée | « Réservation enregistrée pour le <date>. » | FR-005 |
| Annulation effectuée | « Réservation annulée. Le matériel est de nouveau disponible à cette date. » | FR-013, FR-014 |

---

## Messages d'état vide

Aucun de ces messages ne doit s'accompagner d'une erreur technique (CL-05, FR-022).

| Situation | Message | Exigences |
|---|---|---|
| Aucun matériel à afficher | « Aucun matériel n'est enregistré. » | FR-022 |
| Aucune réservation pour l'étudiant courant | « Vous n'avez aucune réservation pour le moment. » | FR-022 |
| Aucun étudiant courant défini, sur la page des réservations | « Choisissez un étudiant pour consulter ses réservations. » | FR-017 |

---

## Libellés de disponibilité

| Situation | Libellé | Exigences |
|---|---|---|
| Aucune réservation active à la date consultée | « Disponible » | FR-003, FR-004 |
| Une réservation active existe à cette date | « Réservé » | FR-004 |
| Réservation annulée à cette date | Le matériel reste « Disponible » | RG-07, FR-014 |

Le libellé « Réservé » est volontairement neutre : il **n'indique pas qui** a réservé le
matériel. Afficher le nom d'un autre étudiant ne serait demandé par aucune règle de l'énoncé et
exposerait une information sur une personne à un utilisateur qui n'est pas authentifié.

Ces deux libellés sont produits par le modèle de vue `MaterielDisponibilite` et **non** dans le
gabarit : le texte affiché ne doit exister qu'à un seul endroit du code, faute de quoi une
reformulation en oublierait un exemplaire.

---

## Libellés d'état d'une réservation

| Statut technique | Libellé affiché | Exigences |
|---|---|---|
| `ACTIVE` | « Active » | FR-013 |
| `ANNULEE_PAR_ETUDIANT` | « Annulée » | FR-013, FR-026 |
| `ANNULEE_ADMINISTRATIVE` | « Annulée » | FR-026, FR-027 |

**Note** : l'état `ANNULEE_ADMINISTRATIVE` n'est produit par aucune fonctionnalité de cette
version. Le libellé est défini pour que l'affichage reste correct si un tel état apparaissait
dans les données, mais aucune action de l'application ne le crée (FR-027).

---

## Messages de validation de formulaire

Produits par les contraintes de validation, avant tout appel au service.

| Champ | Condition | Message | Exigences |
|---|---|---|---|
| Date de réservation | Absente | « La date de réservation est obligatoire. » | FR-019 |
| Matériel | Absent | « Le matériel est obligatoire. » | FR-016 |
| Étudiant | Aucun sélectionné | « Veuillez choisir un étudiant. » | FR-001 |

---

## Ce que l'application ne doit jamais afficher

| Interdit | Motif |
|---|---|
| Une trace d'exécution Java | CL-05, RG-09 |
| Un nom de table, de colonne ou de classe | CL-05, RG-09 |
| Un code d'erreur brut seul, sans phrase compréhensible | RG-09 |
| Le détail d'une contrainte de base de données | RG-09 |
| Le nom d'un étudiant autre que l'étudiant courant dans la page des réservations | Pas demandé par l'énoncé ; information personnelle exposée sans authentification |
