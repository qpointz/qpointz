# WI-398 - Capability-owned publish validators

> **Story context:** Publishable artifact inventory, validator key table, and capability ownership matrix —
> [`STORY.md`](STORY.md) § *Publishable artifact inventory* and § *Validator registry*. SPI contract —
> [`docs/design/agentic/artifact-publish-validation.md`](../../../design/agentic/artifact-publish-validation.md).

## Goal

Implement **publish validators** in each capability that emits host-actionable artifacts, and register
them with the runtime gate from WI-397.

## Prerequisites

- **WI-397** — `CapabilityPublishValidator` SPI and `ArtifactPublishGate` integrated into emission.

## Requirements

### sql-query

- Move authoritative SQL validation from model-invoked `validate_sql` **as publish gate** into
  `SqlQueryCapabilityPublishValidator` (may delegate to existing `SqlValidator` /
  `BackendSqlValidator`).
- Publish gate validates **draft/generated-sql payload** before `ProtocolFinal`.
- Preserve normalized SQL, dialect, and title/description rules currently split between
  `validateSqlContext` and `BackendSqlValidator`.
- `validate_sql` tool may remain as an **optional draft helper** for the model (WI-399 decides
  deprecation vs thin wrapper).

### chart-mapping

- Validator for chart visualization payloads published with SQL finals (`sql-with-chart` completion).
- Enforce trusted schema presence / column binding rules currently spread across
  `validate_chart_spec` and coordinator steps.

### metadata-authoring

- Validator for `facet-proposal` / `metadata.faceting.capture` publish payloads.
- Enforce facet type, target entity, payload shape (align with facet JSON schema generator where
  applicable).

### Registry / wiring

- Each capability provider exposes its validator via dependency container.
- Spring autoconfigure assembler registers validators with the global publish gate.
- Unit tests per capability: pass, fail, normalized payload, warnings.

## Acceptance criteria

- Each publishable artifact kind in `sql-query.yaml`, `chart-mapping.yaml`, and
  `metadata-authoring.yaml` has a registered capability validator.
- Invalid SQL / facet / chart payloads do not reach CHAT_STREAM in capability unit + integration
  tests.
- Validators are reusable regardless of profile (`data-analysis`, `analysis-copilot`, schema
  authoring).

## Verification

```bash
cd ai && ./gradlew :mill-ai:test :mill-ai-data:test :mill-ai-autoconfigure:test
```
