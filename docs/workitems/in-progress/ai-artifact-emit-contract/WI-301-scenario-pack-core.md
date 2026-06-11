# WI-301 — ScenarioPack core + regression record

Status: `done`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-300** (YAML spec frozen in design doc).

## Goal

Replace the unfinished [`mill-ai-test`](../../../../ai/mill-ai-test/) skeleton with the greenfield harness core — all implementation under `ai/mill-ai-test/src/main/kotlin/`.

## Deliverables

**Remove / replace (breaking):**

- [`ConversationScenario.kt`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/ConversationScenario.kt), [`Expectations.kt`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/Expectations.kt), json/text expect subpackages
- [`ConversationScenarioBaseTest.kt`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/ConversationScenarioBaseTest.kt)
- [`TestScenario.kt`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/TestScenario.kt)
- Fixtures `src/test/resources/ai-test/scenario/trivial.yml`, `json-expects.yml`

**Add (`io.qpointz.mill.ai.test.scenario.v3`):**

- [ ] `ScenarioPack`, `ScenarioAction` (`AskAction`, `VerifyAction`, `ReplyAction`)
- [ ] `ScenarioPackLoader` — classpath / file, multi-doc YAML
- [ ] `TurnOutcome` — `response`, `events`, `artifacts`, `sseEvents`, `structuredParts`, `transcript`
- [ ] `TurnCheck` + `TurnCheckRegistry` — polymorphic checks (`events`, `artifacts`, `sse`, `response`, `transcript`)
- [ ] `ScenarioPackTestBase` — JUnit `@TestFactory`; writes regression record after pack
- [ ] `ConversationRegressionRecord`, `TurnRecord`
- [ ] `ConversationRegressionWriter` → `build/reports/scenarios/<pack-slug>.record.json` (+ optional `.normalized.json`)
- [ ] `ConversationRegressionComparator` — baseline diff; offline check replay entry point
- [ ] `TurnOutcomeSerializer` — stable JSON for `AgentEvent`, artefact payloads, `ChatSseEvent`
- [ ] `RecordNormalizer` — produces `*.record.normalized.json` for baseline comparison

### Baseline normalization rules (deterministic diff)

Scrub or replace in normalized output:

| Field / pattern | Treatment |
|-----------------|-----------|
| `runId`, `chatId`, `turnId`, `eventId`, `artifactId` | Replace with stable placeholders (`<runId>`, etc.) or omit |
| UUID-shaped strings anywhere in payload | Replace with `<uuid>` |
| `recordedAt`, `createdAt`, wall-clock timestamps | Omit or fixed sentinel `1970-01-01T00:00:00Z` |
| `runMeta.gitCommit` | Omit in baseline (keep in raw record) |
| Token stats (`inputTokens`, `outputTokens`, `totalTokens`) | Omit |
| Optional null / empty fields | Normalize consistently (omit vs explicit null — pick one, document) |

**Preserve:** event type sequences, tool names, protocol ids, persist kinds, payload structure (SQL text, facet fields), SSE `partType` / `presentation`, check results.

**Ordering:** event arrays compared in emission order; document if any async reordering is possible.

**Refresh:** `UPDATE_BASELINES=1 ./gradlew :ai:mill-ai-test:testIT` rewrites committed baselines after intentional contract change.

**Unit tests (`:ai:mill-ai-test:test`):**

- [ ] `ScenarioPackLoaderTest`
- [ ] `TurnCheckRegistryTest`
- [ ] `ConversationRegressionRecordTest` — round-trip + offline replay
- [ ] `ConversationRegressionComparatorTest` — detects structural drift; ignores scrubbed fields
- [ ] `RecordNormalizerTest` — UUID/timestamp/id scrubbing stable across runs

## Acceptance criteria

- [x] `./gradlew :ai:mill-ai-test:test` passes.
- [x] Sample pack YAML loads; checks run against a hand-built `TurnOutcome` in unit tests.
- [x] Record JSON contains all fields documented in WI-300 design doc.
- [x] Normalized baseline of same pack is **byte-stable** across two consecutive runs with identical script.
- [x] Module description in [`build.gradle.kts`](../../../../ai/mill-ai-test/build.gradle.kts) updated (no longer "skeleton").
