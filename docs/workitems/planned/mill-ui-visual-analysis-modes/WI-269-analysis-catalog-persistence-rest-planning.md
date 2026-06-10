# WI-269 - Analysis catalog persistence and REST planning

Status: `planned`
Type: `feature`, `docs`
Area: `persistence`, `services`
Backlog refs: **U-14**

## Goal

Plan persistence and REST APIs for a shared Analysis catalog that can store both SQL and Visual
Analysis records without leaking persistence entities into service contracts.

## Scope

1. Define pure domain module:
   - `Analysis`
   - `AnalysisType`
   - `AnalysisSummary`
   - `AnalysisSpec`
   - `AnalysisCatalog` port
2. Define persistence adapter:
   - table layout for analysis records.
   - JSON spec storage with `spec_version`.
   - tags and timestamps.
   - owner/visibility fields if security model is ready; otherwise explicit placeholders.
3. Define REST service module:
   - `GET /api/v1/analyses`
   - `GET /api/v1/analyses/{analysisId}`
   - `POST /api/v1/analyses`
   - `PUT /api/v1/analyses/{analysisId}`
   - `DELETE /api/v1/analyses/{analysisId}`
   - optional preview endpoint for Visual Analysis board/path output.
4. Define compatibility with `/api/v1/queries`:
   - existing SQL saved-query endpoints can be implemented as a filtered SQL view over analyses.
   - UI migration can happen incrementally.
5. Define validation/error mapping:
   - unknown analysis -> `404`
   - invalid spec -> `422`
   - unsupported board/compiler feature -> `422` or structured warning
   - unauthorized -> existing `/api/**` security behavior
6. Define audit/ownership hooks:
   - who created/updated analysis.
   - future sharing and collaboration.

## Acceptance

- Persistence layout is defined with migration strategy.
- REST contract is documented with request/response examples.
- Contract-purity boundary is explicit: services depend on ports/domain, not JPA entities.
- Backward compatibility for saved SQL queries is described.
- Security and ownership assumptions are documented.

## Dependencies

- Persistence/Flyway conventions in `persistence/`.
- Existing API security configuration for `/api/**`.

## Open Questions

- Should analysis specs be stored as JSON text, normalized board rows, or both?
- Do we need optimistic locking (`version`) for collaborative editing from the first release?
- Should list responses include full specs or summaries only?
