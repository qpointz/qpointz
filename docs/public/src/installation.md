# Installation

## Runtime requirements

- **Java 25** — Use JDK **25** to build and run Mill’s Spring Boot services. The platform was raised to Java 25 for RWS OData 2.16.x and current Spring Boot 4 / Spring Framework 7 stacks (see [Platform runtime](reference/platform-runtime.md)).

## Build from source

Clone the repository and run Gradle from the **repository root**:

```bash
./gradlew build
```

Module-scoped tasks use the same wrapper, for example:

```bash
./gradlew :apps:mill-service:bootRun
```

Integration tests (where a module defines a `testIT` suite):

```bash
./gradlew testIT
```

## Docker and samples

For a guided first run with sample data and containers, use the [Quickstart](quickstart.md).

## Further reading

- [Platform runtime](reference/platform-runtime.md) — Spring Boot, Jackson, Spring AI, gRPC, and Jakarta baselines
- [Security](security/index.md) — OIDC sign-in (for example [Authentik](security/authentik-oidc.md))
- [Spring Boot 3.5 to 4.0 migration plan](../../../design/platform/spring4-migration-plan.md) — engineering migration checklist (design doc in the same repo)
