# BiblioUniv — Design

## Direction

Une bibliothèque universitaire contemporaine, éditoriale et accueillante. L'interface privilégie la lecture, la recherche et les actions rapides plutôt que l'effet tableau de bord générique.

## Fondations visuelles

- **Encre** `#17372F` : navigation, titres forts et actions principales.
- **Corail** `#D9654B` : accent d'action et repères importants.
- **Papier** `#F7F6F1` : fond général, avec surfaces blanches pour le contenu.
- **Ardoise** `#53615D` : texte secondaire ; contraste vérifié pour les informations essentielles.
- **Safran** `#E9B949` : accent ponctuel pour les états et couvertures.
- **Typographie** : Newsreader pour les titres éditoriaux, DM Sans pour l'interface, avec des replis locaux.

## Règles d'interface

- Navigation latérale sur grand écran et navigation compacte sur mobile.
- Grilles et tableaux alignés, marges régulières et angles discrets (4 à 8 px).
- Actions principales nommées ; icônes Lucide réservées aux commandes compactes et accompagnées d'un libellé accessible.
- Les couvertures servent de repères visuels ; les métadonnées restent lisibles et hiérarchisées.
- Chaque écran de données prévoit chargement, absence de résultat, erreur et contenu normal.
- Les couleurs ne portent jamais seules une information : associer un libellé ou une icône.
- Composants de référence : recherche, filtres, carte d'ouvrage, badge d'état, bouton principal, navigation et formulaire.

## Écrans de référence

1. **Connexion** : formulaire sobre, identité BiblioUniv et retours d'erreur accessibles.
2. **Accueil protégé** : shell partagé, identité de session et espace d'accueil neutre adapté au rôle.
3. **Accès refusé** : explication claire et retour vers l'espace autorisé.
4. **Mon profil** : consultation et modification des informations personnelles par l'étudiant.
5. **Catalogue ouvrages** : recherche, filtres pris en charge par l'API et cartes lisibles.
6. **Détail d'ouvrage** : métadonnées, filière, niveau et résumé.
7. **Mes emprunts** : statuts, dates d'échéance et accès à la lecture des emprunts actifs.
8. **Lecture en ligne** : document intégré quand l'API renvoie une URL, état indisponible sinon.
9. **Documents ouvrages** : téléversement et remplacement réservé à l'administrateur établissement.
10. **Gestion des ouvrages** : formulaire et liste d’administration avec états actifs et archivés.
11. **Gestion des mémoires** : métadonnées, archivage et gestion des fichiers PDF associés.
12. **Gestion des étudiants** : création de compte, import Excel avec retour par ligne et état d’activation explicite.
13. **Référentiels** : gestion des filières, niveaux et catégories de l’établissement.

## Comportement et accessibilité

- Actions accessibles au clavier, avec un état de focus visible.
- Contrôles nommés ; icônes décoratives masquées aux lecteurs d'écran.
- Retours d'action et erreurs annoncés clairement.
- Les écrans d’administration prévoient chargement, erreurs, listes vides et retours d’action ; les mutations sont réservées à l’admin établissement.
- Respecter `prefers-reduced-motion` et rester utilisable sur mobile.

## Limite de cette référence

L’administration des documents est réservée à l’admin établissement. Le téléchargement PDF est servi après vérification du tenant, du propriétaire et de l’emprunt actif ; un ouvrage sans fichier présente un état indisponible. Le serveur contrôle le type PDF et limite les fichiers à 20 Mo.