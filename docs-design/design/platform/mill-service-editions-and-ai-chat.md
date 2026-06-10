# Mill service editions: AI v3 chat (`mill-ai-v3-autoconfigure` starter)

## Context

**`apps/mill-service`** is the main Spring Boot assembly for Mill. The **`ai`** edition adds **`mill-ai-v3-autoconfigure`**, which pulls **`mill-ai-v3-service`** (unified AI v3 chat REST + SSE) transitively; editions without **`ai-chat-service`** omit both. The **`io.qpointz`** component scan in [`MillService`](../../../apps/mill-service/src/main/java/io/qpointz/mill/app/MillService.java) only picks up controllers from modules that are **dependencies** of `mill-service`.

Edition resolution and **feature → `implementation` module wiring** are implemented by the Mill plugin (`configureEditionPackaging`); see [`docs/design/build-system/gradle-editions.md`](../build-system/gradle-editions.md).

## Feature: `ai-chat-service`

| Gradle module | Role |
|---------------|------|
| [`:ai:mill-ai-v3-autoconfigure`](../../../ai/mill-ai-v3-autoconfigure/) | **Starter:** Spring Boot auto-config + transitively **`mill-ai-v3-service`** (HTTP/SSE API, OpenAPI). Also pulls **`mill-ai-v3-data`**, etc. |
| [`:ai:mill-ai-v3-persistence`](../../../ai/mill-ai-v3-persistence/) | JPA entities and adapters for chat metadata, transcript, memory, artifacts. Enables **`AiV3JpaConfiguration`** when JPA repositories are present (same pattern as AI v3 integration tests). |

Without this feature, running only **`mill-ai-v3-cli`** against a host that never enabled the feature would fail to find chat routes.

## Edition: `ai`

- **Extends:** **`minimal`** (inherits `data-services` and all existing `mill-service` dependencies).
- **Adds:** feature **`ai-chat-service`**.

**Default edition** in `apps/mill-service/build.gradle.kts` is **`ai`** so a standard `bootRun` / distribution includes AI v3 chat. To build **without** AI modules, pass **`-Pedition=minimal`** (or another edition that omits `ai-chat-service`).

```bash
./gradlew :apps:mill-service:bootRun -Pedition=ai --args='--spring.profiles.active=ai'
```

The Spring profile **`ai`** is declared in [`application.yml`](../../../apps/mill-service/application.yml) and [`src/main/resources/application.yml`](../../../apps/mill-service/src/main/resources/application.yml). It sets **`mill.ai.model`** and **`mill.ai.chat`** (API key via **`OPENAI_API_KEY`**, optional **`OPENAI_MODEL`** / **`OPENAI_BASE_URL`**). The Gradle edition only adds JARs; the profile activates AI configuration binding.

Distribution output is edition-scoped (see `gradle-editions.md`), e.g. `build/install/mill-service-boot-ai`.

Other editions (`integration`, `samples`) are unchanged; add **`ai-chat-service`** to them later if product requires combined installs.

## Runtime configuration

- **LLM:** `mill.ai.model.*` (API key, model name, optional base URL) — see [`mill-ai-v3-autoconfigure/README.md`](../../../ai/mill-ai-v3-autoconfigure/README.md) and [`docs/design/agentic/v3-chat-service.md`](../agentic/v3-chat-service.md).
- **Schema-capable profiles** (`schema-authoring`, …): the host must supply **`SchemaFacetService`**, **`SqlProvider`**, **`SqlDialectSpec`**, etc.; a full Mill service already pulls data lane modules; see autoconfigure README for the exact bean matrix.
- **Security:** align `UserIdResolver` / chat `default-user-id` with your deployment (`mill.ai.chat.*`).

## Related

- [`gradle-editions.md`](../build-system/gradle-editions.md) — edition DSL and dependency wiring
- [`docs/design/agentic/v3-chat-service.md`](../agentic/v3-chat-service.md) — HTTP contract
- [`ai/mill-ai-v3-cli/README.md`](../../../ai/mill-ai-v3-cli/README.md) — HTTP test bench against a running service
