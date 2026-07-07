# Generated SQL artifact context (`title` / `description`)

**Status:** Locked design for WI-367 (Gap 15-16)
**Capability:** `sql-query`
**Consumers:** `ui/mill-ui` SQL cards, `generated-chart` decoration, GET/SSE replay

Related:

| Document | Role |
|----------|------|
| [`chart-artifact-contract.md`](./chart-artifact-contract.md) | Chart artifacts copy query-level `title` / `description` |
| [`../workitems/in-progress/ai-chart-mapping/WI-367-generated-sql-title-description.md`](../workitems/in-progress/ai-chart-mapping/WI-367-generated-sql-title-description.md) | Implementation WI |
| [`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml) | Tool + protocol schema (WI-367 updates) |

---

## 1. Problem

`generated-sql` artifacts today carry only machine-readable SQL fields. Chat shows a generic
**“Generated SQL”** header; chart artifacts cannot inherit a durable user-visible headline from the
query artifact. Emission via `validate_sql` → `OnToolSuccess` has no channel for human context.

---

## 2. Locked decision (Gap 15)

**Production path A:** the model supplies **`title` and `description` on every `validate_sql` tool
call**. They are **first-class, mandatory inputs** — not optional metadata and not runtime-invented
fallbacks for new emissions.

| Rule | Detail |
|------|--------|
| Source | LLM on `validate_sql` **input** only |
| Mandatory | Both fields required on every `validate_sql` invocation |
| Emission | Copied into `info` on the completion-coordinator draft; persisted when the plan finalizes (not `emitsOnSuccess`) |
| No model protocol | Model must not call `sql-query.generated-sql` directly (unchanged) |
| Legacy replay | Older persisted artifacts without fields remain parseable; UI uses replay-only fallback (Gap 16) |

**Non-goal:** deterministic server-side title synthesis for new artifacts (table-name heuristics,
SQL parsing, utterance substring). The LLM owns the pair; the handler **rejects** missing values.

---

## 3. Field contract

| Field | Required | Constraints | Purpose |
|-------|----------|-------------|---------|
| `title` | yes | Non-blank after trim; **3–120** characters | Short headline (card title, chart query-level title) |
| `description` | yes | Non-blank after trim; **10–500** characters | One–two sentences: what the query returns and why |

Guidance for prompts (normative intent, not hard validation):

- **Title** — noun phrase aligned with user intent (“Clients by country”, “Monthly revenue 2024”).
- **Description** — plain language; no SQL keywords; mention grain/filters when relevant.

---

## 4. `validate_sql` tool contract (target)

### 4.1 Input (additions)

```yaml
validate_sql:
  input:
    type: object
    required:
      - sql
      - attempt
      - title
      - description
    properties:
      sql:
        type: string
      attempt:
        type: integer
      title:
        type: string
        description: Short human-readable headline for the query artifact.
      description:
        type: string
        description: One or two sentences explaining what the query represents.
```

### 4.2 Input hygiene (before SQL semantic validation)

The handler validates **`title` and `description` first**:

| Check | On failure |
|-------|------------|
| `title` missing, blank after trim, or length outside 3–120 | `passed: false`, `code: missing_context_fields` |
| `description` missing, blank after trim, or length outside 10–500 | same |
| Both invalid | single message listing all context field errors |

SQL semantic validation runs **only** when context fields pass.

### 4.3 Output (echo on all outcomes)

Tool result includes echoed context fields so `ArtifactEmissionCoordinator` can emit them:

```json
{
  "artifactType": "sql-validation",
  "passed": true,
  "attempt": 1,
  "message": null,
  "normalizedSql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "title": "Clients by country",
  "description": "Counts clients grouped by country."
}
```

On context failure:

```json
{
  "artifactType": "sql-validation",
  "passed": false,
  "attempt": 1,
  "code": "missing_context_fields",
  "message": "title and description are required on every validate_sql call. Provide a short title (3-120 chars) and description (10-500 chars) that explain the query in plain language.",
  "title": "",
  "description": ""
}
```

---

## 5. `generated-sql` artifact (target)

### 5.1 Protocol / emitted payload

Add to `sql-query.generated-sql` **required** properties:

```json
{
  "artifactType": "generated-sql",
  "title": "Clients by country",
  "description": "Counts clients grouped by country.",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "dialectId": "CALCITE",
  "statementKind": "select",
  "source": "generated",
  "validationWarnings": []
}
```

`ArtifactEmissionCoordinator.buildGeneratedSqlPayload` must copy `title` and `description` from the
`validate_sql` tool result map. Emission must **not** proceed if either is absent after a passed
validation (defensive invariant; input hygiene should prevent this).

### 5.2 Refinement turns

When refining SQL, the model **must** supply an updated `title` / `description` pair on each
`validate_sql` retry that reflects the refined query (prompt rule in `sql-query.system`).

---

## 6. Prompt changes (`sql-query.system`)

Extend validation step (WI-367):

```text
3. Call validate_sql as a tool, passing sql, attempt, title, and description.
   - title: short headline for the query (required, 3-120 characters).
   - description: plain-language summary of what the query returns (required, 10-500 characters).
   - Do NOT produce a final answer yet.
```

Correction loop: if `code: missing_context_fields`, fix title/description and/or SQL before retry.

---

## 7. Legacy and replay-only fallback (not for new emissions)

| Context | `title` / `description` | UI behaviour |
|---------|-------------------------|--------------|
| New emission after WI-367 | Always present (mandatory) | Show artifact fields |
| Pre-WI-367 persisted rows | Absent | **Replay fallback:** card title = `"Generated query"`; optional subtitle = first line of SQL truncated to 80 chars (Gap 16 / WI-367) |

Replay fallback applies **only** when loading old artifacts. It is **not** used when the model
omits fields on a new `validate_sql` call.

---

## 8. Chart mapping consumption

Chart artifacts copy query-level context from the decorated SQL artifact:

- Root `title` / `description` on `generated-chart` default from generated-sql when chart-mapping
  does not override.
- Per-chart titles in `charts[]` remain optional and chart-specific.

See [`charts/multi-chart-artifact-model.md`](./charts/multi-chart-artifact-model.md).

---

## 9. WI-367 implementation checklist

- [x] Extend `validate_sql` YAML input/output and handler args
- [x] Input hygiene + `missing_context_fields` before SQL validator
- [x] Echo fields on `SqlValidationArtifact`
- [x] Extend `buildGeneratedSqlPayload`
- [x] Extend `sql-query.generated-sql` protocol schema
- [x] Update `sql-query.system` prompt
- [x] Wire mapper + UI types (Gap 16 — §10)
- [x] Tests: reject missing title/description; emit on success; legacy parse

---

## 10. Wire and UI (Gap 16 — locked)

Gap 15 makes `title` and `description` mandatory on **new** emissions. Gap 16 locks **pass-through**
on replay and display. Implementation is WI-367; policy is not open.

### 10.1 Backend wire (`ArtifactWireMapper.mapSql`)

Include when present on persisted inner payload:

```kotlin
payload = buildMap {
    put("sql", sql)
    (inner["dialectId"] as? String)?.let { put("dialectId", it) }
    (inner["title"] as? String)?.let { put("title", it) }
    (inner["description"] as? String)?.let { put("description", it) }
}
```

No transformation beyond type-safe string extraction.

### 10.2 mill-ui parse (`parseSqlArtifact`)

Extend `ChatMessageArtifact` SQL variant:

```typescript
| {
    kind: 'sql';
    sql: string;
    title?: string;
    description?: string;
    dialectId?: string;
    artifactId?: string;
    status?: string;
  }
```

Map wire payload fields when strings; omit when absent (legacy).

### 10.3 SQL card display (`SqlArtifactCard`)

| Case | Heading | Subtitle |
|------|---------|----------|
| `title` present | `artifact.title` | `artifact.description` when present |
| Legacy (both absent) | `"Generated query"` | First line of SQL, max 80 chars, ellipsis if truncated |

Keep dialect id as dimmed inline metadata. SQL `CodeHighlight` unchanged.

### 10.4 Chart wire (WI-369)

Full `generated-chart` payload (including `charts[]`, encodings, query-level `title`/`description`)
uses a separate mapper branch — same **pass-through** rule, not part of WI-367 SQL scope.
