# BiblioUniv — Produit

## Vision

BiblioUniv est une plateforme SaaS multi-tenant de gestion des bibliothèques universitaires numériques. Elle permet à chaque établissement de proposer ses ressources documentaires et ses services dans un espace sécurisé, adapté au rôle de chaque utilisateur.

Le frontend consomme le contrat REST du backend. Toute capacité nouvelle est ajoutée explicitement côté serveur ; le client ne contourne pas les contrôles de sécurité.

## Profils utilisateurs

### Étudiant

Consulte et recherche les ouvrages disponibles, emprunte un ouvrage, lit en ligne les documents auxquels il a accès et suit ses emprunts. Le backend expose la consultation des notifications et pénalités personnelles ; leur écran dédié n'est pas encore intégré à l'espace étudiant.

### Administrateur d’établissement

Gère les ressources et services de son établissement : catalogue, mémoires, étudiants et référentiels. Il consulte le tableau de bord, suit les emprunts, configure les règles de pénalité et consulte l'historique des notifications automatiques. Il ne voit ni ne devine les données d’un autre établissement.

### Administrateur de plateforme

Gère les établissements et les rôles, et consulte les statistiques globales de la plateforme. Son espace ne dépend pas d'un tenant établissement.

## Périmètre actuel

La base commune et les parcours des lots 1, 3 et 4 sont intégrés :

- Connexion, déconnexion et récupération de la session courante.
- Renouvellement de session selon le contrat de l’API.
- Navigation protégée par authentification et par rôle.
- Client HTTP partagé, gestion cohérente des erreurs 401/403 et layout commun.
- Accueils adaptés aux trois profils, avec accès rapides et indicateurs administratifs disponibles.

Le lot 1 a commencé par deux tranches :

- **Mon profil** : consultation via `GET /api/profil` et mise à jour via `PUT /api/profil`.
- **Catalogue ouvrages** : recherche et filtres filière/niveau/catégorie via l’API, puis consultation du détail d’un ouvrage.
- **Emprunts** : demande depuis la fiche, liste personnelle et accès à la lecture d’un emprunt actif.

La lecture utilise `GET /api/emprunts/{id}/lire` et un téléchargement PDF protégé qui revérifie le tenant, le propriétaire et le statut actif de l’emprunt. L’admin d’établissement peut téléverser/remplacer le PDF associé à un ouvrage de son tenant ; seuls les PDF valides jusqu’à 20 Mo sont acceptés. Les fichiers sont stockés sous `OUVRAGES_STORAGE_DIR` et ne sont jamais exposés par une URL publique.

Le lot 3 ajoute à l’espace administrateur établissement :

- La création, la modification, l’archivage, la réactivation et la suppression des ouvrages.
- La gestion des mémoires et de leurs PDF (disponibilité, téléchargement et suppression), sans URL publique pour les fichiers.
- La création et l’import Excel des étudiants, ainsi que l’activation et la désactivation de leurs comptes.
- La gestion des filières, niveaux et catégories de l’établissement.
- Une session d’accès d’un compte désactivé est refusée dès sa prochaine requête, et le renouvellement est également bloqué.

Les écrans d’administration consomment les endpoints protégés de l’API. Les listes, mutations et fichiers restent limités au tenant courant par le serveur ; les rôles et données d’un autre établissement ne sont ni affichés ni reconstruits côté client.

Le lot 4 comprend le tableau de bord établissement, la gestion des emprunts et retours, la consultation des notifications automatiques, la gestion des règles/pénalités et l'administration plateforme des établissements et rôles. L'admin établissement ne rédige plus de notifications individuelles depuis l'interface.

### Notifications et pénalités automatiques

- À la création d'un ouvrage ou d'un mémoire, les étudiants actifs du tenant correspondant à sa filière et à son niveau reçoivent une notification `NOUVELLE_RESSOURCE`.
- Le traitement planifié (une fois par heure par défaut, réglable via `app.penalites.processing-delay-ms`) envoie un `RAPPEL_ECHEANCE` moins de 48 heures avant l'échéance, au plus une fois par étudiant et période de 24 heures.
- À l'échéance dépassée, l'emprunt passe en retard et l'étudiant reçoit un `AVERTISSEMENT_RETARD`.
- Une pénalité et sa notification sont créées après le délai de tolérance de la règle du tenant. Sans règle configurée, aucun frais/pénalité n'est appliqué; l'avertissement de retard est conservé.
- La conséquence de la règle est appliquée à la pénalité; une suspension temporaire désactive le compte étudiant.
- Les notifications sont internes à l'application : aucun envoi par e-mail ou SMS n'est implémenté.

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
- L’étudiant peut consulter et mettre à jour son profil avec les validations prévues par l’API.
- L’étudiant peut rechercher les ouvrages et filtrer par filière, niveau et catégorie selon les paramètres supportés par l’API.
- L’étudiant peut créer un emprunt, le retrouver dans sa liste et ouvrir la lecture si le backend fournit une URL de document.
- L’admin d’établissement peut téléverser un PDF uniquement pour un ouvrage de son tenant.
- L’admin d’établissement peut gérer les ouvrages, mémoires, fichiers associés, étudiants et référentiels de son tenant.
- L’admin d’établissement peut activer et désactiver les comptes étudiants via l’API.
- Un compte désactivé ne peut plus utiliser un jeton d’accès existant ni renouveler sa session.
- Un PDF n’est servi qu’à l’étudiant propriétaire d’un emprunt actif du tenant courant.
- Les vues restent compréhensibles pendant le chargement et en cas d’erreur.
- Les autres membres peuvent réutiliser les conventions et composants communs pour construire leurs lots.
