# Regression Diff Table Builder — Tool Specification (v1)

## Purpose

The **Regression Diff Table Builder** compares two regression runs (baseline vs candidate) and produces **human-readable diff tables** (Markdown and/or CSV).

The tool is intentionally:
- model-agnostic
- scenario-agnostic
- planner-agnostic
- LLM-agnostic

It answers one question:

> **What changed between two versions, and does it matter?**

---

## Scope

### In Scope
- Consume regression run artifacts (JSON)
- Normalize results into a comparable model
- Compare selected metrics and contracts
- Classify differences as PASS / WARN / FAIL
- Emit Diff Tables as the **primary artifact**

### Out of Scope
- Scenario execution
- SQL correctness or planning
- Business semantics
- LLM interaction
- Automatic remediation

---

## Inputs

### Baseline Run Artifact
```json
{
  "runId": "v1.3",
  "scenarioName": "basic-scenario",
  "results": [ ... ]
}
```

### Candidate Run Artifact
```json
{
  "runId": "v1.4",
  "scenarioName": "basic-scenario",
  "results": [ ... ]
}
```

**Requirements**
- Same `scenarioName`
- Preserved step ordering
- Includes `ask` and `verify` actions
- Includes `metrics` from `verify`

---

## Outputs

### Required
- **Markdown Diff Table**

### Optional
- CSV Diff Table
- JSON Diff Summary (machine-readable)

---

## Diff Granularity

- Diffs are produced **per scenario step**
- Steps are keyed by:
  - step index
  - human-readable label (derived from `ask`)

Each step may generate multiple diff rows.

---

## Normalized Comparison Model (Flattened)

### Intent
- `intent`

### Presence
- `has.sql`
- `has.data`
- `has.chart`
- `has.enrichment`

### SQL Shape (Flattened)
- `sqlShape.tables`
- `sqlShape.hasJoin`
- `sqlShape.hasWhere`
- `sqlShape.hasAggregation`
- `sqlShape.hasGrouping`
- `sqlShape.hasLimit`
- `sqlShape.selectArity`
- `sqlShape.filterColumns`
- `sqlShape.aggregationFunctions`

### Data Metrics
- `data.size`
- `data.fields.count`
- `data.container`

### Performance Metrics
- `llm.promptTokens`
- `llm.completionTokens`
- `llm.totalTokens`
- `execution.timeMs`

### Verification Signals
- `verify.status` (PASS / WARN / FAIL)
- `verify.warnReasons[]`

---

## Comparison Rules

### Equality
- Scalars: strict equality
- Sets: order-independent equality
- Missing vs present: treated as a change

### Delta
- Numeric: `candidate - baseline`
- Sets: added / removed elements

---

## Status Classification

### PASS
- No meaningful change
- Explicitly allowed change
- Performance improvement

### WARN
- Semantic drift with valid execution
- Examples:
  - `hasWhere = true` AND `data.size = 0`
  - Prompt tokens exceed soft threshold
  - Additional SQL shape features appear

### FAIL
- Contract violation
- Examples:
  - Intent changed
  - Required artifact missing
  - Forbidden SQL shape change (e.g., unexpected JOIN)

Rules must be configurable; sensible defaults must exist.

---

## Diff Table (Markdown)

### Header
```markdown
### <scenarioName> — <baselineRunId> → <candidateRunId>
```

### Columns

| Column | Description |
|------|------------|
| Step | Step label |
| Metric | Metric name |
| Baseline | Baseline value |
| Candidate | Candidate value |
| Δ | Delta (if applicable) |
| Status | PASS / WARN / FAIL |

### Example

```markdown
| Step | Metric | v1.3 | v1.4 | Δ | Status |
|------|--------|------|------|---|--------|
| list clients | intent | get-data | get-data | = | PASS |
| list clients | sqlShape.hasWhere | false | false | = | PASS |
| list clients | data.size | 10 | 10 | = | PASS |
| list clients | promptTokens | 4688 | 1210 | -3478 | PASS |
| count clients | data.size | 3 | 0 | -3 | WARN |
| count clients | note | — | possible value-mapping miss | WARN |
```

---

## Configuration

### Thresholds
```yaml
thresholds:
  promptTokens:
    warnAbove: 5000
  executionTimeMs:
    warnAbove: 15000
```

### Forbidden Changes
```yaml
forbidden:
  intentChange: true
  sqlShape:
    hasJoin: false
```

---

## Determinism

- Deterministic
- Side-effect free
- Same inputs → identical outputs

---

## Design Principles

- Diff Tables are the **primary artifact**
- JSON is raw evidence, not the report
- Prefer WARN over false FAIL
- Avoid SQL syntax coupling
- Compare **declared contracts**, not internals

---

## Non-Goals

- SQL correctness validation
- Planner-based analysis
- Business KPI validation
- Dashboards or visualizations

---

## Summary

The Regression Diff Table Builder provides a **cheap, stable, and scalable** mechanism to track behavioral changes in LLM-driven systems by comparing **semantic execution contracts**, not implementation details.
