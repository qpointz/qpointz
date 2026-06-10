# mill-py — platform HTTP clients (metadata + schema explorer)

Deliver **synchronous and asynchronous Python clients** in [`clients/mill-py`](../../../../clients/mill-py) for Mill **platform REST**: metadata management ([`metadata/mill-metadata-service`](../../../../metadata/mill-metadata-service) under `/api/v1/metadata`) and **schema exploration** ([`data/mill-data-schema-service`](../../../../data/mill-data-schema-service) under `/api/v1/schema`). Reuse [`mill.auth`](../../../../clients/mill-py/mill/auth.py) and extract shared **httpx** construction / error handling from Jet [`HttpTransport`](../../../../clients/mill-py/mill/_transport/_http.py) so TLS and authentication stay consistent with the data-plane HTTP client.

**Canonical metadata I/O** (multi-document `kind:` stream per [`MetadataYamlSerializer`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt), same family as [`test/datasets/skymill/skymill-canonical.yaml`](../../../../test/datasets/skymill/skymill-canonical.yaml)):

- **`GET /export` (WI-202)** — Emit the **full canonical document stream** in serializer order: **`kind: MetadataScope`** (all persisted scopes), **`kind: FacetTypeDefinition`** (**only** for catalog types with a **known type definition** — omit separate definition docs for undefined / flexible-open types), then **`kind: MetadataEntity`** with embedded **`facets`**: **every** facet assignment on each entity that matches **`scope`** is included (including flexible/undefined types); **`scope`** is the **only** filter on facet rows, not “defined facet types only.” **Format:** YAML (multi-doc) or **JSON** (single array of document maps, same order). This matches Skymill-style seeds, not “entities-only” export.
- **Import** — **Single file** `POST /import`; **`kind:`** multi-document YAML only (legacy `entities:` list envelopes are **rejected** by the serializer). **WI-202** updates controller/OpenAPI text that still mentions `entities:` / `facet-types:` sections so it matches runtime.
- **mill-py helpers (WI-203)** — Client-side merge of multiple seeds then one upload; export helpers use the same **canonical** notion as **WI-202**.

**Python public API (locked for implementation):** package **`mill.metadata`** (sync + **`mill.metadata.aio`**); schema explorer **`mill.schema_explorer`** with **`SchemaExplorerClient`** and **`mill.schema_explorer.aio`** (same `connect` pattern). See **WI-197** / **WI-198**.

**URN semantics** for model entities are defined in [`data/mill-data-metadata`](../../../../data/mill-data-metadata) (`ModelEntityUrn`, `SchemaModelRoot`); the Python README should point integrators at those types and at **`metadataEntityId`** from schema explorer responses when calling metadata write APIs.

**Design reference:** [`.cursor/plans/mill-py metadata client-51c85f63.plan.md`](../../../../.cursor/plans/mill-py%20metadata%20client-51c85f63.plan.md) (iterative plan; close the story against repo state, not the plan file alone).

**Story closure (repo workflow):** When the branch is merge-ready, the owner updates **[`docs/workitems/MILESTONE.md`](../../MILESTONE.md)** and **[`docs/workitems/BACKLOG.md`](../../BACKLOG.md)** per [`RULES.md`](../../RULES.md), and adds or updates **user-facing** docs under [`docs/public/src/`](../../../../docs/public/src/) if this work is product-visible beyond the package README (**WI-199** lists README only; closure still requires the global checklist).

## Work Items

- [x] WI-192 — Shared HTTP common module + Jet transport refactor (`WI-192-mill-py-http-common-and-transport-refactor.md`)
- [x] WI-193 — Metadata REST DTO models (`WI-193-mill-py-metadata-dto-models.md`)
- [x] WI-194 — Metadata client: scopes, import, export (`WI-194-mill-py-metadata-scopes-import-export.md`)
- [x] WI-195 — Metadata client: entities and facets (`WI-195-mill-py-metadata-entities-facets.md`)
- [x] WI-196 — Metadata client: facet type catalog (`WI-196-mill-py-metadata-facet-type-catalog.md`)
- [x] WI-197 — Schema explorer client (`WI-197-mill-py-schema-explorer-client.md`)
- [x] WI-198 — Async platform HTTP clients (`WI-198-mill-py-platform-http-async-clients.md`)
- [x] WI-199 — Unit tests, README, regression (`WI-199-mill-py-platform-http-tests-and-docs.md`)
- [x] WI-200 — Design documentation (`docs/design`) (`WI-200-mill-py-platform-http-design-docs.md`)
- [x] WI-201 — Integration tests (`WI-201-mill-py-platform-http-integration-tests.md`)
- [x] WI-202 — Metadata API: full canonical export (YAML + JSON), selective facet `scope`, fix import OpenAPI (`WI-202-metadata-api-canonical-export-json.md`)
- [x] WI-203 — mill-py: canonical mass import / export helpers (`WI-203-mill-py-canonical-metadata-helpers.md`)
