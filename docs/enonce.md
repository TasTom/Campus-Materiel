# Énoncé — Campus Matériel

Document de référence du projet. Il consolide le besoin, les données et les règles métier.
Toute décision prise avec l'assistant doit être répercutée ici ou dans les documents Spec Kit,
jamais conservée uniquement dans une conversation.

## 1. Contexte

Le département informatique prête du matériel aux étudiants : ordinateurs portables,
vidéoprojecteurs et kits électroniques.

Les réservations sont actuellement inscrites dans un tableau partagé. Des doublons apparaissent
et certaines annulations ne sont pas prises en compte.

L'application doit permettre de :

1. consulter le matériel disponible ;
2. réserver un équipement pour une journée ;
3. consulter les réservations d'un étudiant ;
4. annuler une réservation ;
5. empêcher les doubles réservations.

## 2. Périmètre de la première version

- L'application utilise trois étudiants fictifs préenregistrés.
- Une liste permet de sélectionner l'étudiant courant.
- **Cette sélection simule une identité pour le TP : elle ne constitue pas une authentification.**
- L'application fonctionne localement avec des données fictives.
- Chaque équipement représente un objet physique unique : deux ordinateurs identiques ont deux
  identifiants différents.

### Fonctionnalités exclues de cette version

- les comptes et mots de passe ;
- les courriels et notifications ;
- la gestion des retards et des pénalités ;
- les réservations sur plusieurs jours ;
- les réservations par créneau horaire ;
- l'ajout ou la suppression de matériel par une interface d'administration.

## 3. Jeu de données initial

### Matériel

| Identifiant | Équipement | Catégorie |
|---|---|---|
| MAT-001 | Ordinateur portable A | Informatique |
| MAT-002 | Ordinateur portable B | Informatique |
| MAT-003 | Vidéoprojecteur A | Projection |
| MAT-004 | Kit Arduino A | Électronique |
| MAT-005 | Kit Arduino B | Électronique |

### Étudiants fictifs

- Alice Martin
- Bilal Dupont
- Chloé Bernard

## 4. Règles métier obligatoires

| Référence | Règle |
|---|---|
| RG-01 | Une réservation concerne un étudiant, un équipement et une date. |
| RG-02 | Un équipement ne peut avoir qu'une réservation active pour une même date. |
| RG-03 | Une réservation peut concerner aujourd'hui ou une date future, jamais une date passée. |
| RG-04 | Un étudiant peut réserver plusieurs équipements pour une même journée, **dans la limite de deux réservations actives par journée**. |
| RG-05 | L'étudiant courant ne peut annuler que ses propres réservations. |
| RG-06 | Une réservation active peut être annulée jusqu'au jour réservé inclus. |
| RG-07 | Une annulation conserve l'historique et libère l'équipement pour la date concernée. |
| RG-08 | Les réservations sont conservées après redémarrage de l'application. |
| RG-09 | Une entrée invalide entraîne un message compréhensible et aucune modification des données. |

« Aujourd'hui » désigne la date du serveur local. Pour les tests automatiques, cette date doit
pouvoir être fixée artificiellement.

## 5. Situations limites et décisions attendues

| Situation | Décision attendue |
|---|---|
| Nouvelle annulation d'une réservation déjà annulée | Aucune modification ; message indiquant son état |
| Réservation d'un matériel inconnu | Refus ; aucune création |
| Date absente ou mal formée | Refus avec explication |
| Annulation d'une réservation passée | Refus |
| Liste sans résultat | Message explicite, sans erreur technique |
| Annulation par un étudiant qui n'est pas le propriétaire | Refus ; réservation inchangée, y compris si la requête est adressée directement au serveur |
| Troisième réservation active demandée pour une même journée | Refus avec message indiquant la limite atteinte ; aucune création |
| Réservation demandée pour un jour où la limite est atteinte, mais sur un matériel déjà occupé | Refus pour indisponibilité du matériel : inutile d'annoncer la limite si l'action demandée ne peut pas aboutir |

## 6. Scénarios de recette

Base de test réinitialisée, date courante fixée au **10 mars 2030**.
Les scénarios sont indépendants, sauf lorsqu'une préparation est indiquée.

| Test | Action et préparation | Résultat attendu |
|---|---|---|
| T01 | Alice réserve MAT-001 pour le 12 mars | Réservation active créée |
| T02 | MAT-001 est déjà réservé le 12 ; Bilal le demande pour le 12 | Refus ; aucun doublon |
| T03 | MAT-001 est réservé le 12 ; Bilal le demande pour le 13 | Réservation acceptée |
| T04 | Alice demande MAT-002 pour le 9 mars | Refus ; aucune création |
| T05 | Alice réserve MAT-001 et MAT-002 pour le 12 | Deux réservations acceptées |
| T06 | Alice annule sa réservation du 12 | État annulé ; historique conservé |
| T07 | Après cette annulation, Bilal réserve le même matériel le 12 | Réservation acceptée |
| T08 | Bilal tente d'annuler une réservation d'Alice | Refus ; réservation inchangée |
| T09 | Une réservation existe ; l'application redémarre | Réservation toujours présente |
| T10 | La date est absente ou invalide | Message explicite ; aucune création |
| T11 | Alice annule une réservation déjà annulée | Aucun changement ; message adapté |
| T12 | Deux demandes visent le même matériel et la même date | Au plus une réservation active |
| T13 | Alice a deux réservations actives le 12 ; elle demande un troisième matériel pour le 12 | Refus ; message indiquant la limite ; aucune création |
| T14 | Préparation : Alice a deux réservations actives le 12 ; elle en annule une, puis demande un autre matériel pour le 12 | Réservation acceptée |
| T15 | Préparation : Alice a deux réservations actives le 12 ; elle demande un matériel pour le 13 | Réservation acceptée : la limite porte sur une journée |

**Tests automatiques obligatoires : T01 à T08 et T10 à T15.**
T09 est vérifié manuellement avec une procédure reproductible.

## 7. Évolution du besoin — appliquée

Le département a formulé une demande complémentaire :

> Un étudiant ne peut pas avoir plus de deux réservations actives pour une même journée.

**Cette demande est désormais intégrée à l'application.** Elle a été traitée dans l'ordre imposé :
reformulation de la règle existante (RG-04 ci-dessus), ajout des critères d'acceptation, mise à jour
du plan et des tâches, puis implémentation et vérification.

La règle **porte sur les réservations actives** : deux réservations annulées ne consomment pas le
quota, ce qui garantit qu'une annulation rend immédiatement une possibilité de réservation.

Cette évolution est tracée dans `docs/journal-decisions.md` (D-13) et couverte par les scénarios
T13 à T15.
