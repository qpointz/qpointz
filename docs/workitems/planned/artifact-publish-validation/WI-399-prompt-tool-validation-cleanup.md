# WI-399 - Prompt and tool validation cleanup

> **Story context:** Correction loop, attempt policy, and migration from model-owned validation —
> [`STORY.md`](STORY.md) § *Correction loop* and [`docs/design/agentic/artifact-publish-validation.md`](../../../design/agentic/artifact-publish-validation.md) § *Migration*.
> Tool fate options: [`GAPS.md`](GAPS.md) GAP-2, GAP-4.

## Goal

Align capability prompts and tool surfaces with the **runtime publish gate** (WI-397/398): remove
model-owned validation loops and sporadic "you must call validate_*" instructions that duplicate or
conflict with deterministic publish validation.

## Prerequisites

- **WI-397** — publish gate active.
- **WI-398** — capability validators registered.

## Requirements

### Prompt review (all gated capabilities)

For each affected capability YAML (`sql-query`, `chart-mapping`, `metadata-authoring`, profile
prompts that restate validation):

- **Remove** instructions that make the model the validation authority, e.g.:
  - "never skip validate_sql"
  - "retry up to 3 attempts" (runtime owns attempts)
  - "emit SQL only after validate_sql passes" (runtime gate owns publish)
- **Replace** with draft-oriented guidance:
  - compose / refine SQL or facet payloads using schema tools
  - propose drafts; runtime publishes when valid
  - on publish rejection, revise using the returned validator message
- **analysis-copilot** profile intent: keep "emit structured SQL proposals" but clarify that
  **publication** is runtime-validated (not model self-attestation).

### Tool surface review

- Decide per tool whether it remains a **draft helper** or becomes **internal-only**:
  - `validate_sql` — optional draft check vs deprecated in favor of publish-only validation
  - `validate_chart_spec` — same pattern
- Tool results for diagnostic kinds (`sql-validation`, `chart-validation`) stay
  `destinations: []` — not chat artifacts.
- Document the split in `docs/design/agentic/artifact-publish-validation.md`.

### Tests and scenarios

- Update `mill-ai` unit tests and scenario baselines that assert model-driven validation flows.
- Add regression: invalid draft → no structured part → model revises → valid publish.
- Update `docs/public/src/` AI chat docs if they mention model-side validation.

### Mock UI (mill-ui)

- Document that mock chat is not publish-gate faithful (existing policy) OR add a minimal stub gate
  for Analysis mock SQL synthesis (product decision — default: document only).

## Acceptance criteria

- No capability prompt instructs the model to be the sole validation gate for publishable artifacts.
- Agent integration tests demonstrate runtime-enforced retry without relying on prompt compliance.
- Design doc and public docs describe draft vs publish responsibilities.

## Verification

```bash
cd ai && ./gradlew test testIT
cd ui/mill-ui && npm run test -- --run
```
