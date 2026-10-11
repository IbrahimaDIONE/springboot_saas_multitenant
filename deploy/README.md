# Déploiement BiblioUniv

Cible : `root@82.29.172.15`, site `https://bibliouniv.duckdns.org`.
Le pipeline ne configure pas automatiquement un VPS vierge : effectuer cette
préparation une fois avant le premier push sur `main`.

## 1. Préparer le VPS

Se connecter avec `ssh root@82.29.172.15`. Installer Docker Engine et le plugin
Docker Compose depuis la documentation officielle adaptée au système du VPS :
https://docs.docker.com/engine/install/ . Installer aussi `curl` et `flock`.
Le pipeline produit des images Linux amd64 : vérifier `uname -m` (`x86_64`).
Vérifier `docker compose version` et la disponibilité de l'option `up --wait`.

Le DNS A de `bibliouniv.duckdns.org` doit pointer vers `82.29.172.15`.
Supprimer/corriger un éventuel AAAA incorrect. Autoriser les ports TCP 22, 80 et
443 dans le pare-feu du VPS et du fournisseur. Les ports 80/443 doivent être
libres ; adapter le reverse proxy existant si nécessaire. PostgreSQL et l'API
ne publient aucun port directement.

Créer les secrets sur le VPS, une seule fois (ne pas écraser un `.env` existant) :

```bash
install -m 700 -d /opt/bibliouniv
cd /opt/bibliouniv
umask 077
if [ ! -e .env ]; then
  printf 'DOMAIN=bibliouniv.duckdns.org\nDB_PASSWORD=%s\nJWT_SECRET=%s\n' \
    "$(openssl rand -hex 32)" "$(openssl rand -hex 64)" > .env
fi
chmod 600 .env
```

Ne pas changer `DB_PASSWORD` après initialisation sans changer également le
mot de passe du rôle PostgreSQL. Les secrets restent sur le VPS.

## 2. Configurer GitHub Actions

Créer une clé SSH dédiée sur une machine de confiance :

```bash
ssh-keygen -t ed25519 -f bibliouniv_deploy -N '' -C github-bibliouniv
```

Ajouter le contenu de `bibliouniv_deploy.pub` au fichier
`/root/.ssh/authorized_keys` du VPS (dossier mode 700, fichier mode 600).
Dans GitHub → Settings → Environments, créer l'environnement `production` et
limiter les déploiements à `main`. Ajouter ses deux secrets :

| Secret | Contenu |
| --- | --- |
| `VPS_SSH_KEY` | Clé privée complète `bibliouniv_deploy` |
| `VPS_KNOWN_HOSTS` | Entrée SSH vérifiée pour `82.29.172.15` |

Obtenir l'entrée avec `ssh-keyscan -t ed25519 82.29.172.15`, puis comparer son
empreinte avec celle affichée depuis la console du fournisseur VPS :
`ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub`.
Ne pas faire confiance au seul résultat de `ssh-keyscan`.
Ne jamais ajouter de clé privée ou de mot de passe au dépôt. Le mot de passe
root communiqué dans la conversation doit être remplacé avec `passwd`.

## 3. Sécuriser les comptes applicatifs avant publication

Les migrations existantes V2, V17, V18 et V19 installent des comptes de test et
des mots de passe prédéfinis. Le pipeline conserve ces migrations pour ne pas
invalider leurs checksums Flyway. Avant d'exposer le service, initialiser la
base avec l'API seule, désactiver les comptes de démonstration et attribuer un
mot de passe BCrypt unique à l'administrateur conservé. Cette étape dépend
des comptes que vous souhaitez conserver ; elle n'est pas exécutée par CI.
Pour préparer la base avant le premier déploiement public, construire/charger
les images puis lancer uniquement `postgres api` avec le fichier Compose.
Ne pas lancer le service `web` tant que ces comptes ne sont pas sécurisés.

## 4. Fonctionnement du pipeline

- Pull request : compilation Java, tests Maven et build React dans Docker.
- Push sur `main` ou lancement manuel depuis `main` : mêmes contrôles, puis
  transfert des deux images par SSH. Aucun registre Docker privé à configurer.
- Sauvegarde PostgreSQL avant les migrations si la base existe déjà.
- Démarrage avec attente du contrôle de santé, puis vérification du site HTTPS.

Caddy délivre et renouvelle le certificat TLS automatiquement. Il sert React
avec le fallback SPA et transmet `/api/*` à Spring Boot, sans changer l'URL.
Références : [configuration Caddy](https://caddyserver.com/docs/caddyfile/patterns)
et [attente Compose](https://docs.docker.com/reference/cli/docker/compose/up/).

Les données PostgreSQL, documents importés et certificats utilisent des volumes
persistants. Ne pas exécuter `docker compose down -v` en production.

## Exploitation et échecs

```bash
cd /opt/bibliouniv
export IMAGE_TAG=$(cat current-release)
docker compose --env-file .env -f releases/$IMAGE_TAG/compose.yml ps
docker compose --env-file .env -f releases/$IMAGE_TAG/compose.yml logs --tail=100 api web
```

Une brève interruption est possible lors du remplacement des conteneurs.
En cas d'échec, GitHub marque le déploiement en erreur ; `current-release`
n'est mis à jour qu'après succès, mais les conteneurs peuvent avoir changé.
Pour diagnostiquer une tentative échouée, utiliser son SHA dans `IMAGE_TAG`.
Il n'y a pas de rollback automatique des migrations : vérifier leur compatibilité
avant de relancer `bash releases/<ancien-SHA>/release.sh <ancien-SHA>`
(les anciennes images doivent encore être présentes localement, sinon recharger
leur archive avant utilisation). Ne jamais restaurer une base
automatiquement, car cela pourrait supprimer de nouvelles écritures.

Les sauvegardes `backups/*.dump` sont privées mais locales au VPS. Prévoir
une copie hors serveur, une sauvegarde des volumes documents et une politique
de rétention des sauvegardes, releases et images Docker selon l'espace disponible.
