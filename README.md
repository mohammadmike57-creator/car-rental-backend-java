# NCT Rental — server

Spring Boot 3 (Java 17) server for the NCT Rental system. One Render service runs it at
www.nctrental.com: it serves the website, the staff API and the broker XML/REST gateway
(brokers see the brand **UR-Drive Jordan** only).

## What is where

- `src/main/java/com/carrental/backend/`
  - `security/`, `config/` — login (JWT), the server secret, CORS, rate limits
  - `controller/`, `service/` — staff API; `StateSectionGuard` checks who may change which data
  - `ota/` — broker gateway: OTA XML (REST and SOAP), `/api/v3`, OAuth clients, webhooks, rates
  - `notify/` — email (SMTP settings are stored encrypted)
  - `audit/`, `staff/` — activity log, staff accounts
- `src/test/` — unit and HTTP tests (`mvn test`)
- The website sources at the top level are a copy of the website repository; `Dockerfile` builds
  them and puts them inside the server.

## Settings (environment variables on Render)

Required:

| Name | What |
|------|------|
| `JWT_SECRET` | Random value, at least 32 characters. The server will not start without it. It signs logins and protects stored secrets (SMTP password, connector keys). |
| `JDBC_DATABASE_URL` or `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL |

Optional: `JWT_SECRET_PREVIOUS` (old secret(s), comma separated, kept while changing the secret
so logins and stored secrets keep working), `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS`, `MAIL_*`,
`LOG_LEVEL_*`, `SESSION_*`.

Monitoring: `ALERT_EMAIL` (error summary emails, at most every 30 minutes) and Render's Health Check Path `/health` (OK only while the database answers). Errors get a reference like `ERR-7K2M9Q` and are listed in Activity Log → System errors.

Data storage: `STORAGE_MODE` = `document` (default, as before), `dual`, `tables` or `tables-only` (see `docs/database-plan.md`; change one step at a time, with a Render export first).

Never commit secret values. `docs/application.example.yml` lists the settings without values.

## Changing the server secret

1. Set `JWT_SECRET_PREVIOUS` to the current value and `JWT_SECRET` to a new random value.
2. Deploy. Everyone stays logged in and stored secrets still open.
3. Re-save the SMTP and connector settings once so they are protected with the new secret, then
   remove `JWT_SECRET_PREVIOUS` after the login lifetime has passed.

## Working on it

```bash
mvn test                 # all tests
mvn spring-boot:run      # needs JWT_SECRET and a database
docker build -t nct .    # same build Render runs
```
