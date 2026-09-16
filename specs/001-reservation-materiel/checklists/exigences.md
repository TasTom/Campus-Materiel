# Checklist de qualité des exigences : Réservation de matériel pédagogique

**Objet** : vérifier que les exigences relatives aux **dates**, aux **autorisations d'annulation**,
aux **conflits de réservation** et aux **messages d'erreur** sont précises, complètes et non
ambiguës.

**Créée le** : 2026-09-16
**Fonctionnalité** : [spec.md](../spec.md)

**Note** : cette checklist produite par `/speckit-checklist` vérifie **la qualité de la
rédaction des exigences**. Elle ne teste **pas** l'application et ne remplace pas la recette
T01 à T12. Un élément coché signifie que la qualité de l'exigence a été relue et jugée
satisfaisante — pas que le code correspondant est écrit.

**Propriété de la relecture** : cette checklist appartient au relecteur. Un élément n'est coché
que lorsque **le binôme** a vérifié le critère. Aucun élément n'est coché d'avance.

---

## A. Dates et référentiel de temps

- [ ] CHK001 La notion de « date courante » est définie sans ambiguïté et son origine est précisée (serveur local et non navigateur) — FR-024, H-02
- [ ] CHK002 Le cas où la date réservée est **égale** à la date courante est tranché explicitement (autorisé pour réserver, autorisé pour annuler) — RG-03, RG-06
- [ ] CHK003 Le cas d'une date **strictement antérieure** à la date courante est tranché pour la réservation — FR-007
- [ ] CHK004 Le cas d'une date **strictement postérieure** est distingué du cas du jour même — FR-007, scénarios 5 et 6 de l'histoire 3
- [ ] CHK005 La granularité temporelle est fixée sans ambiguïté : journée entière, aucun créneau horaire — H-01, exclusions du périmètre
- [ ] CHK006 Le sort d'une réservation dont la date est **passée depuis** (créée valide, devenue passée) est décrit pour la consultation — FR-009, FR-013
- [ ] CHK007 Le format attendu de la date saisie est spécifié, et le comportement en cas de format différent est décrit — FR-019, CHK027
- [ ] CHK008 L'exigence de reproductibilité temporelle est formulée de façon vérifiable, sans imposer de solution technique — FR-024
- [ ] CHK009 La date de référence de la recette est unique et cohérente entre les documents concernés — `docs/enonce.md` (10 mars 2030), `quickstart.md`

## B. Autorisations d'annulation

- [ ] CHK010 La règle d'appartenance est formulée en termes d'identité, pas de rôle ou de droit implicite — FR-010, RG-05
- [ ] CHK011 L'origine de l'identité du demandeur est spécifiée, et il est explicitement interdit qu'elle provienne du formulaire — FR-010, contrat `routes.md`
- [ ] CHK012 Le cas d'un demandeur qui n'est **pas** propriétaire est distingué du cas d'une réservation inexistante — FR-010 vs FR-020
- [ ] CHK013 L'ordre des contrôles à l'annulation est spécifié, notamment que le contrôle du propriétaire **précède** celui de l'état — contrat `routes.md`
- [ ] CHK014 Le comportement en cas d'annulation d'une réservation **déjà annulée** est défini sans ambiguïté — CL-01, FR-013
- [ ] CHK015 La borne temporelle de l'annulation est explicite, **jour réservé inclus** — FR-011, RG-06
- [ ] CHK016 Le cas d'une annulation après le jour réservé est décrit avec le message associé — FR-012
- [ ] CHK017 Les effets d'une annulation réussie sont énumérés : conservation de l'enregistrement, libération du matériel, changement d'état — FR-013, FR-014, RG-07
- [ ] CHK018 L'exigence portant sur l'exhaustivité de l'accès en lecture est formulée : l'étudiant courant ne voit que ses propres réservations — FR-009
- [ ] CHK019 Le fait que l'annulation soit vérifiée **côté serveur** est une exigence, pas une consigne d'implémentation — FR-010, scénario 5 de l'histoire 5, T08
- [ ] CHK020 Le cas d'une annulation demandée sans étudiant courant sélectionné est tranché — FR-017, CL-06

## C. Conflits de réservation

- [ ] CHK021 La définition d'un conflit est complète : elle porte sur un **couple** (matériel, date) et sur les seules réservations **actives** — FR-004, RG-02
- [ ] CHK022 Le rôle des réservations annulées dans la détection de conflit est explicite : elles ne bloquent pas — FR-014, RG-07
- [ ] CHK023 Le cas où l'étudiant **réserve à nouveau son propre créneau** est distingué du conflit avec un autre étudiant — FR-025, décision CA-01
- [ ] CHK024 Le cas d'un même étudiant réservant **plusieurs matériels** à la même date est explicitement autorisé — FR-008, RG-04
- [ ] CHK025 Le cas d'un même matériel réservé à **deux dates différentes** est explicitement autorisé — CL-09, scénario 3 de l'histoire 3
- [ ] CHK026 Le cas de **demandes simultanées** est décrit comme une exigence de résultat (« au plus une réservation active ») sans imposer de mécanisme — FR-021, SC-002, T12
- [ ] CHK027 Le comportement en cas de **matériel inexistant** est distingué du conflit — FR-018, CL-02
- [ ] CHK028 L'unicité de chaque exemplaire physique est une exigence explicite, distincte de la notion de catégorie — FR-023, RG-01
- [ ] CHK029 La limite de deux réservations actives par jour est identifiée comme **hors périmètre** et ne contredit aucune exigence formulée — CA-03, exclusions du périmètre

## D. Messages d'erreur et retours utilisateur

- [ ] CHK030 Les refus sont associés à des **identifiants de message** stables, et non à des chaînes de caractères libres — contrat `messages.md`
- [ ] CHK031 Chaque cas limite identifié (CL-01 à CL-09) possède un message correspondant, ou une justification de son absence — `contracts/messages.md`
- [ ] CHK032 L'exigence « aucune donnée modifiée en cas d'entrée invalide » est vérifiable pour **chaque** cas de refus — FR-016, RG-09
- [ ] CHK033 Le cas de deux messages pour un même type de refus (conflit avec soi-même / conflit avec autrui) est explicitement exigé — FR-025, décision CA-01
- [ ] CHK034 L'interdiction d'afficher une erreur technique ou une trace d'exécution est une exigence, pas une préférence — FR-022, contrat `messages.md`
- [ ] CHK035 Le contenu attendu d'un message en cas de **liste vide** est précisé pour chaque liste concernée — CL-05, FR-022
- [ ] CHK036 L'ordre de priorité entre plusieurs erreurs possibles sur une même requête est défini, afin que le message affiché soit déterministe — contrat `routes.md`
- [ ] CHK037 L'exigence relative à la **langue** des messages est explicite — constitution, section « Workflow de développement »
- [ ] CHK038 Le cas où aucun étudiant courant n'est sélectionné produit un message qui **indique l'action à effectuer**, pas seulement l'erreur — FR-017, contrat `messages.md`

## E. Cohérence transverse et traçabilité

- [ ] CHK039 Chaque règle RG-01 à RG-09 est reliée à au moins une exigence FR, sans règle orpheline — tableau de traçabilité de `spec.md`
- [ ] CHK040 Chaque scénario T01 à T12 est relié à une histoire et à des exigences, sans scénario orphelin — tableau de traçabilité de `spec.md` et `contracts/routes.md`
- [ ] CHK041 Les exigences sont formulées sans détail d'implémentation (aucun langage, framework, route HTTP ou table de base de données) — constitution, principe VI
- [ ] CHK042 Les exigences affaiblies par des verbes vagues (« devrait », « si possible ») ont été remplacées par des obligations vérifiables — constitution, principes I et IV
- [ ] CHK043 Les décisions de clarification CA-01 à CA-03 sont répercutées de façon cohérente dans la spécification, le modèle de données et le contrat, sans version contradictoire subsistante
- [ ] CHK044 L'état `ANNULEE_ADMINISTRATIVE` n'est introduit **que** dans le modèle d'état et ne crée aucune obligation fonctionnelle implicite — FR-027, décision CA-02
- [ ] CHK045 Le périmètre exclu est rappelé de façon identique dans la spécification, le plan et le README, sans divergence — H-07, exclusions

---

## Notes

- **Aucun élément n'est coché d'avance.** L'énoncé du TP stipule : « Relisez vous-mêmes les
  éléments avant de les déclarer satisfaits. » Cocher un élément est un acte de relecture
  humaine, pas une étape automatique.
- Un élément laissé décoché signale une exigence **encore à préciser ou à corriger** dans
  `spec.md`. La correction doit être faite dans la spécification, puis reportée dans le plan et
  les tâches si nécessaire.
- `/speckit-implement` lit l'état des cases comme un point de passage et ne modifie pas les
  marqueurs.
- Les éléments relatifs aux dates (A) et aux conflits (C) conditionnent directement la justesse
  des règles RG-02, RG-03 et RG-06 : ils doivent être relus en priorité (Jalon 1).
- Les éléments D dépendent du contrat `contracts/messages.md` : un message exigé sans code stable
  ne serait pas testable.
