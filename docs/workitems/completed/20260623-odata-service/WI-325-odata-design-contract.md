# WI-325 — OData design contract

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** —

## Goal

Normative design for OData v4 service and story cold-start docs.

Story cold-start detail lives in [`STORY.md`](STORY.md) and [`COLDSTART.md`](COLDSTART.md).

> **Post–WI-325 revision:** execution strategy updated to **RelNode compose + Rel→Substrait adapter**
> (not `DataHandler` / Phase 0 migration wedge). See current [`odata-service.md`](../../../design/platform/odata-service.md).

## Deliverables

- [x] [`docs/design/platform/odata-service.md`](../../../design/platform/odata-service.md)
- [x] Story folder: `STORY.md`, `COLDSTART.md`, WI stubs
- [x] Platform README index
- [x] BACKLOG P-41

## Locked decisions (WI-325; execution refined in doc refresh)

| Topic | Decision |
|-------|----------|
| URL | `/services/odata/v4/` |
| Entity set name | `{schema}_{table}` |
| Execution | RelNode compose → `RelToSubstraitPlanConverter` → `DataOperationDispatcher` |
| `$filter` | EDM → `RexNode`, not SQL string |
| Module boundary | No Calcite in `mill-data-backend-core`; adapter in `mill-data-backends` |

## Acceptance

Design doc standalone; no Gradle changes.
