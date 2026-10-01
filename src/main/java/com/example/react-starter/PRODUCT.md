# BiblioUniv — Produit

## Vision

BiblioUniv est une plateforme SaaS multi-tenant de gestion des bibliothèques universitaires numériques. Elle permet à chaque établissement de proposer ses ressources documentaires et ses services dans un espace sécurisé, adapté au rôle de chaque utilisateur.

Le frontend consomme l’API Spring Boot existante. Il ne modifie pas son contrat et ne contourne pas une fonctionnalité manquante côté serveur.

## Profils utilisateurs

### Étudiant

Consulte et recherche les ouvrages disponibles, emprunte un ouvrage, lit en ligne les documents auxquels il a accès et suit ses emprunts. Dans les lots suivants, il pourra aussi consulter les mémoires, gérer ses favoris et son historique, consulter ses notifications et ses pénalités.

### Administrateur d’établissement

Gère les ressources et les services de son établissement : catalogue, étudiants, emprunts, notifications et règles de pénalité. Il consulte également le tableau de bord de son établissement. Il ne voit ni ne devine les données d’un autre établissement.

### Administrateur de plateforme

Gère les établissements et les rôles, et consulte les statistiques globales de la plateforme.

## Périmètre de l’étape actuelle

Cette étape construit uniquement la base commune, avant le démarrage du lot 1 :

- Connexion, déconnexion et récupération de la session courante.
- Renouvellement de session selon le contrat de l’API.
- Navigation protégée par authentification et par rôle.
- Client HTTP partagé, gestion cohérente des erreurs 401/403 et layout commun.
- Pages d’accueil neutres pour vérifier les espaces des trois profils, sans fonctionnalité métier.

Le profil étudiant, le catalogue, les emprunts et toutes les autres fonctionnalités des lots restent à construire après validation de cette base.

## Principes d’expérience

- Afficher à chaque personne uniquement les écrans et actions autorisés par son rôle.
- Respecter l’isolation entre établissements ; ne jamais exposer ni suggérer des données d’un autre tenant.
- Présenter clairement les états de chargement, les listes vides et les erreurs.
- Traiter une session expirée par une reconnexion cohérente et un accès interdit par un écran ou message explicite, jamais par une page blanche.
- Valider les formulaires côté interface selon les contraintes connues de l’API, tout en laissant le serveur faire foi.
- Garder les parcours lisibles, accessibles et cohérents entre les espaces.

## Critères de réussite de la base commune

- Un utilisateur peut se connecter et arrive dans l’espace correspondant à son rôle.
- Les routes et actions réservées à un rôle sont protégées dans l’interface.
- Les appels API partagent le même client et la même gestion de session.
- Chaque profil est dirigé vers son espace et ne peut pas ouvrir la route d’un autre rôle.
- Les vues restent compréhensibles pendant le chargement et en cas d’erreur.
- Les autres membres peuvent réutiliser les conventions et composants communs pour construire leurs lots.
