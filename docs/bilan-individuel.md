# Bilan individuel — modèle à compléter

**Livrable 7 de l'énoncé** | **Volume attendu : 300 à 500 mots**

> ## À lire avant de commencer
>
> **Un exemple rédigé est disponible : `docs/bilan-tom-tas.md`.** Il montre à quoi ressemble un
> bilan qui répond aux six questions avec des références précises au dépôt.
>
> Ce document-ci est un **modèle**. Il n'est **pas** un bilan : il ne contient aucune expérience
> personnelle, parce qu'aucun assistant ne peut écrire ce que vous avez vécu.
>
> **Chaque membre du binôme rédige son propre bilan.** Deux bilans identiques seront repérés, et
> un bilan qui ne parle pas de votre propre travail ne pourra pas être défendu à la
> démonstration finale.
>
> Le fichier rendu peut s'appeler `docs/bilan-<prénom>.md`. Les réponses doivent être courtes,
> concrètes et vérifiables : citer un fichier, un message, un test ou un numéro de décision est
> toujours plus convaincant qu'une appréciation générale.

---

## Consignes

| Contrainte | Valeur |
|---|---|
| Volume | 300 à 500 mots (le tableau ci-dessous n'est pas compté) |
| Nombre de questions | 6, toutes traitées |
| Preuves attendues | Au moins 3 références précises à des fichiers, décisions ou tests du dépôt |
| Ton | Factuel. Ce qui a mal tourné est plus utile que ce qui a bien marché |

---

## Les six questions

### Q1 — Quelle ambiguïté avez-vous identifiée ?

> *Pistes de recherche dans le dépôt : plusieurs documents du dossier se contredisent sur la
> pile technique ; trois questions n'étaient tranchées par aucune règle ; une expression
> comme « empêcher les doubles réservations » peut se comprendre de plusieurs façons.*

Votre réponse :

_(à compléter — précisez comment vous vous en êtes aperçu et ce que vous avez fait concrètement)_

### Q2 — Quelle proposition de l'IA avez-vous corrigée, et pourquoi ?

> *Matériau disponible : le tableau « Propositions de l'IA corrigées, refusées ou précisées »
> de `docs/journal-decisions.md` contient sept entrées. Choisissez-en une que **vous** avez
> contestée ou validée, et expliquez le raisonnement. Une proposition acceptée sans
> vérification n'est pas un bon exemple : cherchez plutôt celle où vous avez dû vérifier un
> fait.*

Votre réponse :

_(à compléter — indiquez la proposition, votre désaccord, et la vérification qui a tranché)_

### Q3 — Quelle partie du code pouvez-vous expliquer précisément ?

> *Matériau disponible : `ReservationService.reserver` et `annuler`, la colonne technique
> `cleActive`, `EtudiantCourantService`, `DonneesDemoInitialiseur`, `ClockConfiguration`.
> `docs/analyse-initiale.md` §6 contient un exemple d'analyse complète d'une fonction : ne
> recopiez pas celui-là, choisissez-en une autre.*

Votre réponse :

_(à compléter — entrez, sortie, cas d'erreur, test associé)_

### Q4 — Quel travail Spec Kit a-t-il facilité ?

> *Matériau disponible : la constitution (gabarit vide → 6 principes vérifiables) ; la
> spécification en 5 histoires qui a servi de base aux tests ; `tasks.md` ; les checklists ;
> la traçabilité. Comparez avec ce qui se serait passé sans ces documents.*

Votre réponse :

_(à compléter — soyez précis : quel document, pour quelle décision, quel gain réel)_

### Q5 — Quelle difficulté subsiste malgré les spécifications ?

> *Matériau disponible : la limite de deux réservations reste hors périmètre ; l'état
> `ANNULEE_ADMINISTRATIVE` est défini mais non atteignable ; l'identité reste une simulation
> sans authentification ; la vérification T09 est manuelle ; la spécification n'a pas empêché
> les quatre anomalies de rendu détectées par les tests.*

Votre réponse :

_(à compléter — une difficulté que la spécification ne pouvait pas lever)_

### Q6 — Dans quelles situations une exploration rapide par prompts vous paraît-elle utile ?

> *Comparez les deux modes : le parcours structuré suivi ici (besoin → spécification →
> clarification → plan → tâches → réalisation → vérification) et une exploration libre par
> prompts successifs. Cherchez ce que le second fait mieux, et à quelles conditions.*

Votre réponse :

_(à compléter — soyez nuancé : tout n'est pas à jeter dans l'exploration rapide, et tout n'est
pas à garder dans le formalisme)_

---

## Repères factuels du projet

Ces éléments peuvent servir de preuves, mais ne remplacent pas votre analyse.

| Fait | Où le vérifier |
|---|---|
| Deux énoncés du dossier imposent des piles techniques incompatibles | `develloper_une_app.md` §10 et `copilot.md` |
| La décision a été prise explicitement, pas silencieusement | `docs/journal-decisions.md` D-01 |
| L'identifiant `4.1.1.RELEASE` renvoyé par start.spring.io n'existait pas dans Maven Central | Journal D-02 |
| Sept propositions de l'IA ont été corrigées, refusées ou précisées | `docs/journal-decisions.md`, dernier tableau |
| 68 tests automatisés, dont un test par requête HTTP directe pour T08 | `docs/compte-rendu-recette.md` §1 et §2 |
| Six anomalies ont été détectées par exécution, aucune ne reste ouverte | `docs/compte-rendu-recette.md` §4 |
| Quatre anomalies ont été détectées par les tests, aucune ne reste ouverte | `docs/compte-rendu-recette.md` §4 |
| La protection contre les doublons repose sur deux niveaux, et la contrainte de base est prouvée par un test | `research.md` R-01 et `ConflitConcurrentTest` |
| Une décision du binôme a introduit du code non atteignable, assumé et borné | Décision CA-02 et test `jamaisAnnuleeAdministrative` |
| Les cases de la checklist des exigences sont volontairement laissées à cocher par le binôme | `specs/001-reservation-materiel/checklists/exigences.md` |

---

## Barème indicatif de relecture croisée

À utiliser entre membres du binôme avant de rendre : il ne s'agit pas de se noter, mais de
repérer ce qui manque.

| Critère | Vérifié ? |
|---|---|
| Les six questions sont traitées | ☐ |
| Le volume est compris entre 300 et 500 mots | ☐ |
| Au moins trois références précises (fichier, décision ou test) apparaissent | ☐ |
| Une proposition de l'IA est analysée avec le raisonnement qui a tranché | ☐ |
| Une partie du code est expliquée au niveau du détail (entrée, sortie, erreur, test) | ☐ |
| Une difficulté résiduelle est reconnue sans être minimisée | ☐ |
| Le texte pourrait être défendu à l'oral sans relire ses notes | ☐ |
