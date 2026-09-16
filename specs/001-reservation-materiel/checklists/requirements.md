# Checklist qualité de la spécification : Réservation de matériel pédagogique

**Objet** : vérifier la complétude et la qualité de la spécification avant de passer au plan.
**Créée le** : 2026-09-16
**Fonctionnalité** : [spec.md](../spec.md)

Cette checklist porte sur **la qualité des exigences**, pas sur les tests de l'application.
Elle ne remplace pas la recette.

## Qualité du contenu

- [x] Aucun détail d'implémentation (langage, framework, API)
- [x] Centré sur la valeur utilisateur et le besoin métier
- [x] Rédigé pour un lecteur non technique
- [x] Toutes les sections obligatoires sont complétées

## Complétude des exigences

- [x] Exigences vérifiables et sans ambiguïté
- [x] Critères de succès mesurables
- [x] Critères de succès indépendants de la technique
- [x] Tous les scénarios d'acceptation sont définis
- [x] Les cas limites sont identifiés
- [x] Le périmètre est clairement délimité
- [x] Les dépendances et les hypothèses sont identifiées
- [x] Aucune ambiguïté restante — les 3 questions A-01 à A-03 ont été tranchées (décisions CA-01 à CA-03)

## Maturité de la fonctionnalité

- [x] Chaque exigence fonctionnelle possède un critère d'acceptation
- [x] Les histoires utilisateur couvrent les parcours principaux
- [x] La fonctionnalité satisfait les critères de succès définis
- [x] Aucun détail d'implémentation ne s'est glissé dans la spécification

## Vérifications de fond

### Couverture des 12 scénarios de recette imposés

T01 à T12 sont rattachés à une histoire et à des exigences (voir tableau de traçabilité de la
spécification). **Aucun scénario n'est orphelin.**

### Couverture des 8 critères demandés par l'énoncé

| Critère demandé | Où il est traité |
|---|---|
| Une réservation autorisée | Histoire 3, scénario 1 (T01) |
| Une date passée | Histoire 3, scénario 4 (T04) |
| Une double réservation | Histoire 3, scénario 2 (T02) |
| Une annulation autorisée | Histoire 5, scénario 1 (T06) |
| Une annulation par un autre étudiant | Histoire 5, scénario 5 (T08) |
| La disponibilité après annulation | Histoire 5, scénario 2 (T07) |
| La conservation après redémarrage | Histoire 3, scénario de recette T09 (FR-015) |
| Une entrée invalide | Histoire 3, scénarios 8 et 9 (T10) |

### Couverture des 9 règles métier

| Règle | Exigences couvrantes |
|---|---|
| RG-01 | FR-005, FR-023 |
| RG-02 | FR-004, FR-006, FR-021 |
| RG-03 | FR-007 |
| RG-04 | FR-008 |
| RG-05 | FR-009, FR-010 |
| RG-06 | FR-011, FR-012 |
| RG-07 | FR-013, FR-014 |
| RG-08 | FR-015 |
| RG-09 | FR-016 à FR-022 |

**Aucune règle métier n'est sans exigence.**

### Vérification du périmètre après clarification CA-02

La décision CA-02 étend le modèle d'état de la réservation. Elle ne doit pas élargir le
périmètre fonctionnel :

- [x] Aucune route d'administration n'est ajoutée
- [x] Aucun formulaire d'annulation administrative n'est ajouté
- [x] L'exigence FR-027 borne l'usage de cet état à un rôle documentaire et testable
- [x] La limite des deux réservations actives par jour reste hors périmètre (CA-03)

### Décisions imposées par l'énoncé (section 9)

| Situation | Décision attendue | Traitement |
|---|---|---|
| Annulation d'une réservation déjà annulée | Aucune modification ; message indiquant son état | CL-01, FR-013, FR-016 |
| Réservation d'un matériel inconnu | Refus ; aucune création | CL-02, FR-018 |
| Date absente ou mal formée | Refus avec explication | CL-03, FR-019 |
| Annulation d'une réservation passée | Refus | CL-04, FR-012 |
| Liste sans résultat | Message explicite, sans erreur technique | CL-05, FR-022 |

## Notes

- Les exigences n'ont **pas** été exprimées en termes de framework, de route HTTP ni de table
  de base de données : ces choix relèvent du plan.
- FR-024 est conservée dans la spécification car elle conditionne la **vérifiabilité** des
  autres exigences. La manière de l'obtenir (horloge injectable) est un choix technique qui
  appartient au plan.
- Les trois ambiguïtés A-01 à A-03 ont été présentées au binôme et tranchées : elles ne sont
  plus des zones d'ombre, mais des décisions tracées (CA-01 à CA-03).
- CA-02 étend volontairement le modèle d'état au-delà de la solution de référence. Le binôme en
  assume la conséquence : un état supplémentaire à modéliser et à tester, sans fonctionnalité
  associée. Cette décision est journalisée comme proposition corrigée de l'IA.
- Les éléments non cochés doivent être traités avant la validation du Jalon 1.
