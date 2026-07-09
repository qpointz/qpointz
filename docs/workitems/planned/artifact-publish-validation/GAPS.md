# GAPS — artifact publish validation

Open decisions and discovery notes for story planning. **Locked** items belong in
[`STORY.md`](STORY.md) or [`WI-397`](WI-397-artifact-publish-validation-design.md) once agreed.

---

## GAP-1 — Scope of `sql-description` and `sql-result` CHAT_STREAM artifacts

**Context:** `sql-query.yaml` routes `sql-description` and `sql-result` to `CHAT_STREAM` as well as
`generated-sql`.

| Descriptor | Host-actionable? | Draft proposal? |
|------------|------------------|-----------------|
| `generated-sql` | Yes (Apply / Run in Analysis) | Yes |
| `sql-result` | Display rows in General Chat | No (execution output) |
| `sql-description` | Schema hint | Borderline |

**Options:**

- **A (recommended):** Gate **all** `CHAT_STREAM` descriptors; capability provides validators with
  different strictness (proposal vs execution-result vs description).
- **B:** Gate only `EmissionStrategy` protocol-final / host-actionable kinds; leave tool-result
  streams on existing tool routing.

**Status:** Open — decide in WI-397 design review before implementation.

---

## GAP-2 — `validate_sql` tool fate

**Options:** deprecate | thin draft helper | internal-only (not in tool manifest).

**Status:** WI-399 decides after gate is proven in integration tests.

---

## GAP-3 — Mock UI (`mill-ui` chatService) fidelity

Mock Analysis synthesizes SQL parts without a backend gate.

**Default:** document non-fidelity in design doc; optional client-side stub is out of scope unless
product requests it.

---

## GAP-4 — Virtual tool `artifact_publish` in prompts

Whether to add a synthetic tool spec to the model context (documentation-only) vs rely on system
prompt prose describing the JSON shape of injected `ToolExecutionResultMessage`.

**Status:** WI-399 — prefer system prompt + integration test evidence first.
