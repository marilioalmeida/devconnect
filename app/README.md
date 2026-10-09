# DevConnect App

Angular SPA for DevConnect, a social network for developers. It consumes the [Spring Boot API](../api/README.md).

## Tech stack

- **Angular 22** with standalone components, signals, zoneless change detection and lazy routes
- **Angular Material** and Angular CDK, with a custom theme and dark mode
- **`httpResource()`** for reads and **`@angular/forms/signals`** for forms
- **highlight.js**, loaded on demand, for code blocks in posts
- **TypeScript 6**
- **Vitest** for tests

## Requirements

- Node.js with npm
- The DevConnect API running at `http://localhost:8080` (see [api/README.md](../api/README.md))

## Running

```bash
npm install
npm start
```

The app starts at `http://localhost:4200` and reloads on every change.

If the database has the demo data loaded (see the [root README](../README.md)), log in as `marilio@devconnect.com` with the password `password123` to find a feed, a friends list and friend requests already in place. Without the demo data, start from the sign-up screen.

To see both sides of a friendship at the same time, use two browser windows, one of them private: the token is stored in `localStorage`, so two regular tabs share the same session.

## API URL

The API address comes from `src/environments/`:

| File | Used by |
| --- | --- |
| `environment.development.ts` | `npm start` and `npm run watch` |
| `environment.ts` | `npm run build` (production) |

Both currently point to `http://localhost:8080`. To deploy somewhere else, change `apiUrl` in `environment.ts`.

## Scripts

| Command | What it does |
| --- | --- |
| `npm start` | Development server at `http://localhost:4200` |
| `npm run build` | Production build in `dist/` |
| `npm run watch` | Development build in watch mode |
| `npm test` | Tests with Vitest |

## Screens

| Route | Screen | Access |
| --- | --- | --- |
| `/login` | Log in | `guestGuard` |
| `/signup` | Create an account | `guestGuard` |
| `/home` | Paginated feed, post composer and received friend requests | `authenticatedGuard` |
| `/discover` | Search users by name or email and send friend requests | `authenticatedGuard` |
| `/friends` | Friends list with a filter and the option to remove a friend | `authenticatedGuard` |
| `/edit-profile` | Edit your name, nickname and profile image | `authenticatedGuard` |
| `/profile/:id` | Another user's profile, their visible posts and the friendship action | `authenticatedGuard` |

`guestGuard` redirects users who are already logged in to `/home`; `authenticatedGuard` sends users who aren't to `/login`. The root and any unknown route go to `/home`.

## Structure

```
src/app/
├── core/               — shared infrastructure
│   ├── auth/           — session service, interceptors and guards
│   ├── models/         — interfaces mirroring the API contracts
│   ├── notification/   — MatSnackBar wrapper
│   ├── theme/ · time/  — light/dark theme and relative time
│   └── user · post · comment · like · friendship — HTTP clients for the API
├── features/           — one folder per screen
└── shared/             — components reused across screens
                          (post-card, post-comments, post-content, confirm-dialog)
```

Each service in `core/` matches a feature of the API: reads use `httpResource()`, which exposes the data as a signal with built-in loading state, and writes return an `Observable`.

## Authentication

The JWT is stored in `localStorage` under the `devconnect.token` key and exposed as a signal by the `Auth` service.

- **`auth-interceptor`** adds `Authorization: Bearer <token>` to every request, except `POST /login` and `POST /users`, which are public.
- **`error-interceptor`** handles failures in one place: it shows the API message (or a message based on the status) in a snackbar, warns when the API is down (status 0) and, on a `401`, ends the session and redirects to `/login`. Requests that handle their own errors set the `SKIP_GLOBAL_ERROR_HANDLER` context token.
- On startup, `restoreSession()` checks the token expiration (`exp`) locally before any request. An expired token is discarded without calling the server, which avoids the screen flashing as logged in and then falling back to the login page.

## Tests

```bash
npm test
```

There are 40 tests, focused on the authentication infrastructure (session service, token expiration and both interceptors) and on the post content parser that splits text, code blocks, inline code and links. Screen components don't have tests yet.
