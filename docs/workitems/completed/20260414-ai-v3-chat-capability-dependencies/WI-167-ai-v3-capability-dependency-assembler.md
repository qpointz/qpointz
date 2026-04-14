# WI-167 — `CapabilityDependencyAssembler` contract and Spring wiring

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Milestone: `TBD`

## Problem Statement

Chat runtimes must populate **`AgentContext.capabilityDependencies`** for profiles that declare **`schema`**, **`sql-dialect`**, **`sql-query`**, **`value-mapping`**. The **mechanism** should be explicit, testable, and **framework-free at the contract** so **`mill-ai-v3`** stays portable while **`mill-ai-v3-autoconfigure`** owns Spring beans.

## Goal

Introduce a **`CapabilityDependencyAssembler`** (name final in implementation) in **`mill-ai-v3`**: a small abstraction that, given **`AgentProfile`** and persisted **`ChatMetadata`** (and/or the coarse fields already produced by **`ProfileRegistry.rehydrate`**), returns a **`CapabilityDependencyContainer`** aligned with **`SchemaExplorationAgent.buildContext()`** — same capability-id keys and **`CapabilityDependencies`** shapes.

Provide a **default Spring** implementation in **`mill-ai-v3-autoconfigure`** that injects optional collaborators and wire **`LangChain4jChatRuntime`** to apply the assembler output to rehydrated **`AgentContext`**.

## Merge semantics (`AgentContext`)

**Current code (pre-WI-167):** [`LangChain4jChatRuntime`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt) passes **`rehydration.agentContext`** through unchanged; [`ChatRehydration.kt`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/ChatRehydration.kt) (`rehydrate`) builds only coarse fields and **`capabilityDependencies = empty()`**.

**Normative rule — not a three-way “merge”:** For each **`send`**, construct **`AgentContext`** as:

| Field | Source |
|-------|--------|
| `contextType`, `focusEntityType`, `focusEntityId` | **From** **`rehydrate`** only (unchanged). |
| `capabilityDependencies` | **Replace wholesale** with **`assembler` output** for this turn. |

- **Do not** union/overlay **`rehydration.agentContext.capabilityDependencies`** with assembler output. Today rehydration’s container is always **empty**; if it were ever non-empty, WI-167 still **replaces** the entire **`CapabilityDependencyContainer`** with the assembler result unless a **future WI** explicitly changes this contract (e.g. persisted overrides).
- **Do not** partially patch individual capability slots unless specified elsewhere; one **`CapabilityDependencyContainer`** per turn from the assembler.

## Failure semantics (partial Spring stacks)

**Aligned with [`CapabilityRegistry.capabilitiesFor`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityRegistry.kt)** → **`validateDependencies`** → **`require`** on missing **`CapabilityDependencies`** entries.

| Stage | Fails? |
|-------|--------|
| **`GET /api/v1/ai/profiles`** | **No** — does not instantiate capabilities. |
| **`POST /api/v1/ai/chats` (create)** | **No** for dependency validation — **`CapabilityRegistry`** is not run at create; **`profileId`** is stored as a string. |
| **`POST …/messages` (first or later turn)** | **Yes** when the profile requires a dep the assembler cannot fill → **`validateDependencies`** fails → **`IllegalArgumentException`** / reactive error consistent with runtime failures (see **WI-160**). |

- **No** “half working” profile: missing **required** deps for a capability → that capability **does not** construct (hard fail), **not** silent downgrade (except the **explicit** **`ValueMappingResolver`** stub, which is an intentional weak implementation, not a missing bean).

**Optional chat-time preflight** (reject create if stack cannot satisfy profile) is **out of scope** for WI-167.

## Partial Spring classpath / missing collaborators

- **Profile listing** (**`GET /api/v1/ai/profiles`**) and **chat create** are **unchanged** by WI-167; this WI does not add a “supported profile × stack” gate at HTTP create time unless implemented separately.
- **Document:** partial stacks mean **message send / agent run fails** as above; users can still **list profiles**; **optional preflight at create** is **out of scope** for WI-167 unless added later.

## Capability id keys and shared construction (no drift)

- **Today’s duplication risk:** **`SchemaExplorationAgent.buildContext()`** ([`SchemaExplorationAgent.kt`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt) — dependency map ~`CapabilityDependencyContainer.of(...)`) **hardcodes** capability id strings and dependency typings. That is sufficient to copy once, but **brittle** if left only in the agent.
- **Implementation requirement:** introduce a **single shared helper or factory** in **`mill-ai-v3`** (e.g. building a **`CapabilityDependencyContainer`** from **`(AgentProfile, collaborators…)`**) used by **both** **`SchemaExplorationAgent`** (refactor **`buildContext`**) **and** the **Spring `CapabilityDependencyAssembler`** implementation. **`AgentProfile.capabilityIds`** and **`CapabilityDescriptor.id`** remain the **authoritative id strings**; the factory maps each id to the correct **`CapabilityDependencies`** shape.

## Capability id keys (summary)

- The assembler **must** populate **`CapabilityDependencyContainer`** entries **only** for capability ids on the **resolved profile** that need deps, using the **same ids** as the shared factory above — **no** ad hoc literals diverging from **`SchemaAuthoringAgentProfile`** / descriptors.

## Sourcing strategy (reuse existing wiring — do not reinvent)

Implementation **must** pull collaborators from **beans and configuration already provided** by **`mill-ai-v3-autoconfigure`** and, on the host classpath, **`mill-ai-v3-data`**, **`data`**, and **`metadata`** modules as they exist today — **not** duplicate factory logic or parallel property namespaces.

1. **`SchemaCatalogPort`** — use the bean from **`MillAiV3DataAutoConfiguration`** (`SchemaFacetService` → adapter) when present; **`ObjectProvider<SchemaCatalogPort>`** or equivalent.
2. **`SqlValidator` / `SqlQueryToolHandlers.SqlValidationService`** — use **`MillAiV3SqlValidatorAutoConfiguration`** and existing **`SqlProvider`**-backed **`BackendSqlValidator`** wiring when those beans exist.
3. **`SqlDialectSpec`** — on hosts that include **`mill-data-autoconfigure`**, use the existing bean from **`SqlAutoConfiguration`**: **`millDataSqlDialectSpec()`** builds **`SqlDialectSpec`** from **`mill.data.sql.*`** (default dialect key **`MILL_DATA_SQL_CONFIG_KEY`** / `mill.data.sql`). Reference: [`data/mill-data-autoconfigure/.../SqlAutoConfiguration.java`](../../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/SqlAutoConfiguration.java). Inject **`ObjectProvider<SqlDialectSpec>`** (or the bean by type/name) when the autoconfiguration is on the classpath.
4. **`ValueMappingResolver`** — **stub** implementation registered by **`mill-ai-v3-autoconfigure`** when no metadata-backed resolver exists (same role as **`MockValueMappingResolver`** today). **Document the limitation** in README / design: **`value-mapping`** tools are degraded (minimal or empty results) until a real resolver bean is supplied; **`ObjectProvider<ValueMappingResolver>`** prefers real bean, falls back to stub.

**Principle:** the assembler is a **thin combiner** of **already-registered** Spring collaborators. New **`@ConfigurationProperties`** are only justified if no existing metadata/data hook exists; prefer extending **`MillAiV3DataAutoConfiguration`** (or host-specific config) over new standalone config types.

## Module placement

- **Contract + no-op default:** **`ai/mill-ai-v3`** only — no Spring imports.
- **Bean implementation + `LangChain4jChatRuntime` merge:** **`ai/mill-ai-v3-autoconfigure`** — already depends on **`mill-ai-v3-data`** and existing data/SQL autoconfiguration.
- **Not** **`mill-ai-v3-persistence`** — that module is JPA/chat storage; it does not own dialect, SQL validation, or schema catalog collaborators.

## In Scope

1. **Interface / API** in **`mill-ai-v3`** with full KDoc (parameters, return value, threading expectations if any).
2. **No-op implementation** (or documented default on the interface) returning **`CapabilityDependencyContainer.empty()`** so profiles without data deps behave as today.
3. **`@Bean`** in **`mill-ai-v3-autoconfigure`**: `@ConditionalOnMissingBean` production assembler that fills dependencies **from the collaborators above** when beans exist (**see Partial Spring classpath** above).
4. **`LangChain4jChatRuntime`:** inject assembler; after **`rehydrate`**, build **`AgentContext`** = rehydrated coarse fields + **`capabilityDependencies` = assembler output** (see **Merge semantics**).
5. **Gradle:** no new dependency from **`mill-ai-v3-persistence`** into this path.

## Out of Scope (defer to **WI-160** / **WI-169**)

- Full **`testIT`** creating a chat with **`schema-authoring`** and asserting end-to-end message flow without dependency errors (unless trivially included with the wiring).
- Autoconfigure / service **README** “which beans for schema chats.”
- **`mill-ai-v3-cli`** HTTP-only test bench — **WI-169**.

## Acceptance Criteria

- **`CapabilityDependencyAssembler`** (or chosen name) is defined in **`mill-ai-v3`** with KDoc; **no** `org.springframework` imports in that contract file.
- **Shared factory / helper:** **`SchemaExplorationAgent.buildContext()`** is refactored to use the same **`mill-ai-v3`** construction path as the Spring assembler (single source of capability id strings and **`CapabilityDependencies`** shapes).
- **`mill-ai-v3-autoconfigure`** registers a default implementation bean when not overridden; **`LangChain4jChatRuntime`** **replaces** **`capabilityDependencies`** per merge table above (not passthrough of empty rehydration only).
- **`CapabilityDependencyContainer`** keys match **`profile.capabilityIds`** / **`CapabilityDescriptor.id`** for whichever capabilities need deps.

## Deliverables

- This work item definition.
- Code on the story branch per **`docs/workitems/RULES.md`** (typically one commit per WI after work starts).

## Reference

- **`SchemaExplorationAgent.buildContext`:** `ai/mill-ai-v3/.../SchemaExplorationAgent.kt`
- **`LangChain4jChatRuntime`:** `ai/mill-ai-v3-autoconfigure/.../LangChain4jChatRuntime.kt`
- **`MillAiV3DataAutoConfiguration` / `MillAiV3SqlValidatorAutoConfiguration`:** `ai/mill-ai-v3-autoconfigure/`
- **`SqlDialectSpec` (host):** [`data/mill-data-autoconfigure/.../SqlAutoConfiguration.java`](../../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/SqlAutoConfiguration.java)
- Value-mapping **stub** limitation: document in autoconfigure README / **WI-160** (degraded **`value-mapping`** until a real **`ValueMappingResolver`** bean exists)
- Parent acceptance / IT / docs: [**WI-160**](WI-160-ai-v3-chat-runtime-capability-dependencies.md)
- [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) — HTTP-only CLI (no local agent)
- Story: [`STORY.md`](STORY.md)
