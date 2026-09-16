# Guide de validation : Réservation de matériel pédagogique

**Fonctionnalité** : `001-reservation-materiel` | **Date** : 2026-09-16

Ce guide permet de vérifier, par l'exécution, que la fonctionnalité se comporte comme la
spécification le décrit. Il ne contient ni code d'implémentation ni tests complets : ces éléments
appartiennent à `tasks.md` et à la phase d'implémentation.

---

## Prérequis

| Élément | Valeur attendue |
|---|---|
| Java | 25 (`java -version`) |
| JDK utilisé par Maven | 25 |
| Maven | fourni par le Wrapper du dépôt (aucun Maven global requis) |
| Port | `8080`, libre |

Sous Windows, si le `java` du PATH n'est pas la version 25, définir `JAVA_HOME` avant toute
commande Maven :

```powershell
$env:JAVA_HOME = "C:\Users\<utilisateur>\.jdks\ms-25.0.4.1"
```

---

## Commandes de référence

| Objectif | Commande |
|---|---|
| Compiler et exécuter les tests | `.\mvnw.cmd test` |
| Lancer l'application | `.\mvnw.cmd spring-boot:run` |
| Réinitialiser les données locales | arrêter l'application puis `Remove-Item -Recurse -Force data` |

L'application écoute uniquement sur `127.0.0.1:8080`.

---

## Vérification rapide (5 minutes)

### É1 — L'application démarre et les données fictives sont présentes

1. Lancer `.\mvnw.cmd spring-boot:run`.
2. Ouvrir <http://127.0.0.1:8080/materiels>.

**Attendu** : la page affiche les cinq équipements (`MAT-001` à `MAT-005`) avec leur catégorie, et
chacun est indiqué « Disponible ». Aucune trace d'exécution n'est visible.

### É2 — L'étudiant courant peut être choisi

1. Ouvrir <http://127.0.0.1:8080/etudiants/selection>.
2. Choisir « Alice Martin ».

**Attendu** : les trois étudiants fictifs sont proposés ; après le choix, l'application revient à
la page des disponibilités et Alice Martin est l'étudiante courante.

### É3 — Une réservation est créée

1. Depuis la page des disponibilités, choisir une date future (par exemple le 12 mars 2030).
2. Réserver `MAT-001`.

**Attendu** : un message de confirmation nomme la date réservée ; `MAT-001` apparaît ensuite
« Réservé » pour cette date, et les autres matériels restent « Disponible ».

---

## Scénarios de recette

**Cadre d'exécution** : base de test réinitialisée, date courante fixée au **10 mars 2030**.
Dans l'application lancée normalement, la date courante est la date réelle ; les scénarios
T04, T06 et T11 se vérifient donc par les **tests automatisés**, qui fixent l'horloge. La
vérification manuelle n'est pertinente que pour les scénarios sans dépendance à la date.

Les scénarios sont indépendants, sauf lorsqu'une préparation est indiquée.

| Test | Préparation | Action | Résultat attendu | Vérification automatisée |
|---|---|---|---|---|
| T01 | — | Alice réserve MAT-001 pour le 12 mars | Réservation active créée | Oui |
| T02 | MAT-001 réservé le 12 par Alice | Bilal demande MAT-001 pour le 12 | Refus ; aucun doublon | Oui |
| T03 | MAT-001 réservé le 12 | Bilal le demande pour le 13 | Réservation acceptée | Oui |
| T04 | — | Alice demande MAT-002 pour le 9 mars | Refus ; aucune création | Oui |
| T05 | — | Alice réserve MAT-001 et MAT-002 pour le 12 | Deux réservations acceptées | Oui |
| T06 | Réservation d'Alice le 12 | Alice annule | État annulé ; historique conservé | Oui |
| T07 | Après T06 | Bilal réserve le même matériel le 12 | Réservation acceptée | Oui |
| T08 | Réservation d'Alice | Bilal tente de l'annuler, y compris par requête directe | Refus ; réservation inchangée | Oui |
| T09 | Une réservation existe | Arrêter puis relancer l'application | Réservation toujours présente | **Manuelle** (voir ci-dessous) |
| T10 | — | Date absente ou invalide | Message explicite ; aucune création | Oui |
| T11 | Réservation annulée | Alice annule à nouveau | Aucun changement ; message adapté | Oui |
| T12 | — | Deux demandes sur le même matériel à la même date | Au plus une réservation active | Oui |

### Procédure manuelle T09 — persistance après redémarrage

Ce scénario ne peut pas être vérifié par un test qui redémarre réellement l'application. Il est
donc conduit manuellement, de façon reproductible.

1. Supprimer le dossier `data` pour partir d'un état connu.
2. Lancer l'application (`.\mvnw.cmd spring-boot:run`).
3. Sélectionner Alice Martin.
4. Réserver `MAT-003` pour une date future.
5. **Arrêter** l'application (Ctrl+C dans le terminal).
6. **Relancer** l'application.
7. Sélectionner de nouveau Alice Martin, puis consulter ses réservations.

**Attendu** : la réservation de `MAT-003` est toujours présente, avec son état.

**Résultat observé** : _(à compléter lors de la recette)_

**Statut** : _(à compléter : conforme / non conforme)_

**Anomalie éventuelle** : _(à compléter)_

### Vérification de T08 par requête directe

Masquer un bouton dans la page ne prouve rien : un client peut envoyer une requête forgée. La
vérification doit donc contourner l'interface.

1. Sélectionner Alice et créer une réservation ; noter son identifiant.
2. Sélectionner ensuite Bilal (l'étudiant courant change).
3. Adresser directement au serveur une demande d'annulation de la réservation d'Alice, sans
   passer par le bouton de la page.

**Attendu** : l'annulation est refusée ; la réservation d'Alice reste active.

**Résultat observé** : _(à compléter lors de la recette)_

---

## Points de contrôle issus de la constitution

| Principe | Comment le vérifier |
|---|---|
| I. Lisibilité | Ouvrir un contrôleur et `ReservationService` : le comportement doit être explicable sans outil d'analyse |
| II. Séparation des responsabilités | Aucune condition métier dans un contrôleur ou un template Thymeleaf |
| III. Validation côté serveur | Provoquer un refus par requête directe, sans passer par l'interface |
| IV. Traçabilité | Chaque test porte la référence de l'exigence qu'il vérifie |
| V. Persistance et temps | Le dossier `data` apparaît après le premier lancement ; les tests utilisent une base en mémoire |
| VI. Périmètre | Aucune route ni page d'administration n'existe |

---

## Critères de validation finale

| Référence | Critère | Comment le constater |
|---|---|---|
| SC-001 | Consulter les disponibilités d'une date en une action, sans saisie de texte | Ouvrir `/materiels`, choisir une date |
| SC-002 | Jamais plus d'une réservation active sur un même couple (matériel, date) | Exécuter T02 et T12 |
| SC-003 | 100 % des réservations survivent au redémarrage | Exécuter la procédure T09 |
| SC-004 | Les 12 scénarios produisent le résultat attendu | Tableau de recette complété |
| SC-005 | Les 27 exigences sont couvertes et tracées | Matrice de traçabilité |
| SC-006 | Toute saisie invalide produit un message compréhensible, sans écriture | Exécuter T10 et T11 |
| SC-007 | Un équipement annulé redevient réservable | Exécuter T06 puis T07 |

---

## Résultats de recette

Le compte rendu détaillé (résultat obtenu, statut, anomalie éventuelle pour chaque test) est
consigné dans `docs/compte-rendu-recette.md` après exécution. Ce guide décrit **comment** vérifier ;
il ne préjuge pas du résultat.
