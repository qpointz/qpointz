# AI v1 integration testing (retired)

This document records the former **`ai:integration`** GitLab CI job and how v1 NL2SQL scenario
tests worked, so a future **v3-based** integration pipeline can be designed without reverse-engineering
git history.

## Status

| Item | State |
|------|--------|
| `ai:integration` job (`.gitlab/pipelines/integration.yml`) | **Disabled** (`rules: when: never`) |
| v1 modules | Under `ai/legacy/` — reference only, `testIT` disabled |
| Supported AI stack | `ai/mill-ai*` (formerly `mill-ai-v3*`) |

## Former CI job

**Job name:** `ai:integration`  
**Stage:** `integration`  
**Trigger:** Child pipeline when `RUN_INTEGRATION=true` (see root `.gitlab-ci.yml`).

**Script (historical):**

```bash
export REGRESSION_VERSION_TAG=${QP_VERSION_PEP440:-$CI_COMMIT_REF_SLUG}
./gradlew --no-daemon --continue --console plain :ai:testIT
```

**Artifacts:** JSON scenario reports from v1 core:

```
ai/legacy/mill-ai-v1-core/build/reports/scenarios/*.json
```

The job used `allow_failure: true` because scenario tests depended on live LLM credentials
(`OPENAI_API_KEY`) and external model availability.

## What `:ai:testIT` exercised (v1)

The aggregate task (`io.qpointz.plugins.mill-aggregate` on `:ai`) delegated to every subproject
with a `testIT` suite. For v1, the meaningful coverage was:

| Module | Integration focus |
|--------|-------------------|
| `ai/legacy/mill-ai-v1-core` | NL2SQL scenario regression (`testIT`), embedding/value-mapping flows against sample datasets |
| `ai/legacy/mill-ai-v1-nlsql-chat-service` | Chat service slice tests with Spring context |

Scenario definitions and report output lived under `mill-ai-v1-core` build reports. A commented
companion job `ai:publish-regression` copied those reports to shared storage for trend comparison.

## Runtime coupling (removed from product)

v1 was wired via Gradle edition feature `aiv1` (`mill-ai-v1-nlsql-chat-service` + `mill-ai-v1-core`).
**`apps/mill-service` editions no longer include v1**; production AI is `feature("ai-chat-service")`
→ `mill-ai-autoconfigure` + `mill-ai-persistence`.

No non-legacy module should depend on `:ai:legacy:*`.

## Related CI jobs (unchanged)

| Job | Purpose |
|-----|---------|
| `ai:build` (`ai/.gitlab-ci.yml`) | `:ai:test` + `:ai:testITClasses` on protected/feature pipelines |
| `core:integration`, `cloud:integration` | Other module `testIT` aggregates |

`ai:build` still compiles and runs **unit** tests for supported `mill-ai*` modules; it does not
replace end-to-end v1 scenario regression.

## Reimplementation checklist (v3)

When restoring AI integration CI:

1. Define scenario matrix (profiles, capabilities, datasets) against **`mill-ai-test`** / service `testIT`.
2. Add a new job (e.g. `ai:v3-integration`) running a **scoped** Gradle task — not unfiltered `:ai:testIT`
   over legacy modules.
3. Gate on secrets (`OPENAI_API_KEY`, vector store endpoints) with explicit `rules` and `allow_failure`
   policy.
4. Publish structured reports (JUnit + JSON) under `ai/mill-ai-test/build/reports/` or similar.
5. Remove `when: never` from the old job name only if renaming would break dashboards; prefer a new job name.

## References

- Legacy module layout: [`ai/legacy/README.md`](../../../../ai/legacy/README.md)
- Agentic runtime design: [`../agentic/`](../agentic/)
- Mill service AI edition: [`apps/mill-service/build.gradle.kts`](../../../../apps/mill-service/build.gradle.kts) — `feature("ai-chat-service")`
