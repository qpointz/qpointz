# Backend metadata in the Data Model

Mill can show **extra facet panels** on schemas, tables, and columns that come **directly from the active query backend**, not from the metadata repository. Think of this as **"how this backend is wired"**: storage paths, file readers, table mapping, and similar **operator-facing** detail that helps you audit a deployment next to **captured** descriptions and **logical** structure.

---

## What it is for

**Purpose:** **Expose backend-specific information** — facts that depend on *this* backend's configuration (for example Mill **flow** source descriptors) — in the same **Data Model** views that already show stored metadata and inferred **logical layout**.

That lets you:

- See **why** a table appears in the catalog (readers, mapping rules) without opening YAML on the server (subject to what your org chooses to surface).
- Distinguish **repository-backed** glossaries from **runtime-inferred** structure and **backend** binding context.

This layer is **read-only** in the UI: it is **not** a substitute for editing captured facets.

---

## How it relates to other facet sources

Mill merges several **sources** of facet rows when you open entity details:

| Source | What you see | Typical `originId` in APIs |
|--------|----------------|----------------------------|
| **Metadata repository** | Descriptions, relations, and other **saved** assignments you can edit (when allowed). | `repository-local` |
| **Logical layout** | Structural hints from the **catalog snapshot** (schemas, tables, columns) shared across backends. | `logical-layout` |
| **Backend metadata** | **Configuration- or connector-specific** facets for the **active** backend (flow descriptors today; others may follow). | e.g. `flow` |

Details on captured vs inferred and **optional API filters**: [Multi-source facets](multi-source-facets.md).

---

## Global metadata controls

All backend-contributed metadata sources are governed by a single global configuration block. These properties apply to **all** backends (Flow, Calcite, JDBC).

### Properties

All properties are under the `mill.data.backend.metadata` prefix.

| Property | Default | Description |
|----------|---------|-------------|
| **`enabled`** | `true` | Global kill-switch. When `false`, no backend `MetadataSource` beans are registered — neither **logical layout** nor backend-specific contributors (e.g. **flow**). Persisted metadata (`repository-local`) is unaffected. |
| **`redact`** | `basic` | Controls payload hygiene for inferred facets. See [Redaction](#redaction) below. |

### Example — disable all backend metadata

```yaml
mill:
  data:
    backend:
      metadata:
        enabled: false
```

No inferred facets appear in the Data Model. Queries, schema discovery, and persisted metadata remain fully functional.

---

## Redaction

Inferred facet payloads (especially from the Flow backend) may contain storage configuration including authentication fields. The `redact` property controls how much detail is exposed.

| Mode | Description |
|------|-------------|
| **`none`** | Pass storage `params` through unchanged. **Use with caution** — credentials may appear in the Data Model UI and APIs. |
| **`basic`** (default) | Remove top-level `connectionString`; strip secret fields from nested `auth` (keys such as `accountKey`, `sasToken`, `accessKey`, `secretKey`, legacy `accessKeyId` / `secretAccessKey`, `sessionToken`, `accessToken`, `serviceAccountJson`, …); keep non-secret hints such as `accountName` and `preferAmbientCredentials` when meaningful. Drop SAS query material from `endpoint` URLs (base URL kept). Adds `connectionStringConfigured` / `delegatedAuthConfigured` booleans when secrets were present. |
| **`safe`** | After the same hygiene as **`basic`**, keep only allow-listed structural keys per `storage.type` (for example `bucket`, `container`, `prefix`, `region`, `projectId`, `requesterPays` for S3, `rootPath`), plus the configured flags above. Endpoints and connection strings are never emitted. |

For Azure **`adls`**, **`basic`** keeps a sanitized **`endpoint`** and **`container`**; **`safe`** keeps **`container`** and **`prefix`** only (no `endpoint`).

Redaction levels are ordered by restrictiveness: `none` < `basic` < `safe`.

### Example — maximum redaction

```yaml
mill:
  data:
    backend:
      metadata:
        enabled: true
        redact: safe
```

---

## Flow backend (file-based sources)

When **`mill.data.backend.type`** is **`flow`**, Mill registers a metadata contributor that emits **flow** facet types (storage summary, table inputs, column binding) with `originId: flow`.

The global `mill.data.backend.metadata.enabled` property controls whether the flow metadata source is registered. There is no separate flow-specific toggle — use per-source overrides in individual source descriptors for fine-grained control.

| Property | Default | Scope | Description |
|----------|---------|-------|-------------|
| **`mill.data.backend.metadata.enabled`** | `true` | Global | Gates **all** backend metadata sources. When `false`, no `originId: flow` or `originId: logical-layout` rows appear. |
| **`mill.data.backend.metadata.redact`** | `basic` | Global | Payload hygiene. See [Redaction](#redaction). |
| **`mill.data.backend.flow.cache.facets.enabled`** | `true` | Flow only | When `false`, facet inference is always computed on demand (no snapshot cache). |
| **`mill.data.backend.flow.cache.facets.ttl`** | unset | Flow only | Optional duration (e.g. `5m`) controlling how long snapshot inference may be cached. |

**Note:** **`cache.schema.*`** is separate: it controls **Calcite schema** reuse for query planning, not the Data Model facet cache above.

### Per-source metadata overrides

Individual flow source descriptors can carry an optional `metadata` block (at the top level, alongside `storage` and `readers`) to override global defaults for that specific source:

```yaml
name: sensitive-data
storage:
  type: s3
  bucket: secret-bucket
  auth:
    accessKey: ${AWS_ACCESS_KEY_ID}
    secretKey: ${AWS_SECRET_ACCESS_KEY}
metadata:
  enabled: false
```

```yaml
name: public-data
storage:
  type: local
  rootPath: /data/public
metadata:
  redact: none
```

#### Resolution rules — `enabled`

| Global `enabled` | Source `enabled` | Effective |
|-------------------|------------------|-----------|
| `false`           | (any)            | **disabled** — global disable is absolute |
| `true`            | omitted          | **enabled** |
| `true`            | `true`           | **enabled** |
| `true`            | `false`          | **disabled** — source opts out |

Global disable always wins. A source-level override can only **opt out** of a globally enabled setting.

#### Resolution rules — `redact`

| Global `redact` | Source `redact` | Effective |
|-----------------|-----------------|-----------|
| `safe`          | (any less restrictive) | **safe** — global floor cannot be lowered |
| `basic`         | `safe`          | **safe** — source tightens |
| `basic`         | `none`          | **basic** — source cannot relax below global |
| `basic`         | omitted         | **basic** |
| `none`          | `basic`         | **basic** — source tightens |
| `none`          | `safe`          | **safe** — source tightens |

The effective level is `max(global, source)` — the **more restrictive** of the two always wins. A source can tighten redaction but never relax it below the global floor.

See also: [Configuration — metadata block](../sources/configuration.md#metadata).

### Example — full flow metadata configuration

```yaml
mill:
  data:
    backend:
      metadata:
        enabled: true
        redact: basic
      type: flow
      flow:
        cache:
          facets:
            enabled: true
            ttl: 5m
        sources:
          - ./config/my-source.yaml
```

Full flow backend reference: [Flow backend](../backends/flow.md).

---

## Calcite and JDBC backends

The global metadata controls (`mill.data.backend.metadata.enabled` and `redact`) also gate the **logical layout** metadata source for Calcite and JDBC backends. There are no per-source overrides for these backends — only the global settings apply.

When `mill.data.backend.metadata.enabled` is `false`, the `LogicalLayoutMetadataSource` is not registered, and no `originId: logical-layout` facets appear.

---

## See also

- [Multi-source facets](multi-source-facets.md) — `origin`, `originId`, `?scope=` / `?origin=`
- [Metadata in Mill](system.md) — product overview
- [Flow backend](../backends/flow.md) — configuration reference for file-based query
- [Configuration — metadata block](../sources/configuration.md#metadata) — per-source override syntax
- Design (repository): `docs/design/metadata/backend-provided-metadata.md`
