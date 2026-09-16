# Journal de décisions — Campus Matériel

Ce journal consigne les décisions importantes du projet et, en particulier, les propositions de
l'IA qui ont été **corrigées, refusées ou précisées** par le binôme. Le TP exige au moins trois
telles propositions.

Chaque décision indique : le contexte, la décision, la justification et les conséquences.

---

## D-01 — Ambiguïté sur la source de vérité technique

**Date :** 16 septembre 2026
**Contexte :** le dossier contient plusieurs documents dont deux se contredisent.

- `develloper_une_app.md` (énoncé principal du TP) impose, en section 10, la pile
  **Python + Flask + SQLite + HTML/CSS rendu serveur + pytest**.
- `copilot.md` décrit une variante **IntelliJ + Java 25 + Spring Boot 4.1.1 + Thymeleaf +
  Spring Data JPA + H2**.

Le TP principal est désigné comme document de référence par l'utilisateur, mais il contient une
contradiction interne avec la variante fournie dans le même dossier.

**Décision :** ne pas trancher silencieusement. L'ambiguïté a été signalée au binôme, qui a
choisi la **pile Java 25 / Spring Boot 4.1.1** après avoir vu les deux options.

**Justification :** en cas de contradiction entre documents, la règle de travail du projet impose
de signaler le conflit au binôme plutôt que de choisir seul. Le choix a été confirmé
explicitement.

**Conséquences :**

- Les principes, le plan et les tâches suivent la variante Spring Boot (voir `copilot.md`) :
  `Clock` injectable, séparation contrôleur / service / repository, session HTTP pour l'identité
  simulée, H2 en mode fichier pour l'application et en mémoire pour les tests.
- L'énoncé principal **reste le référentiel fonctionnel** : règles RG-01 à RG-09, situations
  limites imposées, scénarios de recette T01 à T12 et livrables de la section 15.
- Cette décision est rappelée dans le `README.md` afin qu'un relecteur ne voie pas une
  incohérence entre les documents.

---

## D-02 — Version du parent Maven corrigée

**Date :** 16 septembre 2026
**Contexte :** le squelette Spring Boot a été demandé à start.spring.io avec
`bootVersion=4.1.1.RELEASE`. La première compilation échoue :

```
Non-resolvable parent POM ... spring-boot-starter-parent:pom:4.1.1.RELEASE (absent)
```

**Cause :** les métadonnées de start.spring.io renvoient l'identifiant `4.1.1.RELEASE`, alors que
l'artefact réellement publié dans Maven Central est `4.1.1`. Le suffixe `.RELEASE` est un
vestige de l'ancienne convention de nommage.

**Décision :** corriger le `pom.xml` pour utiliser `<version>4.1.1</version>`.

**Justification :** vérification directe de la liste des versions publiées dans Maven Central,
qui contient `4.1.0`, `4.1.1` et `4.2.0-M1`, mais aucune version suffixée `.RELEASE`.

**Conséquences :** `.\mvnw.cmd test` passe de « Non-resolvable parent POM » à `BUILD SUCCESS`.
Cette correction est un exemple de proposition d'outillage acceptée après vérification, et non
sur parole.

---

## D-03 — JDK 25 : réutilisation de l'installation existante

**Date :** 16 septembre 2026
**Contexte :** la commande `java -version` du terminal renvoie `23.0.2`, et aucun gestionnaire de
paquets (`winget`, `scoop`, `choco`) n'est disponible pour installer Java 25. Maven n'est pas
installé non plus.

**Décision :** utiliser le JDK 25 déjà présent dans `C:\Users\tomta\.jdks\ms-25.0.4.1`
(Microsoft OpenJDK 25.0.4.1), téléchargé par IntelliJ, et le Maven Wrapper du projet plutôt
qu'un Maven global.

**Justification :** le projet exige Java 25 et le Maven Wrapper. Aucune installation
supplémentaire n'est nécessaire ; le JDK 25 est vérifié opérationnel
(`openjdk version "25.0.4.1"`).

**Conséquences :** `JAVA_HOME` doit pointer sur ce JDK pour que la compilation fonctionne.
Aucun Maven global ne doit être installé, conformément aux consignes du projet.

---

## D-04 — Initialisation de Spec Kit dans le dossier existant

**Date :** 16 septembre 2026
**Contexte :** le TP propose `specify init campus-materiel`, ce qui créerait un sous-dossier.
Le dossier courant contient déjà les documents du TP et doit accueillir le code.

**Décision :** initialiser Spec Kit dans le dossier courant avec
`specify init . --integration copilot --script ps`.

**Justification :** évite de dupliquer les documents sources et de maintenir deux dépôts Git.
L'option `--force` est nécessaire car le dossier n'est pas vide.

**Conséquences :** les commandes Spec Kit sont disponibles sous forme de *skills* dans
`.github/skills/` avec la syntaxe `/speckit-constitution`, `/speckit-specify`, etc. Cette
syntaxe diffère de la notation `/speckit.nom` utilisée dans l'énoncé, qui correspond à une
version antérieure de Spec Kit. La correspondance est sans ambiguïté : `speckit-specify`
correspond à `/speckit.specify`.

---

## D-05 — Exclusions Git adaptées

**Date :** 16 septembre 2026
**Contexte :** l'énoncé demande de configurer les exclusions avant le premier commit. Le
squelette Spring Boot fournit son propre `.gitignore`, qui ignore les fichiers de l'IDE mais
laisse `data/` versionnable.

**Décision :** fusionner les deux listes et ajouter explicitement `data/`, ainsi que les
exclusions de l'outillage Python utilisé par Spec Kit.

**Justification :** le dossier `data/` contient la base H2 locale. Le versionner ferait entrer
des données d'exécution dans le dépôt et rendrait les tests de persistance T09 dépendants de
l'état local d'un poste. Le fichier `.gitattributes` normalise les fins de ligne pour éviter les
avertissements à chaque `git add`.

**Conséquences :** `git status` reste propre après un lancement de l'application.

---

## D-06 — Message distinct en cas de re-réservation par le même étudiant

**Date :** 16 septembre 2026
**Contexte :** l'énoncé ne précise pas le message à afficher lorsqu'un étudiant demande un
matériel qu'il a lui-même déjà réservé à la même date. Le refus est certain (RG-02) ; seul le
message est ambigu.

**Décision :** afficher un message distinct indiquant que l'étudiant a déjà réservé ce matériel
pour cette date, plutôt que le message générique d'indisponibilité.

**Justification :** l'étudiant comprend plus vite la situation et n'a pas à deviner s'il s'est
trompé de matériel ou de date. Le coût est limité : une exigence (FR-025) et un test.

**Conséquences :** FR-025 ajoutée à la spécification ; scénario 7 de l'histoire 3 précisé ; un
test doit vérifier que les deux messages sont bien distincts.

---

## D-07 — L'état d'une réservation distingue l'origine de l'annulation

**Date :** 16 septembre 2026
**Contexte :** le modèle minimal de l'énoncé ne mentionne que « état ». La solution de référence
proposée par l'assistant (`copilot.md`) définit exactement deux valeurs : `ACTIVE` et `ANNULEE`.
Aucune règle de l'énoncé ne demande de distinguer l'origine d'une annulation.

**Décision :** retenir **trois** états — active, annulée par l'étudiant, annulée par
l'administration — et documenter explicitement que le troisième n'est atteignable par aucune
fonctionnalité de cette version.

**Justification :** décision explicite du binôme, qui souhaite que le modèle d'état n'ait pas à
être modifié lorsqu'un traitement administratif sera ajouté. Le principe VI de la constitution
autorise un ajout hors périmètre **à condition d'une décision explicite et consignée**, ce qui
est le cas ici.

**Conséquences et risque assumé :**

- FR-026 et FR-027 ajoutées à la spécification.
- L'état « annulée par l'administration » est **du code non atteignable**. Ce point est assumé
  et borné : aucune route, aucun formulaire et aucun service d'annulation administrative ne sont
  créés. FR-027 transforme cette limite en exigence vérifiable, qui sera couverte par un test
  garantissant que l'application ne produit jamais cet état.
- Cette décision **s'écarte de la solution de référence** de l'assistant. Elle est donc
  journalisée comme proposition corrigée (voir tableau ci-dessous, entrée 6).

---

## D-08 — La limite de deux réservations actives par jour reste hors périmètre

**Date :** 16 septembre 2026
**Contexte :** l'énoncé réserve cette règle à l'étape « évolution », après la recette. Il serait
tentant de l'implémenter immédiatement pour éviter une seconde passe.

**Décision :** ne pas l'implémenter. La limite est confirmée hors périmètre et tracée
explicitement comme telle.

**Justification :** le déroulé pédagogique attendu est « évolution du besoin → modification de la
spécification → répercussion sur le plan et les tâches → implémentation ». L'implémenter
maintenant ferait disparaître l'exercice et priverait le binôme du test d'un point d'évaluation.
De plus, l'énoncé exige de ne pas conserver deux formulations contradictoires de RG-04 : la
reformulation doit être faite au moment de l'évolution.

**Conséquences :** la limite figure dans les exclusions du périmètre de la spécification et dans
ce journal. L'étape « évolution » commencera par la reformulation de RG-04.

---

## D-09 — Proposition de l'IA refusée : implémenter la limite des deux réservations

**Date :** 16 septembre 2026
**Contexte :** lors de la rédaction de la spécification, l'assistant a proposé d'intégrer
directement la limite de deux réservations actives par étudiant et par jour, au motif qu'elle
« simplifierait » la validation.

**Décision :** refusée. La proposition contredit l'énoncé, qui place cette règle après la
recette, et aurait fait disparaître l'étape « évolution » du TP.

**Justification :** une proposition techniquement raisonnable peut être pédagogiquement fausse.
Le rôle du binôme est de contrôler la conformité au périmètre, pas seulement la faisabilité.

---

## D-10 — Analyse de cohérence : trois écarts détectés puis corrigés

**Date :** 16 septembre 2026
**Contexte :** avant la livraison, une analyse de cohérence a été menée entre la spécification,
le plan, les tâches et le code réellement écrit, conformément à l'étape `/speckit-analyze`.

**Écarts détectés :**

| # | Écart | Gravité | Correction |
|---|---|---|---|
| 1 | `spec.md` (SC-005) exigeait la couverture de « FR-001 à FR-024 », alors que la clarification CA-01 et CA-02 avait ajouté FR-025 à FR-027. Le critère de succès était donc en retard sur la spécification. | Moyenne | Plage corrigée en « FR-001 à FR-027 » |
| 2 | `plan.md` annonçait « 6 routes », alors que le contrat et le code en comptent 7. | Faible | Corrigé en « 7 routes HTTP (dont une redirection) » |
| 3 | La spécification indiquait que la décision CA-03 était « à tracer dans le journal », alors qu'elle l'était déjà (D-08). | Faible | Renvoi explicite vers D-08 |
| 4 | Les tâches T001 à T039 étaient toutes cochées « à faire » alors qu'elles étaient réalisées et vérifiées. | Moyenne | Cases mises à jour |

**Décision :** corriger les quatre écarts plutôt que de les contourner.

**Leçon retenue :** les écarts 1 et 4 sont apparus **après** l'ajout de nouvelles exigences en
cours de route. Une exigence ajoutée par une décision de clarification doit être reportée non
seulement là où on la rédige, mais partout où un intervalle ou un décompte la mentionne. C'est
le risque documentaire le plus fréquent dans ce projet : le texte se contredit avec lui-même
lorsqu'un compte d'exigences reste figé à sa valeur initiale.

**Portée du contrôle :** aucun écart n'a été trouvé sur le code lui-même. Les règles RG-01 à
RG-09 et les exigences FR-001 à FR-027 sont toutes reliées à une tâche et à une vérification
(voir `docs/matrice-tracabilite.md`).

---

## D-11 — Deux fichiers de code non atteignables ou non utilisés, assumés

**Date :** 16 septembre 2026
**Contexte :** l'analyse de cohérence a relevé deux cas de code qui n'est pas exercé par
l'application en fonctionnement.

**Cas concernés :**

1. `StatutReservation.ANNULEE_ADMINISTRATIVE` : aucune fonctionnalité ne peut produire cet état
   (décision CA-02).
2. `ReservationRepository.countByEtudiantIdAndDateReservationAndStatut` : aucune règle de cette
   version ne l'appelle ; elle est préparée pour la limite de deux réservations.

**Décision :** conserver les deux, et le documenter.

**Justification :** le principe VI de la constitution autorise un ajout hors périmètre sur
décision explicite et consignée. Pour le premier cas, FR-027 transforme la limite en exigence
vérifiable et un test garantit que l'application ne produit jamais cet état. Pour le second,
le choix est assumé et réversible : `data-model.md` indique explicitement que la méthode peut
être supprimée sans gêner l'étape d'évolution.

**Conséquence :** ces deux éléments sont signalés dans la documentation pour qu'un relecteur ne
les découvre pas avec surprise. Ils sont comptés comme des limites connues dans le compte rendu
de recette.

---

## D-12 — Convergence : quatre écarts entre le contrat et le code

**Date :** 16 septembre 2026
**Contexte :** après la recette, une comparaison systématique a été menée entre le code
réellement écrit et les documents de conception (étape `/speckit-converge`).

**Écarts détectés :**

| # | Écart | Nature | Correction |
|---|---|---|---|
| 1 | `ReservationRepository.findByDateReservationAndStatut` était déclarée, documentée comme utile à FR-004, et **jamais appelée** | Code mort + affirmation fausse dans `data-model.md` | Méthode supprimée ; le tableau des requêtes du modèle de données corrigé |
| 2 | `MaterielDisponibilite.getLibelleDisponibilite()` était déclarée et jamais appelée : les libellés « Disponible » et « Réservé » étaient **écrits en dur dans le gabarit**, alors que le contrat des messages les définit | Duplication d'un texte du contrat en deux endroits | Le gabarit affiche désormais la valeur du modèle ; une reformulation ne peut plus en oublier un exemplaire |
| 3 | `MessagesRefusService.listeMaterielsVide()` était déclarée et jamais appelée : la phrase « Aucun matériel n'est enregistré » était **écrite en dur dans le gabarit** | Même duplication que le cas 2 | Le contrôleur place le message dans le modèle ; le gabarit l'affiche |
| 4 | `MotifRefus.ETUDIANT_INCONNU` et son message existaient dans le code mais **absents du contrat des messages** | Contrat incomplet | Ligne ajoutée à `contracts/messages.md` |

**Décision :** corriger les quatre écarts dans le code et les documents, plutôt que de mettre à
jour la documentation pour qu'elle décrive ce que le code fait.

**Justification :** dans les quatre cas, c'est le **contrat** qui décrit l'intention, et le code
qui s'en écartait. Aligner la documentation sur le code aurait fait perdre l'intention et
affaibli la portée du contrat.

**Leçon retenue :** les écarts 2 et 3 portent sur des **textes affichés définis à deux endroits**.
Un texte dupliqué est un texte qui divergera : un des deux exemplaires sera modifié et pas
l'autre. La règle appliquée est désormais : un texte affiché n'existe qu'à un seul endroit du
code.

**Écart de méthode relevé au passage :** le test écrit pour couvrir FR-022 (liste de matériel
vide) a d'abord **fait échouer 37 autres tests**, parce qu'il vidait la table du matériel sans la
restaurer — tous les tests partagent la même base en mémoire, et l'initialiseur ne s'exécute
qu'au démarrage du contexte. Le test a été corrigé par une restauration explicite dans un bloc
`finally`. Ce défaut n'a été visible que parce que la suite complète a été relancée après l'ajout
du test ; il serait passé inaperçu si le test avait été vérifié isolément.

---

## Propositions de l'IA corrigées, refusées ou précisées

| # | Proposition initiale | Décision du binôme | Motif |
|---|---|---|---|
| 1 | Utiliser la pile Python/Flask de l'énoncé principal | Refusée après arbitrage | Le binôme a choisi Java/Spring Boot ; l'ambiguïté a été signalée puis tranchée explicitement (D-01) |
| 2 | Conserver `4.1.1.RELEASE` comme version du parent Maven issu de start.spring.io | Corrigée en `4.1.1` | Vérification dans Maven Central : la version suffixée `.RELEASE` n'existe pas (D-02) |
| 3 | Installer un Maven global et un JDK 25 supplémentaire | Refusée | Un JDK 25 est déjà présent et le Maven Wrapper suffit ; les consignes du projet imposent le Wrapper (D-03) |
| 4 | Créer le projet dans un sous-dossier `campus-materiel/` | Corrigée | Le dossier courant contient déjà les documents ; un sous-dossier aurait dupliqué le dépôt (D-04) |
| 5 | Conserver le `.gitignore` généré tel quel | Précisée | Ajout de `data/` et de l'outillage Python ; sans quoi la base locale aurait été versionnée (D-05) |
| 6 | Modèle de réservation à deux états `ACTIVE` / `ANNULEE` (solution de référence) | Précisée par décision du binôme | Le binôme veut distinguer l'origine de l'annulation, avec un troisième état non atteignable et borné par FR-027 (D-07) |
| 7 | Intégrer dès maintenant la limite de deux réservations actives par jour | Refusée | Contredit le déroulé de l'énoncé, qui place cette règle à l'étape « évolution » (D-09) |
| 8 | Annoncer « 6 routes » et « FR-001 à FR-024 » sans revérifier après les clarifications | Corrigée | L'analyse de cohérence a relevé deux décomptes restés figés avant l'ajout de FR-025 à FR-027 (D-10) |
| 9 | Laisser les cases des tâches à « à faire » alors qu'elles étaient réalisées | Corrigée | Un suivi d'avancement faux est une incohérence documentaire : les cases ont été mises à jour (D-10) |
| 10 | Affirmer dans le modèle de données qu'une méthode servait à FR-004 alors qu'elle n'était jamais appelée | Corrigée | Méthode supprimée et document corrigé (D-12) |
| 11 | Écrire les libellés « Disponible » / « Réservé » et le message de liste vide en dur dans le gabarit | Corrigée | Les textes proviennent du modèle, un seul exemplaire par texte (D-12) |

*Ce tableau est complété au fil du projet.*
