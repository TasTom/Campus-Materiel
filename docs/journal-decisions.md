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

## Propositions de l'IA corrigées, refusées ou précisées

| # | Proposition initiale | Décision du binôme | Motif |
|---|---|---|---|
| 1 | Utiliser la pile Python/Flask de l'énoncé principal | Refusée après arbitrage | Le binôme a choisi Java/Spring Boot ; l'ambiguïté a été signalée puis tranchée explicitement (D-01) |
| 2 | Conserver `4.1.1.RELEASE` comme version du parent Maven issu de start.spring.io | Corrigée en `4.1.1` | Vérification dans Maven Central : la version suffixée `.RELEASE` n'existe pas (D-02) |
| 3 | Installer un Maven global et un JDK 25 supplémentaire | Refusée | Un JDK 25 est déjà présent et le Maven Wrapper suffit ; les consignes du projet imposent le Wrapper (D-03) |
| 4 | Créer le projet dans un sous-dossier `campus-materiel/` | Corrigée | Le dossier courant contient déjà les documents ; un sous-dossier aurait dupliqué le dépôt (D-04) |
| 5 | Conserver le `.gitignore` généré tel quel | Précisée | Ajout de `data/` et de l'outillage Python ; sans quoi la base locale aurait été versionnée (D-05) |

*Ce tableau est complété au fil du projet.*
