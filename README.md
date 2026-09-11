# BiblioUniv sécurisé, JWT et multi-tenant

API Java 21 / Spring Boot 4 / Spring Security / PostgreSQL / Flyway. Trois clients utilisent la même base, mais chaque ouvrage, filière, niveau et projet est isolé par `tenant_id`.

## Sécurité

```text
POST /api/auth/login
  -> username + password BCrypt
  -> access token JWT (15 minutes, claim tenant_id signé)
  -> refresh token opaque (7 jours, hash SHA-256 en base)

Authorization: Bearer <accessToken>
  -> validation cryptographique du JWT
  -> TenantFilter lit tenant_id
  -> TenantContext -> Service -> Repository(id + tenantId)
```

`POST /api/auth/refresh` renouvelle les deux tokens et révoque l'ancien refresh token. `POST /api/auth/logout` révoque le refresh token. En production, remplacez le secret de démonstration avec `JWT_SECRET` et stockez-le dans un coffre de secrets.

## Comptes

| Utilisateur | Mot de passe | Tenant |
|---|---|---|
| `client-a` | `password` | `tenant-a` |
| `client-b` | `password` | `tenant-b` |
| `client-c` | `password` | `tenant-b` |

## Bibliothèque

- CRUD `/api/filieres` filtré par tenant ;
- CRUD `/api/niveaux` filtré par tenant ;
- CRUD `/api/ouvrages` filtré par tenant ;
- recherche `GET /api/ouvrages?search=mot-cle&filiereId=...&niveauId=...` sur le titre, l’auteur ou le résumé ;
- relation JPA `Ouvrage ManyToOne Filiere` et `Ouvrage ManyToOne Niveau` ;
- une filière ou un niveau d’un autre tenant est traité comme inexistant.

Exemple Ouvrage :

```json
{
  "titre": "Algorithmique fondamentale",
  "auteur": "Thomas Cormen",
  "resume": "Introduction aux algorithmes et à leur analyse.",
  "filiereId": "31000000-0000-0000-0000-000000000001",
  "niveauId": "32000000-0000-0000-0000-000000000001"
}
```

## Démarrage

```bash
docker compose up -d
mvn spring-boot:run
```

Flyway applique V1 à V5, dont `V4__biblio_users_etudiants.sql` et `V5__biblio_ouvrages_filieres_niveaux.sql`. Variables : `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`, `JWT_SECRET`.

## Test automatique

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-tenants.ps1
```

Le script teste login JWT, trois tenants, ouvrages isolés, filière, niveau, création, accès étranger en `404` et rotation du refresh token.

La gestion des étudiants est disponible pour `ADMIN_ETABLISSEMENT` :

- `GET /api/etudiants` liste les étudiants du tenant courant ;
- `POST /api/etudiants` crée un compte étudiant inactif ;
- `POST /api/etudiants/{id}/activation` active un compte étudiant.

## Postman

Importez [SaaS-Multitenant.postman_collection.json](postman/SaaS-Multitenant.postman_collection.json). Exécutez les requêtes dans l'ordre : les scripts Postman enregistrent automatiquement `accessToken`, `refreshToken`, `categoryId` et `productId`.

## Architecture

```text
config/      SecurityFilterChain, JWT encoder/decoder, audit JPA
security/    UserDetails, JwtService et erreurs 401/403
tenant/      extraction du claim tenant_id et contexte de requête
domain/      TenantUser, RefreshToken, Etudiant, Filiere, Niveau, Ouvrage, Project
controller/  Auth, Session, Profil, Etudiant, Filiere, Niveau, Ouvrage et Project
service/     transactions et règles d'isolation
repository/  requêtes systématiquement filtrées par tenant
dto/         contrats sans tenantId modifiable
mapper/      entités JPA vers réponses publiques
```

## Production

Utilisez HTTPS, une clé longue générée aléatoirement ou un couple RSA, OAuth2/OIDC, cookies `HttpOnly` si le frontend est web, rotation/nettoyage des refresh tokens, limitation de débit, audit et PostgreSQL Row-Level Security.
