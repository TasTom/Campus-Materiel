# Consignes du projet Campus Matériel

## Sources de vérité

- Lire `docs/enonce.md` et les documents Spec Kit (`specs/`, `.specify/memory/constitution.md`)
  avant de modifier le code.
- En cas de contradiction entre une conversation et ces documents, les documents font foi.
  Signaler la contradiction au binôme au lieu de trancher silencieusement.

## Technique

- Utiliser Java 25 et le Maven Wrapper (`./mvnw`, `.\mvnw.cmd`), jamais un Maven global.
- Respecter les versions définies dans `pom.xml`.
- Pile imposée : Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, H2, Jakarta Validation.
- Ne pas ajouter de dépendance sans justification écrite.

## Organisation du code

- Les contrôleurs gèrent les échanges HTTP.
- Les services portent les règles métier.
- Les repositories gèrent l'accès aux données.
- La date courante est fournie par une `Clock` injectable.
- Les données sont fictives et réinitialisables.

## Règles de travail

- Écrire les documents et les messages utilisateur en français.
- Valider les entrées côté serveur, jamais uniquement dans le formulaire.
- Ne pas ajouter de fonctionnalité hors périmètre sans décision explicite du binôme.
- Exécuter les tests pertinents après modification.
- Ne pas annoncer un test réussi sans l'avoir exécuté.
- Préserver le travail existant et expliquer les modifications.
- Procéder par incréments : chaque incrément est relu, lancé et testé avant le suivant.
