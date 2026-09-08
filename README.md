# Starter SaaS sécurisé, JWT et multi-tenant

API pédagogique Java 21 / Spring Boot 4.1 / Spring Security / PostgreSQL / Flyway. Trois clients utilisent la même base, mais chaque catégorie, produit et projet est isolé par `tenant_id`.

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

## Catalogue

- CRUD `/api/categories` filtré par tenant ;
- CRUD `/api/products` filtré par tenant ;
- recherche `GET /api/products?search=mot-cle` sur le nom du produit ou de sa catégorie ;
- relation JPA `Product ManyToOne Category` ;
- une catégorie d'un autre tenant est traitée comme inexistante ;
- `imageUrl` accepte uniquement une URL HTTP(S) et évite de stocker l'image binaire dans PostgreSQL.

Exemple Produit :

```json
{
  "name": "Dock USB-C",
  "price": 129.90,
  "stock": 10,
  "imageUrl": "https://placehold.co/600x400?text=Dock",
  "categoryId": "30000000-0000-0000-0000-000000000001"
}
```

## Démarrage

```bash
docker compose up -d
mvn spring-boot:run
```

Flyway applique V1 à V4, dont `V3__jwt_refresh_and_categories.sql` et `V4__biblio_users_etudiants.sql`. Variables : `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`, `JWT_SECRET`.

## Test automatique

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-tenants.ps1
```

Le script teste login JWT, trois tenants, produits isolés, catégorie, image, création, accès étranger en `404` et rotation du refresh token.

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
domain/      TenantUser, RefreshToken, Etudiant, Category, Product, Project
controller/  Auth, Session, Profil, Etudiant, Category, Product et Project
service/     transactions et règles d'isolation
repository/  requêtes systématiquement filtrées par tenant
dto/         contrats sans tenantId modifiable
mapper/      entités JPA vers réponses publiques
```

## Production

Utilisez HTTPS, une clé longue générée aléatoirement ou un couple RSA, OAuth2/OIDC, cookies `HttpOnly` si le frontend est web, rotation/nettoyage des refresh tokens, limitation de débit, audit et PostgreSQL Row-Level Security.
