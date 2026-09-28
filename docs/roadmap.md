# Roadmap

Each step is a separate branch and PR. Commits follow Conventional Commits.

## Phase 0: Foundation

0. **Commit project docs**
   `CLAUDE.md` and this roadmap, as one small `docs:` PR.

1. **Git hygiene**
   Untrack log files and `build/` output (only `build/.../ContactMapperImpl.java` is tracked), fix `.gitignore` (logs/, *.log, .env, .env.*, !.env.example), and commit the executable bit on `gradlew` (`git update-index --chmod=+x gradlew`).
   Note: untracking does not remove old log files from history.

2. **ADR setup**
   Create `docs/adr/` with a template. I will write the first ADR by hand; do not generate it.
   Candidate first ADR: how to handle the credentials already in git history (rotate vs rewrite history).

3. **Externalize configuration**
   Move credentials, URLs and the Hazelcast address/cluster name to environment variables. Commit a `.env.example`; `.env` stays ignored.
   - Define how `.env` is loaded locally (e.g. `spring.config.import=optional:file:.env[.properties]` or IntelliJ run configurations).
   - The current DB credentials are in git history on GitHub: rotate them (and/or rewrite history, per the ADR).

4. **Remove dead code**
   Delete commented-out files and unused code (e.g. the unused `uriComponents` copies of the city URL). Git keeps the history.

5. **Fix known bugs**
   - `RateLimiterService` starts a `new Timer()` thread per request (resource leak), and its check and increment are not atomic.
   - Circuit breaker `waitDurationInOpenState: 10000s` (~2.8 h; milliseconds were intended).
   - `RestTemplate` has no connect/read timeouts.
   - `GlobalExceptionHandler` throws inside an `@ExceptionHandler`; `NoDataFoundException` returns 400 instead of 404.
   - Stale caches: no eviction on writes (short-term: evict on save/update/delete; full redesign in Phase 4).
   - `GlobalExceptionHandler` returns raw exception messages to clients (e.g. the internal city-service URL in an I/O error).
   - Report export fails with `JRException: Unable to load report`: the `.jrxml` uses the pre-7 format, but the project uses JasperReports 7. `ResourceUtils.getFile` also fails inside a packaged jar.

6. **Abstract the city service**
   Put the external city lookup behind an interface and add a local stub implementation, so the app and tests don't depend on the external service.
   - The stub is active only under the `local` and `test` profiles; every other profile uses the real client.
   - Look the city up in the service layer, not inside the MapStruct mapper (`BirthCityConverter` currently makes an HTTP call during mapping).
   - The real client gets timeouts and a circuit-breaker fallback.

7. **Fix the Hazelcast setup**
   Before testing with it: `CacheService`'s no-arg constructor starts an embedded member next to the client from `HazelcastClientConfig`, and `hazelcast-jet-spring-boot-starter` is the wrong artifact (replace it with `com.hazelcast:hazelcast`). Use one client-server setup. This changes dependencies; ask before changing `build.gradle`.

8. **Test baseline**
   Add Testcontainers for PostgreSQL and Hazelcast so tests run without a manually configured local environment. This adds dependencies; ask before changing `build.gradle`.
   - Use `@ServiceConnection` for container wiring.
   - Write integration tests for the current endpoints, not only `contextLoads`. They protect the upgrade and are the baseline for the API redesign.
   - Needs Docker locally.

9. **Spring Boot upgrade**
   3.2 → 3.5 (clear deprecation warnings) → 4.1. Tests from step 8 must pass at each stage.
   - 3.5 stage: dependency cleanup (drop the Liquibase version pin; remove `commons-io` if unused and `spring-boot-starter-logging`; align springdoc).
   - Before 4.1: check that every third-party library supports Boot 4 / Spring Framework 7 (springdoc, resilience4j, JasperReports, Hazelcast, later Spring AI). A library can block this stage.

## Phase 1: API redesign (resource-style REST)
Must come after the upgrade (Spring Boot 4 has built-in API versioning) and before authentication (security rules depend on URL patterns).
- Resource-based URLs with HTTP verbs, under `/api/v1/` (e.g. `POST /api/v1/persons`, `DELETE /api/v1/contacts/{id}`)
- Correct status codes (201 + Location, 204, 404)
- Pagination for list endpoints
- Errors as `ProblemDetail` (RFC 9457); whether to keep the `GenericDTO` envelope is decided in an ADR
- Controllers grouped by resource instead of one per operation
- Process: endpoint inventory → ADR (written by me) → integration tests first → migrate one resource per PR

## Later phases (outline)
2. **Logging:** correlation ID (MDC + response header + outbound propagation), structured JSON logs, stop logging all headers.
   Must come before authentication, so JWTs never reach the logs.
3. **Docker Compose:** app, PostgreSQL, Hazelcast; healthchecks; the report template must load from the classpath stream (it breaks inside a jar today).
4. **Caching:** Spring Cache abstraction with Hazelcast, TTLs, eviction on writes; cache the city lookup.
5. **Authentication & authorization:** Spring Security, JWT, roles; decide user ↔ Person ownership in an ADR.
6. **Load balancer:** nginx with multiple instances; shared rate-limit state; forwarded headers.
7. **LLM chatbot:** Spring AI with tool calling; read-only tools first; authorization enforced inside tools.
8. **Frontend.**
