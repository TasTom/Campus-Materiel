# Campus Matériel

Application de réservation de matériel pédagogique pour le département informatique.

TP BUT Informatique 2ᵉ année — développement piloté par les spécifications avec GitHub Spec Kit.

## Objectif

Le département prête du matériel aux étudiants : ordinateurs portables, vidéoprojecteurs et
kits électroniques. Les réservations sont aujourd'hui inscrites dans un tableau partagé, ce qui
provoque des doublons et des annulations non prises en compte.

L'application permet de :

1. consulter le matériel disponible ;
2. réserver un équipement pour une journée ;
3. consulter les réservations d'un étudiant ;
4. annuler une réservation ;
5. empêcher les doubles réservations.

Le besoin complet, les règles métier RG-01 à RG-09 et les scénarios de recette T01 à T12 sont
décrits dans [`docs/enonce.md`](docs/enonce.md).

## Versions utilisées

| Outil | Version |
|---|---|
| Java | 25.0.4.1 (Microsoft OpenJDK) |
| Spring Boot | 4.1.1 |
| Maven Wrapper | 3.9.16 |
| H2 | version gérée par Spring Boot |
| GitHub Spec Kit | 1.0.7 |
| Python (outillage Spec Kit uniquement) | 3.11.6 |
| uv | 0.12.13 |

**Spec Kit n'est pas une dépendance Maven.** C'est un outil de développement qui ajoute au
dépôt les fichiers utilisés par l'agent (`.specify/`, `.github/skills/`).

## Installation

### 1. Java 25

```powershell
java -version   # doit afficher "25"
```

Définir `JAVA_HOME` si nécessaire :

```powershell
$env:JAVA_HOME = "C:\Users\<utilisateur>\.jdks\ms-25.0.4.1"
```

### 2. Outillage Spec Kit (une seule fois)

```powershell
uv tool install specify-cli
specify version
```

L'intégration GitHub Copilot et les scripts PowerShell sont déjà présents dans le dépôt.
Pour les réinstaller sur un poste vierge :

```powershell
specify init . --integration copilot --script ps
```

## Lancement de l'application

```powershell
.\mvnw.cmd spring-boot:run
```

L'application écoute uniquement sur `127.0.0.1:8080`.
La console H2 de développement est disponible sur <http://127.0.0.1:8080/h2-console>
(JDBC URL : `jdbc:h2:file:./data/campus-materiel`, utilisateur `sa`, mot de passe vide).

## Tests

```powershell
.\mvnw.cmd test
```

Les tests utilisent une base H2 en mémoire (`src/test/resources/application.properties`) :
la base locale de développement n'est jamais modifiée.

## Réinitialisation des données

Les données sont fictives et réinitialisables.

1. Arrêter l'application.
2. Supprimer le dossier `data/` :

```powershell
Remove-Item -Recurse -Force data
```

3. Relancer l'application : le programme d'initialisation recrée les trois étudiants fictifs et
   les cinq équipements.

Le dossier `data/` est exclu du dépôt Git : les données locales ne sont jamais versionnées.

## Limites de cette version

Sont exclus du périmètre :

- les comptes et mots de passe ;
- les courriels et notifications ;
- la gestion des retards et des pénalités ;
- les réservations sur plusieurs jours ;
- les réservations par créneau horaire ;
- l'ajout ou la suppression de matériel par une interface d'administration.

La sélection de l'étudiant courant **simule une identité pour le TP : elle ne constitue pas une
authentification.** Chacun peut changer d'étudiant.

## Contenu du dépôt

| Chemin | Contenu |
|---|---|
| `docs/enonce.md` | Besoin, données, règles RG-01 à RG-09, scénarios T01 à T12 |
| `.specify/memory/constitution.md` | Principes du projet |
| `specs/` | Spécification, plan et tâches produits par Spec Kit |
| `src/main/java/com/campus/campusmateriel/` | Code de l'application |
| `.github/skills/` | Commandes Spec Kit pour GitHub Copilot |
| `.github/copilot-instructions.md` | Consignes de travail de l'agent |

## Contradiction entre documents sources

Le document `develloper_une_app.md` (énoncé principal) impose Python/Flask/SQLite/pytest ; le
document `copilot.md` décrit une variante Java 25/Spring Boot. **La pile Java/Spring Boot a été
retenue par décision explicite du binôme** : l'énoncé principal reste le référentiel
fonctionnel (règles métier, scénarios de recette, livrables), tandis que les choix techniques
suivent `copilot.md`. Cette décision est consignée dans `docs/journal-decisions.md`.
