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

### Runtime dependencies (required even for the `@SpringBootTest` context test)
- PostgreSQL at `localhost:5432/phonebook`, user/password `admin`/`admin` (`src/main/resources/application.yaml`).
- Hazelcast member at `127.0.0.1:5701`, cluster `dev` (`core/config/HazelcastClientConfig` creates a client bean at startup).
- `BirthCityConverter` calls an external city-lookup REST service (`siciltest.gelbim.gov.tr`) while mapping `birthCity` in Person responses.

## Architecture

Package-by-feature (`person/`, `contact/`) with shared infrastructure in `core/`. Each feature has `controller/ → service/ → repository/` plus `dto/`, `entity/` and `mapper/`.

- **One controller + one service per operation** (for example `PersonSaveController`/`PersonSaveService` and `GetAllContactController`/`GetAllContactService`), not one class per resource. Endpoints are RPC-style under `/api` (`/savePerson`, `/getAllContact`, `/updateContact/{id}`, and so on). Adding an endpoint usually means adding a new controller/service pair.
- **Response envelope:** every endpoint returns `ResponseEntity<GenericDTO<T>>` (`core/dto/GenericDTO`: `body`, `errorStatus` (0 = ok, 1 = error), `errorMessage`). `core/exception/GlobalExceptionHandler` maps `NoDataFoundException`, `ValidationControlException`, `RateLimitException` (429), resilience4j `CallNotPermittedException` and generic `RuntimeException` into this envelope.
- **Mapping:** MapStruct interfaces in `*/mapper/` are compiled with `defaultComponentModel=spring` and constructor injection (compiler args in `build.gradle`). Date strings use the `dd-MM-yyyy` format through `@Named` helpers. Reusable mapping helpers live in `core/mapperhelpermethods/` and are pulled in with `@Mapper(uses = ...)`. Generated `*MapperImpl` classes are written to `build/generated/...`; don't edit them.
- **Validation:** custom Bean Validation annotations and their validators live in `core/validation/` (`@ValidName`, `@ValidPhoneNumber` using libphonenumber, `@ValidBirthdate`, `@ValidGender`, `@ValidPersonId`, `@ValidContactInfo`). They are applied on request DTOs, and controllers use `@Validated` + `@Valid`.
- **Rate limiting:** annotate a controller method with `@RateLimited(service = "KEY")` (`core/ratelimitedannotation`). `core/aspect/RateLimiterOrientedAspect` intercepts it and calls `core/service/RateLimiterService`, which keeps in-memory per-IP counters. Limits per key come from `rate-limiter.limits.<KEY>` in `application.yaml`, bound by `RateLimiterConfig`. A key with no entry is not limited. `core/filtering/RateLimitingFilter` is an old, fully commented-out approach.
- **Circuit breaker:** resilience4j `@CircuitBreaker(name = "exampleService")` on services, configured in `application.yaml`.
- **Schema:** Liquibase owns the schema (`ddl-auto: none`). The changelog entry point is `src/main/resources/db/master.xml`, which includes `db/script/person.xml` and `db/script/contact.xml`. Add schema changes as Liquibase changesets, not entity-driven DDL.
- **Other `core/` pieces:** request/header logging filters and interceptors (`logging/`, `loggingfilter/`, `filtering/`), a Hazelcast-backed `cache/CacheService`, and JasperReports export (`report/`, template `src/main/resources/reports/sample-report.jrxml`). Logging config is `src/main/resources/logback.xml`, which writes to `logs/`.

## Notes
- Many code comments are in Turkish.
- `build/` and `logs/` contain git-tracked files even though `build/` is in `.gitignore`. Don't stage changes to them unless you mean to.

## Working agreements
- This is a learning project. When making architectural or security changes, explain what you did and why.
- Architectural decisions are recorded in docs/adr/. Read relevant ADRs before proposing changes; propose a new ADR for new decisions.
- Ask before adding dependencies to build.gradle.
- Use Conventional Commits (feat:, fix:, chore:, docs:, refactor:, test:).
- Never push. I review and push myself.
## ## Roadmap
The current plan is in docs/roadmap.md. Check it before starting a task and don't work ahead of the current step.