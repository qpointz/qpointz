# Artefact emit contract (v3)

**Status:** Implemented — see **[`artifact-foundation.md`](./artifact-foundation.md)** for the canonical agent-oriented reference  
**Story:** [`docs/workitems/in-progress/ai-artifact-emit-contract/`](../../workitems/in-progress/ai-artifact-emit-contract/STORY.md)

> **For other agents:** start with [`artifact-foundation.md`](./artifact-foundation.md) (end-to-end pipeline, file index, add-artifact checklist). This document retains the original WI-303 decision record.

---

## 1. Problem

`validate_sql` is a **QUERY** tool. `LangChain4jAgent` auto-invokes protocols only after **CAPTURE** tools. The model often answers with prose SQL → no `ProtocolFinal` → no structured chat artefact.

---

## 2. Canonical `ArtifactDescriptor`

One YAML descriptor drives coordinator, router, SSE bridge, and persistence:

| Field | Purpose |
|-------|---------|
| `id` | Key within capability (e.g. `generated-sql`) |
| `protocolId` | e.g. `sql-query.generated-sql` |
| `artifactKind` | Logical kind for checks |
| `persistKind` | e.g. `sql.generated`, `sql.validation` |
| `pointerKeys` | e.g. `[last-sql]` |
| `wirePartType` | SSE part type: `sql`, `facet-proposal` |
| `presentation` | `structured` |
| `protocolMode` | `STRUCTURED_FINAL`, … |
| `sourceEvent` | `tool.result` \| `protocol.final` |
| `emissionStrategy` | `OnToolSuccess`, `OnCaptureSuccess`, … |
| `destinations` | `CHAT_STREAM`, `ARTIFACT` |

Schema must be frozen in WI-303 before coordinator implementation.

---

## 3. Emission strategies

| Strategy | Trigger | Payload source | Model call |
|----------|---------|----------------|------------|
| **OnToolSuccess** | `validate_sql` + `passed=true` | Coordinator **constructs** `ProtocolFinal` from `normalizedSql` + dialect context | **No** |
| **OnCaptureSuccess** | Successful CAPTURE tool | `LangChain4jProtocolExecutor` | **Yes** (script step) |

### Example capability YAML (sql-query)

```yaml
artifacts:
  sql-validation:
    artifactKind: sql-validation
    persistKind: sql.validation
    sourceEvent: tool.result
    destinations: [ARTIFACT]

  generated-sql:
    protocolId: sql-query.generated-sql
    artifactKind: generated-sql
    persistKind: sql.generated
    pointerKeys: [last-sql]
    wirePartType: sql
    presentation: structured
    protocolMode: STRUCTURED_FINAL
    sourceEvent: protocol.final
    destinations: [CHAT_STREAM, ARTIFACT]

tools:
  validate_sql:
    emitsOnSuccess:
      artifact: generated-sql
      when: { field: passed, equals: true }
```

---

## 4. Tool-result vs protocol-final

| Source | Persist kind | Role |
|--------|--------------|------|
| `ToolResult` (`artifactType: sql-validation`) | `sql.validation` | Validator audit |
| `ProtocolFinal` (`sql-query.generated-sql`) | `sql.generated` | Canonical SQL + `last-sql` pointer |

**No duplicate `sql.generated`:** when coordinator emits `ProtocolFinal`, router must not also map tool-result `generated-sql` to `sql.generated`.

---

## 5. Profile gating

Descriptors live in **capability YAML**. Profiles select `capabilityIds` only.

| Profile | Capabilities | POC |
|---------|--------------|-----|
| `data-analysis` | `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query` | SQL |
| `schema-authoring` | above + `schema-authoring`, `metadata-authoring` | SQL + facet |

---

## 6. Wire table (POC)

| Descriptor | `wirePartType` | `persistKind` | `pointerKeys` |
|------------|----------------|---------------|---------------|
| `generated-sql` | `sql` | `sql.generated` | `last-sql` |
| `sql-validation` | — | `sql.validation` | — |
| `inferred-facet` | `facet-proposal` | (per metadata-authoring) | `last-metadata-facet-proposal` |
| schema capture | `schema-capture` | `schema.authoring.capture` | `last-schema-capture` |

---

## 7. Acceptance

Primary acceptance: scenario packs in `mill-ai-test` (`artifact-emit/*.yml`) with regression baselines (WI-307).

Full pipeline, UI cards, and extension checklist: [`artifact-foundation.md`](./artifact-foundation.md).
