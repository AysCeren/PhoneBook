# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.2 / Java 17 training project (Gradle project name `contactsdemo`, base package `com.project.contactsdemo`) modelling a phone book: `Person` 1→N `Contact`, persisted in PostgreSQL.

## Commands

```bash
./gradlew build                      # compile (runs Lombok + MapStruct annotation processors) and test
./gradlew bootRun                    # run the app
./gradlew test                       # all tests
./gradlew test --tests 'com.project.contactsdemo.ContactsdemoApplicationTests'   # single test class
./gradlew test --tests '*ClassName.methodName'                                   # single test method
```

Swagger UI (springdoc) is at `/swagger-ui.html` when running.

### Configuration and runtime dependencies
- Config values come from environment variables; locally from a git-ignored `.env` (copy `.env.example`), loaded via `spring.config.import`. Real env vars override `.env`. In `.env`, activate profiles as `spring.profiles.active=local` (the `SPRING_PROFILES_ACTIVE` form only works as a real env var).
- PostgreSQL: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (no defaults; startup fails if missing). Required for `bootRun` and the `@SpringBootTest` context test.
- Hazelcast: the app is a **client** of a separate cluster (`HAZELCAST_ADDRESS`, default `127.0.0.1:5701`, cluster `dev`). Locally: `docker run -d --name phonebook-hazelcast -p 5701:5701 hazelcast/hazelcast:5.3.7` (match the client version managed by Spring Boot). The client starts asynchronously; without a cluster, cache operations log warnings and the app serves from the database.
- City names: `core/city/CityLookup`. `HttpCityLookup` calls the external service (`CITY_SERVICE_BASE_URL`, internal network only) with timeouts, the `cityService` circuit breaker and a fallback; `StubCityLookup` (built-in plate-code table) is used under the `local` and `test` profiles. Services resolve names via `CityNameResolver`, not in mappers.

## Architecture

Package-by-feature (`person/`, `contact/`) with shared infrastructure in `core/`. Each feature has `controller/ → service/ → repository/` plus `dto/`, `entity/` and `mapper/`.

- **One controller + one service per operation** (for example `PersonSaveController`/`PersonSaveService` and `GetAllContactController`/`GetAllContactService`), not one class per resource. Endpoints are RPC-style under `/api` (`/savePerson`, `/getAllContact`, `/updateContact/{id}`, and so on). Adding an endpoint usually means adding a new controller/service pair.
- **Response envelope:** every endpoint returns `ResponseEntity<GenericDTO<T>>` (`core/dto/GenericDTO`: `body`, `errorStatus` (0 = ok, 1 = error), `errorMessage`). `core/exception/GlobalExceptionHandler` maps exceptions into this envelope with matching statuses (404 not found, 400 validation, 429 rate limit, 503/502 for external-service failures and open circuit breaker). Unexpected exceptions get a generic message and are logged in full; never return raw exception messages to clients.
- **Mapping:** MapStruct interfaces in `*/mapper/` are compiled with `defaultComponentModel=spring` and constructor injection (compiler args in `build.gradle`). Date strings use the `dd-MM-yyyy` format through `@Named` helpers. Reusable mapping helpers live in `core/mapperhelpermethods/` and are pulled in with `@Mapper(uses = ...)`. Mappers must not do I/O (HTTP lookups belong in services). Generated `*MapperImpl` classes are written to `build/generated/...`; don't edit them.
- **Validation:** custom Bean Validation annotations and their validators live in `core/validation/` (`@ValidName`, `@ValidPhoneNumber` using libphonenumber, `@ValidBirthdate`, `@ValidGender`, `@ValidPersonId`). They are applied on request DTOs, and controllers use `@Validated` + `@Valid`. The custom validators throw `ValidationControlException` on the first error instead of returning `false`.
- **Rate limiting:** annotate a controller method with `@RateLimited(service = "KEY")` (`core/ratelimitedannotation`). `core/aspect/RateLimiterOrientedAspect` intercepts it and calls `core/service/RateLimiterService`, which keeps an in-memory fixed window per client IP and key (updated atomically with `ConcurrentHashMap.compute`, expired windows removed by a `@Scheduled` task). Limits per key come from `rate-limiter.limits.<KEY>` in `application.yaml`, bound by `RateLimiterConfig`. A key with no entry is not limited.
- **Circuit breaker:** resilience4j, one instance per external dependency (currently `cityService` on `HttpCityLookup`, with a fallback), configured in `application.yaml`. Don't put breakers on database-only operations.
- **Schema:** Liquibase owns the schema (`ddl-auto: none`). The changelog entry point is `src/main/resources/db/master.xml`, which includes `db/script/person.xml` and `db/script/contact.xml`. Add schema changes as Liquibase changesets, not entity-driven DDL.
- **Caching:** `core/cache/CacheService` (cache-aside on Hazelcast maps named in `CacheNames`). Writes call `clearAfterCommit(...)` for the affected maps; cached values must be `Serializable`.
- **Other `core/` pieces:** request/header logging filters (`loggingfilter/`, `filtering/`) and JasperReports export (`report/`, template `src/main/resources/reports/sample-report.jrxml` in the JasperReports 7 format, compiled once at startup). Logging config is `src/main/resources/logback.xml`, which writes to `logs/`.

## Notes
- Many code comments are in Turkish.
- Unit tests in `src/test` don't need a database; `ContactsdemoApplicationTests` needs PostgreSQL and runs with the `test` profile.

## Working agreements
- This is a learning project. When making architectural or security changes, explain what you did and why.
- Architectural decisions are recorded in docs/adr/. Read relevant ADRs before proposing changes; propose a new ADR for new decisions.
- Ask before adding dependencies to build.gradle.
- Use Conventional Commits (feat:, fix:, chore:, docs:, refactor:, test:).
- Never push. I review and push myself.
## Roadmap
The current plan is in docs/roadmap.md. Check it before starting a task and don't work ahead of the current step.