# DevConnect API

Backend API for DevConnect, a social network for developers.

## Tech stack

- **Java 21**
- **Spring Boot 3.3.2**
- **Spring Security** with JWT (RSA)
- **PostgreSQL 16**
- **Flyway** for migrations
- **JUnit 5 + Mockito** (unit tests)
- **Testcontainers** (integration tests)
- **Maven Wrapper** (no global Maven installation needed)

## Requirements

- Docker (for the local database and the integration tests)
- JDK 21. The Lombok version managed by Spring Boot 3.3 does not run on newer JDKs, so point `JAVA_HOME` to a JDK 21 when building.
- Maven is downloaded automatically by the wrapper (`mvnw` / `mvnw.cmd`)

## Getting started

### 1. Start PostgreSQL

From the repository root:

```bash
docker compose -f api/data/docker-compose.yml up -d
```

The database is created with:

- Database: `devconnect`
- User: `devconnect`
- Password: `devconnect`

The Flyway migrations (`V1` to `V5`, in `src/main/resources/db/migration`) are applied automatically on startup. Since `ddl-auto` is set to `validate`, Hibernate never creates or changes tables: the whole schema comes from the migrations.

### 2. Build and run

```bash
# Build
./mvnw clean compile

# Run
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

There are no keys to set up: the RSA key pair used to sign tokens is generated in memory on startup. Tokens issued before a restart stop being accepted after it.

### 3. Demo data (optional)

Once Flyway has created the schema, fill the database with a ready-made scenario (from the repository root):

```bash
docker compose -f api/data/docker-compose.yml exec -T postgres psql -U devconnect -d devconnect < api/data/seed.sql
```

## Database files

| File | Purpose |
| --- | --- |
| `data/docker-compose.yml` | Starts a local PostgreSQL 16 |
| `src/main/resources/db/migration/V1..V5` | Flyway migrations, the single definition of the schema |
| `data/seed.sql` | Demo data: 17 users, 18 public and private posts, likes, comments, and accepted and pending friendships |

The seed script deletes existing data before inserting, can be run again at any time and always reproduces the same scenario. Every account uses the password `password123`; log in as `marilio@devconnect.com` to see the full scenario, with a feed and a friends list longer than one page. One of the users (`paula.ribeiro@devconnect.com`) is inactive on purpose, to show that inactive accounts cannot log in.

## Tests

There are 214 tests in 44 classes: 183 unit tests for services, mappers and validators using Mockito, plus 31 integration tests that run against a real PostgreSQL.

### Full suite (requires Docker)

```bash
./mvnw test
```

The integration test classes are named `*IntegrationTest` and match Surefire's default pattern, so they run together with the unit tests. **Without Docker running, this command fails.** The project doesn't use `maven-failsafe-plugin`, so `./mvnw verify` runs exactly the same set.

### Unit tests only (no Docker)

```bash
./mvnw test -Dtest='!*IntegrationTest'
```

### What each layer covers

| Suite | Coverage |
| --- | --- |
| Unit | Use-case services, `service/core` services, mappers and validators, without a database, using `@ExtendWith(MockitoExtension.class)` |
| Integration | Full HTTP flows with Testcontainers: `AuthenticationIntegrationTest` (login, invalid credentials, inactive users, protected routes), `SocialJourneyIntegrationTest` (an end-to-end social journey between two users) and `ValidationIntegrationTest` (validation messages and the error contract) |

## API documentation

Interactive documentation is available at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

(Available after `./mvnw spring-boot:run`.)

## Authentication

Only `POST /login`, `POST /users` and the Swagger routes are public. Everything else requires the header:

```
Authorization: Bearer <access_token>
```

The token comes from `POST /login`, is signed with RSA, is valid for 1 hour (`api.jwt.expiration-seconds`) and carries the `USER` scope, which is what `@PreAuthorize("hasAuthority('SCOPE_USER')")` on the controllers requires.

## Endpoints

### Authentication

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/login` | 200 | — | Authenticates and returns an access token |

### Users

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/users` | 201 | — | Registers a new user |
| `GET` | `/users/me` | 200 | ✔ | Data of the authenticated user |
| `PUT` | `/users/me` | 200 | ✔ | Updates name, nickname and profile image |
| `GET` | `/users?search=&page=&size=` | 200 | ✔ | Paginated search by name **or** email, with the friendship status of each user |
| `GET` | `/users/{userId}` | 200 | ✔ | A user's profile and the friendship status with them |

### Posts

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/posts` | 201 | ✔ | Publishes a post |
| `GET` | `/posts/feed?page=&size=` | 200 | ✔ | Paginated feed with your posts and your friends' posts, newest first |
| `PATCH` | `/posts/{postId}` | 200 | ✔ | Edits the post content (author only) |
| `PATCH` | `/posts/{postId}/visibility` | 200 | ✔ | Switches between `PUBLIC` and `PRIVATE` (author only) |
| `DELETE` | `/posts/{postId}` | 204 | ✔ | Deletes the post (author only) |
| `GET` | `/users/{userId}/posts?page=&size=` | 200 | ✔ | A user's posts, respecting visibility for the viewer |

### Comments

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/posts/{postId}/comments` | 201 | ✔ | Comments on a post |
| `GET` | `/posts/{postId}/comments?page=&size=` | 200 | ✔ | The post's comments, paginated |
| `DELETE` | `/posts/{postId}/comments/{commentId}` | 204 | ✔ | Deletes the comment (comment author or post author) |

### Likes

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/posts/{postId}/likes` | 201 | ✔ | Likes the post and returns the updated summary |
| `DELETE` | `/posts/{postId}/likes` | 200 | ✔ | Removes your like and returns the updated summary |
| `GET` | `/posts/{postId}/likes?page=&size=` | 200 | ✔ | Lists who liked the post, paginated |

### Friendships

| Method | Route | Status | Auth | Description |
| --- | --- | --- | --- | --- |
| `POST` | `/friendships` | 201 | ✔ | Sends a friend request |
| `PATCH` | `/friendships/{id}/accept` | 200 | ✔ | Accepts a received request (recipient only) |
| `DELETE` | `/friendships/{id}` | 204 | ✔ | Declines a request or removes a friendship |
| `GET` | `/friendships/requests` | 200 | ✔ | Pending requests you received |
| `GET` | `/friendships?search=&page=&size=` | 200 | ✔ | Your friends, filtered by name **or** email |

### Pagination

Paginated endpoints accept `page` and `size` (at most 50 items per page) and return Spring Data's paged envelope: `content` plus a `page` object with `size`, `number`, `totalElements` and `totalPages`.

The `sort` parameter is ignored on purpose: the order is fixed in the JPQL query, which uses `join fetch` to avoid N+1 queries. Allowing arbitrary client-side sorting would break those queries. `GET /friendships/requests` is the only listing endpoint without pagination, since it naturally returns a small volume.

### Errors

`ApiExceptionHandler` answers with a consistent body:

```json
{
  "timestamp": "2026-08-14T10:30:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "You cannot send a friend request to yourself",
  "path": "/friendships"
}
```

Status codes used by the business rules: `400` field validation, `401` invalid credentials or missing/expired token, `403` operation on another user's resource, `404` missing resource, `409` conflict (email already registered, duplicate like or friendship), `422` business rule violated.

When more than one field is invalid, `message` lists all of them at once, in the format `field: reason`, separated by `; ` and sorted by field name. Validation messages come from `ValidationMessages.properties`, and the locale is fixed to English so the responses don't depend on the client's `Accept-Language`.

An unexpected error returns `500` with a generic message: the exception details only go to the server log, never to the response.

## Project structure

```
src/main/java/com/devconnect/api/
├── core/                 — OpenAPI configuration, pagination, text helpers and NowService
├── exception/handler/    — ApiExceptionHandler
├── security/             — SecurityConfig, CorsConfig and login (JWT with RSA)
├── user/                 — sign up, profile, search and editing
├── post/                 — publishing, feed, visibility and deletion
├── comment/              — post comments
├── like/                 — likes and unlikes
└── friendship/           — requests, acceptance, removal and listings
```

## Development

Each feature (e.g. `post`, `friendship`) follows the same layout:

```
<feature>/
├── controller/           — endpoints, with request/ and response/
├── domain/               — entities and enums
├── mapper/               — entity → response
├── repository/           — Spring Data JPA
└── service/              — business rules, one service per use case
    └── core/             — reusable Find*, Validate* and Enrich* services
```

Controllers only expose `request` and `response` objects; an entity is never returned, nor nested inside a response. Every service, validator and mapper has unit tests (`@ExtendWith(MockitoExtension.class)`), and the complete flows have integration tests (`@SpringBootTest` + Testcontainers).
