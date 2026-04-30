# Installation

## Runtime requirements

- **Java 21** — Use JDK **21** to build and run Mill’s Spring Boot services. The stack targets **Spring Boot 4**, **Spring Framework 7**, and **Spring AI 2.0** milestone line; those combinations assume Java **21** (see [Platform runtime](reference/platform-runtime.md) for a concise version table).

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
- [Spring Boot 3.5 to 4.0 migration plan](../../../design/platform/spring4-migration-plan.md) — engineering migration checklist (design doc in the same repo)
