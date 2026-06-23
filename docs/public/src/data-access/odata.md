# OData v4 (live query)

Mill exposes physical tables as an **OData v4** read API for BI clients (Power BI, Tableau, Excel) using the [RWS/SDL OData](https://github.com/RWS/odata) stack (`com.sdl` **2.16.1**).

## Requirements

- **Java 25** — RWS OData 2.14.1+ requires JDK 25 bytecode; Mill’s platform toolchain matches this (see [Installation](../installation.md)).
- **Metadata** — relation and descriptive facets enrich the EDM (`$metadata`); optional file-backed metadata seed for demos.

## Enable

In `application.yml`:

```yaml
mill:
  data:
    services:
      odata:
        enable: true
        external-host: http-request   # or http, grpc-request, etc.
        max-top: 1000                 # optional cap for $top
```

The feature is gated with `@ConditionalOnService(value = "odata", group = "data")` — same pattern as `export` and `query`.

## Base URL

Each physical schema has its own OData service root (RWS requires the `.svc` suffix for URI parsing):

| Resource | Path |
|----------|------|
| Schema catalog | `GET /services/odata/schemas` |
| Service root (per schema) | `/services/odata/{schema}.svc` |
| Metadata (CSDL) | `/services/odata/{schema}.svc/$metadata` |
| Entity set | `/services/odata/{schema}.svc/{table}` |

Entity sets use the **physical table name** (e.g. `cities` under the `skymill` schema). The EDM container is named after the schema.

## Query options (v1)

Supported on entity set reads:

- `$filter` — pushed down to Calcite `RexNode` (untranslatable filters return **400**)
- `$select`, `$orderby`, `$top`, `$skip`
- `$expand` — when declared in relation metadata facets (same-schema targets only)

Not supported in v1: create/update/delete, `$batch`, actions, functions, delta feeds, cross-schema `$expand`.

## Example

```bash
curl -sS "http://localhost:8080/services/odata/schemas" -H "Accept: application/json"
curl -sS "http://localhost:8080/services/odata/skymill.svc/\$metadata" -H "Accept: application/xml"
curl -sS "http://localhost:8080/services/odata/skymill.svc/cities?\$filter=id%20eq%201" \
  -H "Accept: application/json"
```

## Power BI / Tableau

Use the **OData** connector with the per-schema service root URL ending in `.svc` (required by the RWS URI parser). Discover schemas via `/services/odata/schemas`, then connect to e.g. `http://host/services/odata/skymill.svc`. Filters and projections are executed on the Mill data plane (live query, not a static extract).

## Design reference

Platform design: [`docs/design/platform/odata-service.md`](https://gitlab.qpointz.io/qpointz/qpointz/-/blob/dev/docs/design/platform/odata-service.md) (in-repo path for developers).
