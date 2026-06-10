# Agentic Runtime v3 — Learnings from v2

**Status:** Decision record
**Date:** March 15, 2026
**Scope:** What `ai/v2` has that is worth carrying into `ai/v3`, and what is not

---

## 1. Purpose

This document records a systematic comparison of `ai/v2` and `ai/v3` to identify v2
patterns that have real value for v3 — and those that do not — before committing to
additional implementation work.

The comparison was done after `ai/v3` had reached a working multi-mode protocol executor
(WI-067) and a generic `LangChain4jAgent` with a complete unit test layer.

---

## 2. What v2 has

`ai/v2` was a Spring AI / Project Reactor–based stack. Its notable components are:

| Component | Location | Description |
|-----------|----------|-------------|
| `Transformations` | `mill-ai-v2/streaming/` | Reactor Flux operators: line buffering, JSONL parsing, typed deserialization |
| `ConversationScenario` | `mill-ai-v2-test/scenario/` | YAML-driven multi-step conversation scenario model |
| `ConversationScenarioBaseTest` | `mill-ai-v2-test/scenario/` | JUnit5 `DynamicTest` runner for scenario suites |
| `Expectations` | `mill-ai-v2-test/scenario/` | Polymorphic assertion model (`json`, `json-list`, `text` discriminators) |
| `JsonPathMatcher` | `mill-ai-v2-test/scenario/json/` | JSONPath-based field assertions using `com.jayway.jsonpath` |
| `JsonListAsserts` | `mill-ai-v2-test/scenario/json/` | List quantifiers: `all`, `any`, `none`, `one` |
| `JsonAsserts` | `mill-ai-v2-test/scenario/json/` | Individual JSON node assertions: `not-empty`, `json-path` |
| `ProtocolSchema` | `mill-ai-v2/tools/` | Tool callback exposing capability protocols to the model at runtime |
| `Prompts` | `mill-ai-v2/` | Spring AI message factory helpers (`userContent`, `systemContent`, …) |
| `ContextDescriptor` | `mill-ai-v2/` | YAML-driven capability declaration with `createCapability()` factory |
| `Capability.advisors` | `mill-ai-v2/` | Spring AI `Advisor` list for request/response interception |

---

## 3. Gap assessment

### 3.1 High value — worth implementing in v3

#### JSONPath assertions

v2's `JsonPathMatcher`, `JsonListAsserts`, and `JsonAsserts` form a self-contained
assertion library for structured JSON payloads.

These are directly useful for v3 now because:

- `STRUCTURED_FINAL` protocol mode emits a single JSON payload (`ProtocolFinal.payload`)
- `STRUCTURED_STREAM` protocol mode emits per-event JSON payloads (`ProtocolStreamEvent.payload`)
- Unit and `testIT` assertions on those payloads need to go beyond `assertThat(json).isNotBlank()`

The v2 implementation has no Spring or Reactor dependency — it is pure Kotlin + Jackson +
`com.jayway.jsonpath`. It ports directly.

**Recommended:** implement as a new `mill-ai-v3-test` module contribution, available to
both unit and `testIT` layers.

#### Scenario test framework (concept, not code)

v2's `ConversationScenario` + `ConversationScenarioBaseTest` introduce the right concept:
declare a multi-step conversation as a YAML fixture, run it against an agent, assert on the
output.

v3's `TestScenario` is currently a placeholder with only `name` and `input` fields.

The v2 *code* is not portable — it is Spring-specific, uses Reactor, and assumes the v2
`Capability` interface. But the *model* — YAML fixture with steps, expectations, and
measurements — is the right design for v3 as well.

The `v3-validation-harness.md` document already describes the intended v3 harness shape.
The v2 scenario model is confirmation that the concept is sound and gives a reference for
the YAML schema.

**Recommended:** design a v3-native scenario runner based on the principles in both this
document and `v3-validation-harness.md`. Use v2's YAML structure as a reference, not as
source to port.

#### Scenario measurements

v2 scenarios declare `measures: [time, tokens]`. This is lightweight metadata that costs
little to add and provides useful regression signal.

Token counts are available from LangChain4j's `ChatResponse.metadata().tokenUsage()`.
Wall-clock time is trivial to wrap around a run.

**Recommended:** include a `measures` field in the v3 scenario model from the start so the
runner can record and report these without retrofitting.

---

### 3.2 Low value — do not port

#### Reactor streaming DSL

v2 exposes `.content()`, `.json()`, and `.to<T>()` as Reactor `Flux` extension methods.

v3 deliberately dropped Reactor. The `JsonlLineBuffer` (ported from `Transformations.combineContent`
during WI-067) already covers the only piece that was needed — line buffering over
arbitrary token chunks. The higher-level pipeline composition is handled inline in
`LangChain4jProtocolExecutor`, which is correct for the callback model.

**Decision:** not needed.

#### Protocol lookup as a tool

v2 exposed capability protocols as a callable tool so the model could query output
contracts at runtime. This was a workaround for v2's weakly-typed `protocol: JsonNode?`
declaration model.

v3 replaced this with strongly-typed `ProtocolDefinition` + `ProtocolExecutor`. The planner
selects the protocol via `PlannerDecision.protocolId`; the executor enforces it. There is no
need to expose protocol metadata to the model as a callable tool.

**Decision:** superseded by v3 design.

#### Spring AI message helpers (`Prompts`)

`Prompts.userContent()`, `systemContent()`, `userResource()`, `systemResource()` are Spring
AI `Message` factories. v3 uses LangChain4j message types; the helpers do not transfer.
`CapabilityManifest` already covers prompt asset loading.

**Decision:** not applicable.

#### `ContextDescriptor` / `StaticCapability`

v2 introduced `ContextDescriptor` as a YAML-driven factory for `StaticCapability`
instances. v3's `CapabilityManifest` + `CapabilityProvider` covers the same need with a
better model (typed schemas, `ServiceLoader` discovery, dependency injection).

**Decision:** superseded by v3 design.

#### Advisors

`Capability.advisors: List<Advisor>` is Spring AI's request/response interception hook.
v3 is framework-free by design. The `ProtocolExecutor` boundary is the appropriate
extension point for output contract enforcement.

**Decision:** not applicable.

---

## 4. Recommended implementation order

Both high-value items can be developed in `mill-ai-v3-test`:

1. **JSONPath assertion utilities** — implement first; immediately useful for the
   existing protocol mode tests and any future structured-output scenarios.
2. **v3-native scenario runner** — implement after; design the YAML model, typed fixture,
   and JUnit5 `DynamicTest` runner as described in `v3-validation-harness.md`.
3. **Measurements** — include in the scenario runner from the start; do not add as an
   afterthought.

---

## 5. What v3 has that v2 did not

For completeness, the following are v3 additions with no v2 equivalent:

- Strongly-typed `ProtocolDefinition` with mode validation (`TEXT`, `STRUCTURED_FINAL`, `STRUCTURED_STREAM`)
- `ProtocolExecutor` execution boundary separating synthesis from planning
- `CapabilityDependency` injection (enables production data capabilities like `SchemaCapability`)
- Multi-tool call support (`PlannerDecision.toolCalls`)
- Generic `LangChain4jAgent` with profile-driven capability resolution
- Interactive CLI / REPL (`mill-ai-v3-cli`)
- Framework-free core with no Spring or Reactor coupling

---

## 6. References

- `v3-validation-harness.md` — detailed harness design (scenario fixture shape, trace recorder, CI expectations)
- `v3-foundation-decisions.md` — v3 architectural decisions and framework-free core rationale
- `ai/mill-ai-v2/src/main/kotlin/io/qpointz/mill/ai/streaming/Transformations.kt` — source of `JsonlLineBuffer` line-buffering algorithm
- `ai/mill-ai-v2-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/` — v2 scenario model reference
