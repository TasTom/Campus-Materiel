# Bilan individuel

**Auteur** : Tom TAS · **Date** : 16 septembre 2026 · **Volume du corps du bilan : environ 500 mots**

> ## ⚠️ À FAIRE AVANT LA REMISE
>
> Ce texte a été rédigé à partir de **tes réponses** à l'entretien, pas inventé. Il reprend
> fidèlement les ambiguïtés que tu as retenues, la proposition que tu as refusée, la fonction que
> tu as choisie et les difficultés que tu as reconnues.
>
> **Il reste à ta main.** À relire et réécrire avec tes propres mots avant de rendre :
>
> 1. Vérifie que **chaque phrase correspond à ce que tu as réellement fait**.
> 2. Reformule ce qui ne sonne pas comme toi : c'est ton bilan, et il sera lu comme tel.
> 3. Complète avec un souvenir précis si tu en as un (un moment où tu as douté, une vérification
>    que tu as demandée, une erreur que tu as rattrapée).
> 4. **Supprime ce bloc** quand c'est fait.
>
> Un bilan que tu ne peux pas défendre à l'oral te coûterait plus qu'il ne rapporte.

---

## 1. Quelle ambiguïté avez-vous identifiée ?

J'en retiens quatre.

La première : deux documents du dossier se contredisaient — l'un imposait Python et Flask, l'autre
décrivait Java 25 avec Spring Boot. Les deux étaient défendables, donc il fallait trancher et le
tracer.

La deuxième : quel message afficher quand un étudiant redemande un matériel qu'il a lui-même déjà
réservé. Le refus était certain, le message ne l'était pas.

La troisième : la limite de deux réservations par jour était annoncée comme une évolution future
alors qu'elle figurait déjà dans l'énoncé. Le périmètre n'était pas aussi net qu'annoncé.

La quatrième, la plus trompeuse : « empêcher les doubles réservations » se lit facilement comme un
problème d'interface. Or masquer un bouton ne protège rien.

## 2. Quelle proposition de l'IA avez-vous corrigée, et pourquoi ?

J'ai refusé la pile Python et Flask de l'énoncé principal pour retenir Java 25 avec Spring Boot,
parce que c'était la pile que je voulais utiliser.

L'important n'était pas le choix lui-même, mais de l'avoir rendu explicite. Deux documents du même
dossier se contredisaient, et il était facile de suivre l'un sans relever le conflit. La décision a
été consignée dans le journal de décisions plutôt que de rester dans la conversation.

## 3. Quelle partie du code pouvez-vous expliquer précisément ?

`ReservationService.reserver`.

Entrées : l'identifiant du matériel, la date, et l'identifiant de l'étudiant — ce dernier venant de
la session, jamais du formulaire. Sortie : la réservation active créée. En cas de refus, rien n'est
écrit.

La méthode applique cinq contrôles dans un ordre fixe : date, étudiant courant, existence du
matériel, conflit, puis limite du jour. Cet ordre est contractuel : il rend le message déterministe
quand une requête cumule plusieurs anomalies, et il place le conflit avant la limite pour ne pas
inciter à annuler une réservation sans que cela débloque la demande.

Le test associé vérifie chaque motif de refus et l'absence d'écriture.

## 4. Quel travail Spec Kit a-t-il facilité ?

Trois choses.

La constitution, qui a transformé des intentions vagues en principes contrôlables — en particulier
« chaque règle métier possède une vérification ».

La spécification, dont les exigences numérotées ont servi de base aux tests et à la traçabilité : je
réponds en quelques secondes à la question « où est vérifiée RG-02 ? ».

L'analyse de cohérence, qui a révélé des écarts laissés par la relecture : le critère de succès
annonçait « FR-001 à FR-024 » alors qu'il y en avait 27, et le plan parlait de six routes quand il en
existait sept.

## 5. Quelle difficulté subsiste malgré les spécifications ?

Les défauts qui ne se voient qu'à l'exécution. Une expression de fragment Thymeleaf invalide, un
chargement paresseux non anticipé, des apostrophes échappées en HTML : aucune spécification ne les
annonce. Ils ont été trouvés par les tests, pas par la relecture. La spécification décrit le
comportement attendu ; elle ne dit rien de la façon dont un gabarit échoue.

## 6. Dans quelles situations une exploration rapide par prompts vous paraît-elle utile ?

Pour comparer des options avant de décider. Quand deux modèles d'état ou deux mécanismes de
protection sont envisageables, explorer rapidement les deux aide à choisir en connaissance de cause.

À une condition : la décision doit ensuite être figée dans un document. L'exploration peut rester
rapide ; c'est la décision qui doit être écrite.

---

## Repères cités dans ce bilan

| Élément cité | Où le vérifier |
|---|---|
| Contradiction entre les deux énoncés techniques | `docs/journal-decisions.md` D-01 |
| Message distinct en cas de re-réservation | Décision CA-01, `specs/001-reservation-materiel/spec.md` |
| Limite de deux réservations, d'abord hors périmètre | Décisions CA-03 puis D-13 |
| Contrôle côté serveur, pas dans le formulaire | Scénario T08, `AnnulationNonProprietaireTest` |
| Ordre des contrôles à la réservation | `ReservationService.reserver`, `contracts/routes.md` |
| Écarts révélés par l'analyse de cohérence | `docs/journal-decisions.md` D-10 |
| Défauts détectés seulement à l'exécution | `docs/compte-rendu-recette.md` §4 (anomalies A-1 à A-7) |
