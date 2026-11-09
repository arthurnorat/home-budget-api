# Home Budget API — Project Context

## Goal
REST API for a household expense tracker shared by two users. Clients: a web app (Angular), native apps for iPhone (Swift), Mac (Swift) and Android (Kotlin). The clients live in their own repositories.

## Stack
- **Java 21 + Spring Boot 3.5** — REST API (Spring Web, Bean Validation)
- **Firestore** (native mode) — database, accessed with the native `google-cloud-firestore` SDK
- **Google Cloud Run** — hosting, built from source by Google buildpacks (`project.toml` pins Java 21)
- **Firestore emulator** — local development

## Features
- Create, list (by month), update and delete expenses: description, amount, date and category
- Categories: `FIXED` or `VARIABLE` (more categories may be added later)
- Monthly income: one amount per month
- Data shared between the two users, with no authorship

## Authentication
None yet. The API is public (`--allow-unauthenticated` on Cloud Run) and meant for private use between two known users.
Planned (Phase 3): Firebase Auth in the clients sending a bearer token, and Spring Security in the API validating it and allowing only the two known user ids.

## Data model
See the "Data model" section of `README.md` (Firestore collections `expenses` and `monthly_income`). It is internal to the API: clients only see the contract below.

## Conventions
- Monetary values stored in cents (integer), displayed as R$ 0,00
- Dates in ISO 8601 (`YYYY-MM-DD`), displayed as DD/MM/YYYY
- All code in English: variables, class and method names, comments
- Commits in English

## Architecture
- Controllers (`ExpenseController`, `MonthlyIncomeController`) only handle HTTP: validate input, call the services and return DTOs. They never contain business logic.
- Business logic lives only in the services (`ExpenseService`, `MonthlyIncomeService`).
- Data access only through the repositories (`ExpenseRepository`, `MonthlyIncomeRepository`), built on the Firestore SDK. `FirestoreSupport.await` turns SDK failures into `PersistenceException`.
- Entities (`Expense`, `MonthlyIncome`) and DTOs (`expense/dto`, `income/dto`) are kept separate: controllers never expose entities in responses.
- Domain exceptions (e.g. `ExpenseNotFoundException`, `PersistenceException`) are mapped to HTTP responses in a single `GlobalExceptionHandler` (`@RestControllerAdvice`), not handled in each controller.
- Packages are organized by feature (`expense`, `income`), not by layer, with a shared `exception` package.

## Out of scope
Besides the defaults of the global CLAUDE.md, there is no extra project-specific restriction at the moment.

## Infrastructure
- **Cloud Run** (backend) — `https://home-budget-api-689350097433.southamerica-east1.run.app`, region `southamerica-east1`; environment variables: `GOOGLE_CLOUD_PROJECT` (Firestore project) and `JAVA_TOOL_OPTIONS` (`-XX:MaxRAMPercentage=75`); built by buildpacks (`project.toml`), see [step-16](docs/steps/step-16-cloud-run-deploy.md)
- **Firestore** (database) — native mode, region `southamerica-east1`; collections `expenses` and `monthly_income`
- **Vercel** (web frontend) — `https://home-budget-web-ten.vercel.app`
- **Render and Neon** (old infrastructure) — still exist as a fallback but are out of use: Render's Auto-Deploy is off. To be deleted later; Neon variables: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`
- **cron-job.org** — the keep-warm job was **disabled** (not deleted), see [step-17](docs/steps/step-17-cutover.md)

## Status
Current: step-17 done (cutover), with the documentation aligned to the Firestore + Cloud Run architecture. The four clients (web, Android, iOS, macOS) call Cloud Run; Firestore started empty, so the Neon data was not migrated.
Next: Phase 3, authentication (step-18 onwards). Pending after the rollback window: delete Render and Neon (only after the decision on the end of the Google Cloud free trial) and turn on Firestore's delete protection.
Cancelled: Actuator liveness probe, Postgres on a home server, Neon quota monitoring and time-based ping (reasons in [step-12](docs/steps/step-12-keep-warm-workflow.md)).

## API Contract

This file is the source of truth for the API. The web frontend and the native clients import this CLAUDE.md with `@import` to get this contract without duplication.

### URLs
- **Development:** `http://localhost:8080`
- **Production:** `https://home-budget-api-689350097433.southamerica-east1.run.app`

### Endpoints

**POST /expenses** — Create an expense (201)
```json
// Request
{ "description": "Supermercado", "amount": 15000, "date": "2026-04-25", "category": "VARIABLE" }

// Response
{ "expenseId": "uuid", "description": "Supermercado", "amount": 15000, "date": "2026-04-25", "category": "VARIABLE", "createdAt": "2026-04-25T10:30:00Z" }
```

**GET /expenses?month=yyyy-MM** — List the month's expenses (200)
```json
// Response — array of expenses in the same format as above
```

**PUT /expenses/{id}** — Update an expense (200)
```json
// Request — same format as POST
// Response — the updated expense in the same format as above
```

**DELETE /expenses/{id}** — Delete an expense (204, no body)

**GET /income?month=yyyy-MM** — Get the month's income (200)
```json
// Response
{ "month": "2026-06", "amount": 500000 }
// Returns amount=0 if no income was registered for the month
```

**PUT /income?month=yyyy-MM** — Save or update the month's income (200)
```json
// Request
{ "amount": 500000 }
// Response
{ "month": "2026-06", "amount": 500000 }
```

### Fields and types
| Field | Type | Note |
|-------|------|------|
| `expenseId` | UUID (string) | Generated automatically |
| `description` | string | Required, at least 1 character |
| `amount` | integer | In **cents** (R$ 1,00 = 100) |
| `date` | ISO 8601 string | Format `YYYY-MM-DD` |
| `category` | string enum | `"FIXED"` or `"VARIABLE"` |
| `createdAt` | ISO 8601 string | UTC timestamp, generated automatically |

### CORS
Currently allowed origins: `http://localhost:4200`, `https://home-budget-web-ten.vercel.app`

Only browser clients need an allowed origin (add it in `WebConfig.java`). Native apps (iOS, macOS, Android) are not subject to CORS.

### Authentication
None yet. The API is public and meant for private use (see "Authentication" above).
