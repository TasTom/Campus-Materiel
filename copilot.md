# TP Campus Matériel — Version IntelliJ, Java 25, Spring Boot et Spec Kit

## 1. Organisation de la solution

Chaque outil a un rôle précis :

| Outil | Rôle dans le TP |
|---|---|
| IntelliJ IDEA | Écrire, relire, exécuter et déboguer le code |
| Java 25 | Langage et environnement d’exécution |
| Maven Wrapper | Compiler, tester et construire l’application |
| Spring Boot | Configurer et démarrer l’application Web |
| Thymeleaf | Produire les pages HTML |
| Spring Data JPA | Accéder aux données |
| H2 en mode fichier | Conserver les données localement |
| GitHub Copilot | Lire le projet et réaliser les tâches demandées |
| Spec Kit | Structurer les exigences, le plan et les tâches |

**Spec Kit n’est pas une dépendance Maven.** Il s’installe comme outil de développement et ajoute au dépôt les fichiers nécessaires au travail de l’agent.

Le périmètre fonctionnel du TP initial reste applicable : consulter le matériel, réserver pour une journée, consulter ses réservations et annuler, avec les règles RG-01 à RG-09.

## 2. Préparer IntelliJ et Java

### Créer le projet Spring Boot

Dans IntelliJ, utilisez l’assistant de création Spring Boot. S’il n’est pas disponible, générez le projet sur [Spring Initializr](https://start.spring.io/), décompressez-le puis ouvrez son fichier `pom.xml` dans IntelliJ.

Paramètres :

| Champ | Valeur |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.1 |
| Group | `com.campus` |
| Artifact | `campus-materiel` |
| Package name | `com.campus.campusmateriel` |
| Packaging | Jar |
| Java | 25 |

Spring Boot 4.1.1 prend en charge Java 25. La documentation indique Maven 3.6.3 au minimum ; utilisez le Wrapper généré avec le projet pour partager la même version entre étudiants. [Prérequis Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html).

Sélectionnez les dépendances :

- Spring Web ;
- Thymeleaf ;
- Spring Data JPA ;
- Validation ;
- H2 Database.

Conservez les dépendances de test générées par Initializr. Évitez Lombok pour ce TP afin que les étudiants puissent lire les constructeurs et les méthodes effectivement utilisés.

L’assistant IntelliJ permet de choisir Maven et le JDK lors de la génération. [Documentation IntelliJ](https://www.jetbrains.com/help/idea/spring-initializr-project-wizard.html).

### Vérifier le JDK utilisé

Configurez Java 25 pour :

1. le SDK du projet ;
2. l’importation Maven ;
3. l’exécution Maven ;
4. la configuration de lancement de l’application.

Dans le terminal intégré, sous Windows :

```powershell
java -version
.\mvnw.cmd -version
.\mvnw.cmd test
```

Sous Linux ou macOS :

```bash
java -version
./mvnw -version
./mvnw test
```

**Résultat attendu :** Maven utilise Java 25 et le test initial réussit.

## 3. Installer GitHub Copilot dans IntelliJ

Pour ce parcours, utilisez **GitHub Copilot en mode Agent** dans IntelliJ afin de travailler sur les fichiers du projet.

1. Ouvrez les paramètres d’IntelliJ, puis **Plugins > Marketplace**.
2. Recherchez et installez le plugin **GitHub Copilot** publié par GitHub.
3. Redémarrez IntelliJ si nécessaire.
4. Connectez-vous au compte GitHub disposant de l’accès Copilot fourni par l’établissement.
5. Ouvrez **GitHub Copilot Chat** et sélectionnez le mode **Agent**.

La procédure officielle est disponible dans la [documentation d’installation de GitHub Copilot](https://docs.github.com/en/copilot/how-tos/set-up/install-copilot-extension?tool=jetbrains).

**Point de contrôle :** le projet ouvert dans IntelliJ doit être le dossier contenant `pom.xml`. Vérifiez que GitHub Copilot peut lire ce fichier avant de poursuivre.

## 4. Ajouter Spec Kit au projet

L’enseignant prépare Python 3.11 ou supérieur et `uv`. Python sert ici à l’outillage Spec Kit ; l’application reste entièrement en Java.

Enregistrez d’abord le squelette Spring Boot dans Git.

Dans le terminal IntelliJ, à la racine du projet :

```powershell
uv tool install specify-cli
specify version
specify init . --integration copilot --script ps
```

Sous Linux ou macOS :

```bash
uv tool install specify-cli
specify version
specify init . --integration copilot --script sh
```

Le point `.` désigne le projet courant. Examinez les changements proposés dans ce dossier déjà existant. Les options d’intégration et de scripts sont documentées dans le [guide d’installation Spec Kit](https://github.github.io/spec-kit/installation.html).

Après l’initialisation, ouvrez une nouvelle conversation dans **GitHub Copilot Chat**, en mode **Agent**.

Vérifiez que les commandes `/speckit.*` sont proposées. Si votre version du plugin ne les affiche pas directement, utilisez le fichier de prompt Spec Kit correspondant dans `.github/prompts/` comme contexte et demandez à l’agent d’en exécuter les instructions avec la consigne de l’étape.

**Attention à l’endroit où saisir les commandes :**

| Terminal IntelliJ | GitHub Copilot Chat — mode Agent |
|---|---|
| `specify init ...` | `/speckit.constitution` |
| `.\mvnw.cmd test` | `/speckit.specify ...` |
| `.\mvnw.cmd spring-boot:run` | `/speckit.implement ...` |

## 5. Fournir le sujet à GitHub Copilot

Créez dans le dépôt un document `docs/enonce.md` contenant :

- le contexte Campus Matériel ;
- les trois étudiants fictifs et les cinq équipements ;
- les règles RG-01 à RG-09 ;
- les situations limites ;
- les scénarios de recette T01 à T12 ;
- les fonctionnalités exclues.

Conservez la demande de limitation à deux réservations pour l’étape d’évolution.

Dans le dossier `.github` à la racine du dépôt, créez ou complétez le fichier `copilot-instructions.md` avec ces consignes de travail :

```markdown
# Consignes du projet

- Lire docs/enonce.md et les documents Spec Kit avant de modifier le code.
- Utiliser Java 25 et Maven Wrapper.
- Respecter les versions définies dans pom.xml.
- Écrire les documents et les messages utilisateur en français.
- Placer les règles métier dans les services.
- Ne pas ajouter de fonctionnalité hors périmètre.
- Exécuter les tests pertinents après modification.
- Ne pas annoncer un test réussi sans l'avoir exécuté.
- Préserver le travail existant et expliquer les modifications.
```

Ce fichier complète les spécifications sans les remplacer. Consultez la [documentation des instructions de dépôt pour GitHub Copilot](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions-in-your-ide/add-repository-instructions-in-your-ide?tool=jetbrains).

## 6. Définir les principes avec Spec Kit

Dans GitHub Copilot :

```text
/speckit.constitution

Le projet Campus Matériel est un TP de BUT Informatique.

Principes :
- Le code doit pouvoir être expliqué par les étudiants.
- Les contrôleurs gèrent les échanges HTTP.
- Les services portent les règles métier.
- Les repositories gèrent l'accès aux données.
- Les entrées sont validées côté serveur.
- Chaque règle métier possède des tests.
- Les réservations persistent après redémarrage.
- La date courante est fournie par une Clock injectable.
- Les données sont fictives.
- Chaque incrément doit être relu et testé avant le suivant.
```

Relisez la constitution produite. Vérifiez notamment que les principes peuvent être contrôlés concrètement.

## 7. Produire et clarifier la spécification

```text
/speckit.specify

Lis docs/enonce.md.

Spécifie Campus Matériel :
- sélectionner un étudiant fictif ;
- consulter les matériels disponibles pour une date ;
- réserver un matériel pour une journée ;
- consulter les réservations de l'étudiant courant ;
- annuler une réservation autorisée.

Respecte RG-01 à RG-09.
Conserve l'historique des annulations.
Décris les erreurs et les critères d'acceptation.
N'ajoute pas encore la limite de deux réservations.
Ne détaille pas les technologies dans la spécification fonctionnelle.
```

Puis :

```text
/speckit.clarify

Vérifie les règles sur :
- aujourd'hui et les dates passées ;
- les réservations déjà annulées ;
- l'annulation par un autre étudiant ;
- les équipements inconnus ;
- les doubles réservations.

Utilise les réponses présentes dans docs/enonce.md.
Signale les décisions qui restent à prendre.
```

La documentation distingue bien le besoin fonctionnel, traité dans la spécification, des choix d’implémentation, traités dans le plan. [Parcours Spec Kit](https://github.github.io/spec-kit/quickstart.html).

**Solution attendue pour l’identité simulée :** sélectionner un étudiant place son identifiant dans la session HTTP. Les actions suivantes utilisent cette session.

Un formulaire d’annulation ne doit pas permettre de choisir librement l’identifiant du propriétaire. Ce mécanisme reste une simulation pédagogique, puisque chacun peut changer d’étudiant.

## 8. Générer le plan Spring Boot

```text
/speckit.plan

Adapte le squelette Maven existant sans recréer le projet.

Pile :
- Java 25 ;
- Spring Boot 4.1.1 ;
- Spring MVC et Thymeleaf ;
- Spring Data JPA ;
- H2 en mode fichier pour l'application ;
- H2 en mémoire dans les tests ;
- Jakarta Validation ;
- JUnit Jupiter et les outils de test Spring compatibles
  avec le pom.xml existant.

Architecture :
- controller ;
- service ;
- repository ;
- domain ;
- dto ;
- config.

Contraintes :
- identité fictive conservée en session HTTP ;
- formulaires validés côté serveur ;
- règles métier dans ReservationService ;
- transactions pour réserver et annuler ;
- protection contre les réservations concurrentes ;
- Clock injectable pour les dates ;
- initialisation des données sans doublons au redémarrage ;
- aucune fonctionnalité hors énoncé.

Prévois les pages, les routes, les entités, les tests et
les commandes Maven de vérification.
```

### Architecture attendue

```text
src/main/java/fr/iut/campusmateriel/
├── CampusMaterielApplication.java
├── config/
│   ├── ClockConfiguration.java
│   └── DemoDataInitializer.java
├── controller/
│   ├── EtudiantController.java
│   ├── MaterielController.java
│   └── ReservationController.java
├── domain/
│   ├── Etudiant.java
│   ├── Materiel.java
│   ├── Reservation.java
│   └── StatutReservation.java
├── dto/
│   └── ReservationForm.java
├── repository/
│   ├── EtudiantRepository.java
│   ├── MaterielRepository.java
│   └── ReservationRepository.java
└── service/
    └── ReservationService.java
```

Les pages Thymeleaf sont placées dans `src/main/resources/templates`.

### Routes proposées

| Méthode | Route | Action |
|---|---|---|
| GET | `/etudiants/selection` | Afficher les étudiants |
| POST | `/etudiants/selection` | Enregistrer l’étudiant courant en session |
| GET | `/materiels?date=2030-03-12` | Consulter les disponibilités |
| POST | `/reservations` | Réserver |
| GET | `/reservations` | Consulter ses réservations |
| POST | `/reservations/{id}/annulation` | Annuler |

Une action qui modifie les données utilise `POST`. Après une opération réussie, une redirection évite de soumettre à nouveau le formulaire lors d’un rafraîchissement.

## 9. Solution de référence pour les données

### Modèle minimal

| Entité | Attributs principaux |
|---|---|
| `Etudiant` | `id`, `nom` |
| `Materiel` | `id`, `code`, `nom`, `categorie` |
| `Reservation` | `id`, `etudiant`, `materiel`, `dateReservation`, `statut` |

```java
public enum StatutReservation {
    ACTIVE,
    ANNULEE
}
```

Pour `Reservation` :

- utiliser `LocalDate` pour la journée réservée ;
- définir deux relations `@ManyToOne` obligatoires ;
- enregistrer le statut avec `@Enumerated(EnumType.STRING)` ;
- conserver l’enregistrement après annulation.

**Ne placez pas une contrainte d’unicité simple sur matériel + date dans la table des réservations historiques.** Elle empêcherait une nouvelle réservation après annulation.

### Persistance locale

Dans `application.properties` :

```properties
spring.application.name=campus-materiel
server.address=127.0.0.1

spring.datasource.url=jdbc:h2:file:./data/campus-materiel
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

Pour ce TP, `update` simplifie la gestion du schéma. Les migrations versionnées pourront faire l’objet d’un approfondissement.

Dans `.gitignore` :

```gitignore
target/
data/
```

Le programme d’initialisation doit vérifier l’existence des étudiants et équipements avant de les ajouter. Il ne doit ni recréer les données à chaque lancement ni effacer les réservations.

**Erreur à éviter :** utiliser uniquement `jdbc:h2:mem:...` pour l’application ; les données ne seraient pas conservées après arrêt.

## 10. Solution de référence pour les règles métier

### Tester les dates sans dépendre du jour réel

```java
@Configuration
public class ClockConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
```

Le service reçoit cette horloge par son constructeur et utilise :

```java
LocalDate aujourdHui = LocalDate.now(clock);
```

Dans les tests :

```java
Clock horlogeTest = Clock.fixed(
    Instant.parse("2030-03-10T12:00:00Z"),
    ZoneOffset.UTC
);
```

La vérification d’une date passée reste ainsi reproductible.

### Empêcher les doubles réservations

Une vérification de disponibilité suivie d’un enregistrement ne suffit pas si deux requêtes arrivent simultanément.

Pour ce TP, la solution proposée consiste à **verrouiller le matériel dans une transaction**, puis vérifier sa disponibilité avant d’enregistrer.

Exemple de méthode de repository :

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select m from Materiel m where m.id = :id")
Optional<Materiel> findByIdForUpdate(@Param("id") Long id);
```

Spring Data JPA permet de préciser le mode de verrouillage avec `@Lock`. [Documentation du verrouillage](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html).

L’algorithme du service est le suivant :

```text
Dans une transaction :

1. Vérifier l'identité courante et les paramètres.
2. Refuser une date passée.
3. Charger et verrouiller le matériel.
4. Refuser un matériel inconnu.
5. Chercher une réservation ACTIVE pour ce matériel et cette date.
6. Si elle existe, refuser la demande.
7. Sinon, enregistrer la réservation ACTIVE.
8. Valider la transaction.
```

Toutes les créations doivent suivre ce protocole. L’annulation doit également coordonner ses changements avec le verrouillage du matériel.

### Annuler une réservation

```text
Dans une transaction :

1. Vérifier l'existence de la réservation.
2. Vérifier qu'elle appartient à l'étudiant courant.
3. Verrouiller le matériel concerné et relire l'état courant.
4. Si la réservation est déjà annulée, ne rien modifier.
5. Refuser une réservation dont la date est passée.
6. Remplacer ACTIVE par ANNULEE.
7. Conserver l'enregistrement.
```

Les contrôles de propriété sont effectués **dans le service**, même si l’interface masque le bouton aux autres étudiants.

## 11. Préparer les tâches et contrôler la cohérence

Dans GitHub Copilot :

```text
/speckit.checklist

Vérifie que les exigences couvrent les dates limites,
l'identité simulée, les annulations, la persistance
et les conflits de réservation.
```

Puis :

```text
/speckit.tasks

Prévois des incréments courts et leurs tests :
1. entités, repositories et données initiales ;
2. sélection de l'étudiant et consultation du matériel ;
3. création d'une réservation ;
4. consultation et annulation ;
5. erreurs, persistance et concurrence.

Associe chaque tâche aux règles métier concernées.
```

Enfin :

```text
/speckit.analyze
```

Corrigez les incohérences relevées avant de commencer. Une checklist d’exigences validée ne constitue pas une preuve que le code fonctionne.

## 12. Faire réaliser les incréments par GitHub Copilot

Exemple pour commencer :

```text
/speckit.implement

Réalise uniquement le premier incrément :
entités, repositories et initialisation des données.

Respecte le plan et le pom.xml existants.
Ajoute les tests de persistance correspondants.
Exécute Maven Wrapper puis arrête-toi pour relecture.
```

Après relecture :

```text
/speckit.implement

Réalise le deuxième incrément :
sélection de l'étudiant en session et consultation du matériel.

Ajoute les pages Thymeleaf et les tests correspondants.
Exécute Maven Wrapper puis arrête-toi pour relecture.
```

Continuez de la même manière pour les réservations et les annulations.

Dans un **second terminal IntelliJ**, exécutez vous-mêmes :

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Ouvrez [l’application locale](http://localhost:8080).

Pour construire la livraison :

```powershell
.\mvnw.cmd clean verify
```

**À chaque incrément :** relire les modifications, vérifier l’interface, examiner les résultats de tests et créer un commit.

## 13. Tests attendus pour le corrigé

Les scénarios T01 à T12 du TP initial sont conservés.

| Niveau | Vérifications |
|---|---|
| Service | Dates passées, propriété, état, règles d’annulation |
| Repository | Recherche des réservations actives et persistance |
| Web avec MockMvc | Session, formulaires invalides et appels directs |
| Intégration | Conflits entre réservations et transactions |
| Recette manuelle | Parcours complet et redémarrage |

Exemple de test de service, à adapter aux méthodes du projet :

```java
@Test
void refuseUneDoubleReservation() {
    service.reserver(aliceId, materielId, LocalDate.of(2030, 3, 12));

    assertThrows(
        MaterielIndisponibleException.class,
        () -> service.reserver(
            bilalId,
            materielId,
            LocalDate.of(2030, 3, 12)
        )
    );

    assertEquals(
        1L,
        repository.countByMaterielIdAndDateReservationAndStatut(
            materielId,
            LocalDate.of(2030, 3, 12),
            StatutReservation.ACTIVE
        )
    );
}
```

Ce test suppose des données préparées et une horloge fixée au 10 mars 2030.

**Il vérifie deux demandes successives.** Pour vérifier réellement le verrouillage, ajoutez un test avec deux transactions concurrentes, puis contrôlez qu’il existe exactement une réservation active.

Après la réalisation :

```text
/speckit.converge
```

Cette commande complète la revue de conformité dans les versions qui la proposent. Les tests et la recette humaine restent nécessaires. [Guide Spec Kit](https://github.github.io/spec-kit/quickstart.html).

## 14. Appliquer l’évolution : deux réservations maximum

La nouvelle règle devient :

> Un étudiant peut avoir au maximum deux réservations actives pour une même date.

Demandez d’abord à GitHub Copilot, en langage naturel :

```text
La règle RG-04 évolue :
un étudiant peut avoir au maximum deux réservations actives
pour une même date.

Mets à jour la spécification et les critères d'acceptation.
Identifie les conséquences sur le plan et les tâches.
Ne modifie pas encore le code.
```

Relisez les documents, actualisez le plan et les tâches avec Spec Kit, puis relancez `/speckit.analyze`.

### Modification technique attendue

Le service compte les réservations actives de l’étudiant à la date demandée et refuse la création si ce nombre atteint deux.

Pour protéger aussi cette limite contre deux demandes concurrentes visant des matériels différents :

1. verrouiller d’abord l’étudiant ;
2. verrouiller ensuite le matériel ;
3. vérifier la limite et la disponibilité ;
4. enregistrer.

Les opérations de création et d’annulation doivent respecter le même ordre de verrouillage.

Tests supplémentaires :

- la troisième réservation le même jour est refusée ;
- une annulation permet une nouvelle réservation ;
- une réservation un autre jour reste possible ;
- deux demandes concurrentes ne permettent pas de dépasser la limite.

## 15. Livraison et organisation pédagogique

Conservez les quatre séances du TP :

| Séance | Résultat attendu |
|---|---|
| 1 | Projet IntelliJ opérationnel, GitHub Copilot connecté, spécification validée |
| 2 | Plan Spring Boot, modèle de données et tâches validés |
| 3 | Application réalisée par incréments et testée |
| 4 | Recette, évolution et démonstration |

L’enseignant prépare les installations et accès aux outils avant la première séance.

Le dépôt final contient :

- le projet Maven et son Wrapper ;
- les documents Spec Kit ;
- les tests ;
- le README avec les commandes exactes ;
- le compte rendu de recette ;
- le journal des décisions humaines.

Le barème du TP initial peut être conservé.

**Pour la soutenance, chaque étudiant doit expliquer une règle métier dans le service, montrer son test et retrouver l’exigence correspondante dans la spécification.**