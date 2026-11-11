# Rapport technique — Microservice Users & Authentification JWT

> **Projet :** `users-microservice`
> **Technologie :** Spring Boot 3.2.5 / Java 21 / Spring Security / JWT (Auth0 java-jwt) / MySQL
> **Rôle du service :** gérer les utilisateurs, leurs rôles, et délivrer un **token JWT** après un login valide.
> **URL de base :** `http://localhost:8081/users` (port `8081`, context-path `/users`)

---

## 1. Objectif du microservice

Ce microservice est le **service d'identité** de l'application. Il assure trois missions :

1. **Stocker** les utilisateurs (`User`) et leurs rôles (`Role`) dans une base MySQL (`users_db`).
2. **Authentifier** un utilisateur à partir d'un `username` + `password` via l'endpoint `POST /users/login`.
3. **Générer et vérifier** un **token JWT** qui prouve l'identité de l'utilisateur sur les requêtes suivantes, sans session serveur (architecture **stateless**).

Le JWT contient :
- le **sujet** (`sub`) = le nom d'utilisateur ;
- un **claim** `roles` = la liste des rôles (`ADMIN`, `USER`, ...) ;
- une **date d'expiration** (`exp`) = maintenant + 10 jours.

---

## 2. Arborescence du projet et rôle de chaque dossier / fichier

```
users-microservice/
├── .mvn/                         # Wrapper Maven (outils internes)
├── .settings/                    # Configuration Eclipse
├── .apt_generated/               # Code généré par les annotations (build)
├── .apt_generated_tests/         # Idem pour les tests (build)
├── src/
│   ├── main/
│   │   ├── java/com/mezen/users/
│   │   │   ├── UsersMicroserviceApplication.java
│   │   │   ├── entities/
│   │   │   │   ├── User.java
│   │   │   │   └── Role.java
│   │   │   ├── repos/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── RoleRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   └── UserServiceImpl.java
│   │   │   ├── restControllers/
│   │   │   │   └── UserRestController.java
│   │   │   └── security/
│   │   │       ├── SecurityConfig.java
│   │   │       ├── SecurityBeansConfiguration.java
│   │   │       ├── SecParams.java
│   │   │       ├── MyUserDetailsService.java
│   │   │       ├── JWTAuthenticationFilter.java
│   │   │       └── JWTAuthorizationFilter.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/mezen/users/
│       └── UsersMicroserviceApplicationTests.java
├── target/                       # Résultat de compilation (généré, ne pas modifier)
├── pom.xml                       # Dépendances et configuration Maven
├── mvnw / mvnw.cmd               # Maven Wrapper (Linux/Mac et Windows)
├── HELP.md                       # Aide générée par Spring Initializr
├── .classpath / .project         # Fichiers de projet Eclipse
├── .gitignore / .gitattributes   # Configuration Git
└── README / ce rapport
```

### 2.1 Fichiers à la racine

| Fichier | Rôle |
|---|---|
| `pom.xml` | Décrit le projet Maven : Spring Boot 3.2.5, Java 21, dépendances Web, Security, JPA, MySQL, Lombok et **`com.auth0:java-jwt:3.4.1`** (bibliothèque qui crée et vérifie les JWT). |
| `mvnw` / `mvnw.cmd` | Permettent de lancer Maven sans installation locale (`./mvnw spring-boot:run`). |
| `.mvn/` | Fichiers du wrapper Maven. |
| `.settings/`, `.project`, `.classpath` | Métadonnées du projet Eclipse. |
| `target/` | Classes compilées et JAR produit par le build. Généré automatiquement. |
| `HELP.md` | Documentation par défaut de Spring Initializr. |

### 2.2 `src/main/resources/application.properties`

Configuration centrale de l'application :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/users_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
server.servlet.context-path=/users
server.port=8081
spring.main.allow-circular-references=true
```

- **Base de données** : MySQL, base `users_db` créée automatiquement si absente.
- **`ddl-auto=update`** : Hibernate crée / met à jour les tables (`user`, `role`, `user_role`) au démarrage.
- **`context-path=/users`** : toutes les routes sont préfixées par `/users` → le login est donc `POST /users/login`.
- **`allow-circular-references=true`** : évite le blocage lié à la dépendance circulaire entre `SecurityConfig` et `UserServiceImpl`/`MyUserDetailsService`.

### 2.3 `UsersMicroserviceApplication.java`

Point d'entrée Spring Boot (`main`). Il contient aussi une méthode `@PostConstruct init_users()` exécutée **au démarrage** :

- crée les rôles `ADMIN` et `USER` s'ils n'existent pas ;
- crée les utilisateurs de démonstration `admin / 123`, `mezen / 123`, `yassine / 123` s'ils n'existent pas ;
- attribue les rôles : `admin` → `ADMIN` + `USER`, `mezen` et `yassine` → `USER`.

L'initialisation est **idempotente** : elle vérifie l'existence avant d'insérer, ce qui évite l'erreur « Duplicate entry » au second démarrage.

### 2.4 `entities/User.java` et `entities/Role.java`

Ce sont les **entités JPA** (tables de la base).

**`User`**
| Champ | Détail |
|---|---|
| `user_id` | Clé primaire, auto-incrémentée |
| `username` | Unique (contrainte `@Column(unique=true)`) |
| `password` | Stocké **haché avec BCrypt** (jamais en clair) |
| `enabled` | Compte actif ou non |
| `roles` | `@ManyToMany` avec `Role`, chargé en `EAGER` |

**`Role`**
| Champ | Détail |
|---|---|
| `role_id` | Clé primaire auto-incrémentée |
| `role` | Nom du rôle, ex. `ADMIN` ou `USER` |

La relation `ManyToMany` utilise une **table de jointure `user_role`** (`user_id`, `role_id`).
Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) génère getters/setters/constructeurs ; ils sont aussi écrits manuellement dans le fichier.

### 2.5 `repos/UserRepository.java` et `repos/RoleRepository.java`

Interfaces Spring Data JPA : elles fournissent `save`, `findAll`, `findById`, etc. sans écrire de SQL.

- `UserRepository.findByUsername(String)` → retrouve un utilisateur par son login. **Méthode clé du login.**
- `RoleRepository.findFirstByRole(String)` → retrouve un rôle par son nom. `findFirst` évite l'exception si plusieurs lignes `ADMIN` existent en base.

### 2.6 `service/UserService.java` et `service/UserServiceImpl.java`

**Couche métier** entre les contrôleurs/filtres et la base.

Interface `UserService` :
```java
User saveUser(User user);
User findUserByUsername(String username);
Role addRole(Role role);
Role findRoleByRole(String rolename);
User addRoleToUser(String username, String rolename);
List<User> findAllUsers();
```

`UserServiceImpl` (annoté `@Service` et `@Transactional`) :
- `saveUser` : **hache le mot de passe** avec `BCryptPasswordEncoder` et met `enabled = true` par défaut ;
- `findUserByUsername` : délègue au repository (utilisé par `MyUserDetailsService`) ;
- `addRole`, `findRoleByRole`, `findAllUsers` : opérations CRUD simples ;
- `addRoleToUser` : ajoute un rôle à un utilisateur **sans doublon**.

### 2.7 `restControllers/UserRestController.java`

Contrôleur REST exposant :

```java
@GetMapping("all")
public List<User> getAllUsers()
```

→ `GET /users/all` retourne la liste des utilisateurs.
Cet endpoint est **protégé** : il exige un JWT valide **et** le rôle `ADMIN` (voir `SecurityConfig`).

### 2.8 `src/test/java/.../UsersMicroserviceApplicationTests.java`

Test de contexte Spring (`@SpringBootTest`) qui vérifie simplement que l'application démarre sans erreur (`contextLoads`).

### 2.9 Le dossier `security/` — cœur de l'authentification JWT

#### `SecParams.java`
Constantes de sécurité :
```java
EXP_TIME = 10 * 24 * 60 * 60 * 1000; // 10 jours en millisecondes
SECRET   = "Mazen@123";              // clé de signature HMAC256
PREFIX   = "Bearer ";                // préfixe attendu dans le header Authorization
```

#### `SecurityBeansConfiguration.java`
Déclare deux beans indispensables :
- `BCryptPasswordEncoder` : pour hacher les mots de passe et les comparer lors du login ;
- `AuthenticationManager` : récupéré depuis `AuthenticationConfiguration`, il orchestre l'authentification.

#### `MyUserDetailsService.java`
Implémente `UserDetailsService`. **C'est le pont entre la base et Spring Security.**
1. `loadUserByUsername(username)` appelle `UserService.findUserByUsername` ;
2. si l'utilisateur n'existe pas → `UsernameNotFoundException` ;
3. convertit chaque `Role` en `SimpleGrantedAuthority` (`ADMIN`, `USER`) ;
4. retourne un objet `UserDetails` contenant login, mot de passe haché et autorités.

C'est Spring Security qui compare ensuite le mot de passe saisi avec le hash BCrypt stocké.

#### `JWTAuthenticationFilter.java` — filtre de **connexion**
Étend `UsernamePasswordAuthenticationFilter` et intercepte **`POST /login`** (donc en réalité `/users/login`).

- `attemptAuthentication` :
  - lit le corps JSON ;
  - extrait uniquement `username` et `password` (les autres champs envoyés par le front sont ignorés) ;
  - si l'un manque → `BadCredentialsException` ;
  - délègue à `authenticationManager.authenticate(...)`.
- `successfulAuthentication` :
  - récupère les rôles de l'utilisateur ;
  - crée le JWT :
    ```java
    JWT.create()
       .withSubject(username)
       .withArrayClaim("roles", roles.toArray(...))
       .withExpiresAt(new Date(now + EXP_TIME))
       .sign(Algorithm.HMAC256(SECRET));
    ```
  - renvoie le token dans le **header de réponse `Authorization`** (sans le mot `Bearer`, c'est le front qui l'ajoute).
- `unsuccessfulAuthentication` :
  - renvoie un **401 JSON** `{"error":"Unauthorized","message":"login ou mot de passe erronés"}` au lieu de la redirection HTML par défaut.

#### `JWTAuthorizationFilter.java` — filtre de **vérification**
Étend `OncePerRequestFilter` : il s'exécute sur **chaque requête**.

1. Laisse passer sans token les routes publiques : `/login`, `/register`, `/verifyEmail` et les requêtes `OPTIONS` (CORS).
2. Lit le header `Authorization`. S'il est absent ou ne commence pas par `Bearer ` → laisse passer (Spring Security refusera plus loin si la route est protégée).
3. Sinon :
   - retire le préfixe `Bearer ` ;
   - vérifie la **signature** et l'**expiration** avec `JWT.require(HMAC256(SECRET))` ;
   - extrait `sub` (username) et le claim `roles` ;
   - reconstruit les autorités et place l'utilisateur dans le `SecurityContext` ;
4. Si le token est invalide/expiré → **401 JSON** `{"error":"Unauthorized","message":"Token invalide ou expiré"}`.

#### `SecurityConfig.java` — règles globales
Configure la chaîne de sécurité :

- **`STATELESS`** : aucune session HTTP ; l'identité repose uniquement sur le JWT.
- **CSRF désactivé** (inutile pour une API stateless).
- **CORS ouvert** : toutes origines / méthodes / headers, et expose le header `Authorization` pour que le front Angular puisse lire le token.
- **Règles d'accès** :
  | Route | Accès |
  |---|---|
  | `OPTIONS /**` | public |
  | `/login` | public |
  | `/all` | rôle `ADMIN` obligatoire |
  | toute autre route | authentifié (JWT valide) |
- **Gestion des erreurs** :
  - non authentifié → **401** JSON `Authentification requise` ;
  - authentifié mais rôle insuffisant → **403** JSON `Accès refusé : droits insuffisants`.
- **Enregistrement des filtres** : `JWTAuthenticationFilter` et `JWTAuthorizationFilter` sont ajoutés avant `UsernamePasswordAuthenticationFilter`.

---

## 3. Fonctionnement complet du login JWT (pas à pas)

### 3.1 Phase 1 — Connexion

```
Front / Postman
   │  POST http://localhost:8081/users/login
   │  Content-Type: application/json
   │  { "username": "admin", "password": "123" }
   ▼
JWTAuthenticationFilter.attemptAuthentication()
   │  lit username + password
   ▼
AuthenticationManager
   ▼
MyUserDetailsService.loadUserByUsername("admin")
   ▼
UserRepository.findByUsername("admin")  ──►  MySQL (users_db)
   │  retourne User + roles (ADMIN, USER)
   ▼
DaoAuthenticationProvider
   │  compare "123" (saisi) avec le hash BCrypt stocké
   ▼
   ├── Échec ──► 401 JSON « login ou mot de passe erronés »
   └── Succès
         ▼
   JWTAuthenticationFilter.successfulAuthentication()
         │  crée le JWT signé HMAC256
         ▼
   Réponse 200 avec header :
   Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Exemple de commande (PowerShell / curl) :

```bash
curl -i -X POST http://localhost:8081/users/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"123"}'
```

La réponse contient le header `Authorization` avec le token (sans `Bearer `).

### 3.2 Phase 2 — Accès à une ressource protégée

```
Front / Postman
   │  GET http://localhost:8081/users/all
   │  Authorization: Bearer <token>
   ▼
JWTAuthorizationFilter
   │  route protégée → lit le header
   │  vérifie signature + expiration
   │  extrait username + roles
   │  place l'authentification dans le SecurityContext
   ▼
SecurityConfig : /all exige l'autorité ADMIN
   ├── pas de token / token invalide ──► 401
   ├── token valide mais pas ADMIN ───► 403
   └── token valide + ADMIN ──────────► UserRestController.getAllUsers()
                                           ▼
                                       200 + liste JSON des utilisateurs
```

Exemple :

```bash
curl -i http://localhost:8081/users/all \
     -H "Authorization: Bearer <TOKEN>"
```

### 3.3 Contenu du JWT

```json
{
  "sub": "admin",
  "roles": ["ADMIN", "USER"],
  "exp": 1760000000
}
```

- **`sub`** : identité de l'utilisateur.
- **`roles`** : utilisé pour les contrôles d'accès (`hasAuthority("ADMIN")`).
- **`exp`** : date d'expiration (10 jours).
- **Signature HMAC256** avec la clé `SecParams.SECRET` : toute modification du contenu invalide le token.

---

## 4. Ce qui est réellement fonctionnel aujourd'hui

| Fonctionnalité | État | Détail |
|---|---|---|
| Login `POST /users/login` | ✅ | Génère un JWT si les identifiants sont valides |
| Mots de passe hachés | ✅ | BCrypt à l'enregistrement |
| Authentification sans session | ✅ | `SessionCreationPolicy.STATELESS` |
| Vérification du token | ✅ | Signature + expiration sur chaque requête |
| Gestion des rôles | ✅ | `ADMIN` / `USER`, claim `roles` |
| Protection de `GET /users/all` | ✅ | Réservé au rôle `ADMIN` |
| Réponses JSON 401 / 403 | ✅ | Pas de redirection HTML |
| CORS pour le front | ✅ | Origines, méthodes, headers autorisés |
| Données de démonstration | ✅ | `admin/123`, `mezen/123`, `yassine/123` |
| Inscription (`/register`) | ⚠️ | Route déclarée publique dans le filtre, **mais aucun contrôleur ne l'implémente** |
| Vérification e-mail (`/verifyEmail`) | ⚠️ | Déclarée publique, non implémentée |

---

## 5. Points à connaître pour la présentation (questions fréquentes)

**Pourquoi JWT plutôt qu'une session ?**
Le serveur ne stocke aucun état : le token contient l'identité et les rôles, et sa signature garantit qu'il n'a pas été falsifié. C'est adapté aux microservices et aux API appelées par un front Angular.

**Où est stocké le token côté client ?**
Le front le récupère dans le header de réponse `Authorization`, ajoute lui-même `Bearer `, puis l'envoie dans le header de chaque requête.

**Comment le mot de passe est-il protégé ?**
Il est haché avec **BCrypt** (`UserServiceImpl.saveUser`). La base ne contient jamais le mot de passe en clair. Lors du login, Spring compare le mot de passe saisi au hash.

**Que se passe-t-il si le token est modifié ou expiré ?**
La vérification de signature/expiration échoue → `JWTVerificationException` → réponse **401 JSON** et `SecurityContext` vidé.

**Différence entre 401 et 403 ?**
- **401 Unauthorized** : pas de token, token invalide ou expiré → il faut s'authentifier.
- **403 Forbidden** : utilisateur authentifié mais rôle insuffisant (ex. `USER` qui appelle `/all`).

**Pourquoi `findFirstByRole` et non `findByRole` ?**
Pour éviter une `NonUniqueResultException` si la base contient plusieurs lignes portant le même nom de rôle (anciennes initialisations).

**À quoi sert `init_users()` ?**
À créer automatiquement les rôles et comptes de démonstration au démarrage, de façon idempotente, pour que le projet soit testable immédiatement.

---

## 6. Limites et pistes d'amélioration (bonus pour le jury)

1. **Secret codé en dur** (`Mazen@123`) : le déplacer dans une variable d'environnement ou un coffre-fort de secrets.
2. **Token renvoyé dans un header** : le stockage côté navigateur (localStorage) expose au XSS ; envisager un cookie `HttpOnly` ou une stratégie de stockage sécurisée.
3. **Pas de refresh token** : à l'expiration (10 jours), l'utilisateur doit se reconnecter.
4. **`GET /users/all` renvoie les entités `User` complètes**, y compris le hash du mot de passe → utiliser un **DTO** ou `@JsonIgnore` sur `password`.
5. **CORS `*`** : à restreindre aux origines réelles en production.
6. **Routes `/register` et `/verifyEmail` non implémentées** : ajouter les contrôleurs correspondants ou retirer les exceptions du filtre.
7. **Mots de passe de démonstration `123`** : à réserver au développement.
8. **Pas de révocation de token** : un JWT reste valide jusqu'à expiration ; une liste de révocation ou des tokens courts + refresh seraient plus sûrs.
9. **Tests** : seul le chargement du contexte est testé ; ajouter des tests d'intégration du login (succès, mauvais mot de passe, token expiré, accès 403).

---

## 7. Résumé en une phrase

> Ce microservice Spring Boot expose `POST /users/login` : il vérifie les identifiants en base via `MyUserDetailsService`, compare le mot de passe BCrypt, génère un **JWT signé HMAC256** contenant le username, les rôles et une expiration de 10 jours, puis vérifie ce token sur chaque requête protégée grâce à `JWTAuthorizationFilter` et aux règles de `SecurityConfig` — le tout sans session serveur.
