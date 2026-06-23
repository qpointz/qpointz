# WI-324 — Java 25 platform bump (OData prerequisite)

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** WI-325  
**Blocks:** WI-326 (RWS `com.sdl` 2.16.x requires bytecode 69 / JDK 25)

## Goal

Raise the Mill platform toolchain from **Java 21** to **Java 25** so RWS/SDL OData **2.16.1** artifacts compile and run on CI, Docker, and developer machines.

**Before coding:** read [`STORY.md`](STORY.md) § Protocol stack (RWS 2.16.x).

## Rationale

RWS `com.sdl:odata_parser` **2.14.1+** (including **2.16.1**) ships **Java 25** bytecode. Mill cannot depend on RWS OData HTTP/parser modules while pinned to JDK 21.

## Scope

| Area | Action |
|------|--------|
| [`build-logic/.../MillPlugin.kt`](../../../../build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillPlugin.kt) | `JavaLanguageVersion.of(25)`; Kotlin `jvmToolchain(25)` |
| [`.gitlab/templates/gradle.yml`](../../../../.gitlab/templates/gradle.yml) | `zulu-openjdk:25-latest` |
| Docker / packaging | JDBC shell, mill-service images referencing JDK 21 → 25 |
| Docs | `COLDSTART.md`, `CLAUDE.md`, public quickstart JDK lines |
| Verify | Root `./gradlew build`; representative `testIT` on CI |

## Out of scope

- RWS / OData modules (WI-326+)
- Feature work unrelated to JDK compatibility

## Acceptance

- `./gradlew build` green on JDK 25 CI image
- No remaining repo references requiring JDK 21 as the **platform** minimum (module-specific docs may note client JDK separately if needed)
- `STORY.md` WI-324 checked; one `[change]` commit before WI-326

## Risks

- Third-party deps (Calcite, gRPC, Spring AI / LangChain4j, Kotlin) on JDK 25 — fix in this WI before OData code lands

## Completion (normative)

After verify passes: update **tracker** (`STORY.md` `[x]`, this file status) → **commit** (include tracker in commit) → **push** → **CI green**. See [`STORY.md`](STORY.md) § Implementation delivery workflow.
