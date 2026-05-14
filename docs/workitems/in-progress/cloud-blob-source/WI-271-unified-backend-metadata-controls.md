# WI-271 — Unified backend metadata enabled/redact configuration

- **Status:** planned
- **Story:** [`STORY.md`](STORY.md)

---

## Problem

Backend-contributed `MetadataSource` beans lack unified controls:

1. **`LogicalLayoutMetadataSource`** (`logical-layout`) has **no** enable/disable toggle — it is
   always registered when a `SchemaProvider` exists. This applies to all backends (flow, calcite,
   jdbc).

2. **`FlowDescriptorMetadataSource`** (`flow`) is gated by `mill.data.backend.flow.metadata.enabled`
   — a flow-specific property. There is no global equivalent.

3. **Redaction is unimplemented.** `FlowDescriptorMetadataSource.storageToPayload()` serialises the
   full `StorageDescriptor` (including `auth` fields) into facet payloads without stripping
   credentials. The deleted `BackendMetadataProperties.RedactMode` was never wired.

4. There is **no single property** to disable all inferred metadata across backends for performance
   or security.

## Proposed Design

### Configuration hierarchy

Settings are resolved at three levels. A narrower scope can only **restrict** (disable or tighten
redaction) — never **widen** what the broader scope disallows.

```
global (mill.data.backend.metadata)
  └─ flow backend: per-source descriptor (metadata block in YAML)
       └─ effective = merge(global, source-level)
```

Calcite and JDBC backends have no per-source granularity — only the global settings apply.

#### Global settings (`mill.data.backend.metadata`)

```yaml
mill:
  data:
    backend:
      metadata:
        enabled: true       # global kill-switch for all backend MetadataSource beans
        redact: basic        # none | basic | safe
```

| Property  | Default | Description |
|-----------|---------|-------------|
| `enabled` | `true`  | When `false`, no backend `MetadataSource` beans are registered. Persisted metadata (`repository-local`) is unaffected. |
| `redact`  | `basic` | Controls payload hygiene for inferred facets. `none` = verbatim (dangerous). `basic` = strip credential keys, sanitise URLs. `safe` = allow-listed minimal parameter set only. |

#### Flow per-source overrides (source descriptor YAML)

Flow source descriptors can carry an optional `metadata` block to override global defaults for
that specific source:

```yaml
name: sensitive-data
storage:
  type: s3
  bucket: secret-bucket
  auth:
    accessKeyId: ${AWS_ACCESS_KEY_ID}
    secretAccessKey: ${AWS_SECRET_ACCESS_KEY}
metadata:
  enabled: false        # suppress metadata for this source only
```

```yaml
name: public-data
storage:
  type: local
  rootPath: /data/public
metadata:
  redact: none          # relaxed — no secrets in local paths
```

#### Resolution rules

| Global `enabled` | Source `enabled` | Effective |
|-------------------|------------------|-----------|
| `false`           | (any)            | **disabled** — global disable is absolute |
| `true`            | omitted          | **enabled** |
| `true`            | `true`           | **enabled** |
| `true`            | `false`          | **disabled** — source opts out |

| Global `redact` | Source `redact` | Effective |
|-----------------|-----------------|-----------|
| `safe`          | (any less restrictive) | **safe** — global floor cannot be lowered |
| `basic`         | `safe`          | **safe** — source tightens |
| `basic`         | `none`          | **basic** — source cannot relax below global |
| `basic`         | omitted         | **basic** |
| `none`          | `basic`         | **basic** — source tightens |
| `none`          | `safe`          | **safe** — source tightens |

Redaction levels are ordered: `none` < `basic` < `safe`. The effective level is
`max(global, source)` — the **more restrictive** of the two always wins. A source can tighten
but never relax below the global floor.

### Backend applicability

| Backend  | Global settings | Per-source overrides |
|----------|-----------------|----------------------|
| **Flow** | yes — gates `FlowDescriptorMetadataSource` and `LogicalLayoutMetadataSource` | yes — `metadata` block in each source descriptor YAML |
| **Calcite** | yes — gates `LogicalLayoutMetadataSource` | no — no descriptor-level granularity |
| **JDBC** | yes — gates `LogicalLayoutMetadataSource` | no — no descriptor-level granularity |

### Properties class

Recreate `BackendMetadataProperties` (Java, `@ConfigurationProperties`) under
`mill.data.backend.metadata` with `enabled` (boolean) and `redact` (enum).

### Source descriptor model change

Add an optional `metadata` field to `SourceDescriptor`:

```kotlin
data class SourceMetadataDescriptor(
    val enabled: Boolean? = null,   // null = inherit global
    val redact: RedactMode? = null  // null = inherit global
)
```

`SourceDescriptor.metadata` defaults to `null` (fully inherited).

### Gating

Add `@ConditionalOnProperty(prefix = "mill.data.backend.metadata", name = "enabled", ...)` to:

- `LogicalLayoutMetadataSourceAutoConfiguration` (currently ungated)
- `FlowDescriptorMetadataSourceAutoConfiguration` (currently gated by flow-specific property — remove that)

Remove the flow-specific `mill.data.backend.flow.metadata.enabled` property, the
`FlowBackendProperties.MetadataProperties` inner class, and the bean-level
`@ConditionalOnProperty` for `flow.metadata`. Only the global property gates registration.

### Redaction

Redaction tiers applied to inferred facet payloads:

- **`none`**: pass payloads through unchanged.
- **`basic`**: remove keys matching credential patterns (`accessKeyId`, `secretAccessKey`,
  `connectionString`, `accountKey`, `serviceAccountJson`, etc.), replace endpoint URLs with
  `<redacted>` if they contain embedded credentials.
- **`safe`**: emit only `type` and allow-listed structural keys (e.g. `bucket`, `container`,
  `prefix`, `region`). All other keys stripped.

For the flow backend, `FlowDescriptorMetadataSource` resolves the effective redact mode per source
descriptor before building facet payloads. The `storageToPayload()` method applies the resolved
redaction before emitting.

For calcite/JDBC, `LogicalLayoutMetadataSource` applies the global redact mode (no per-source
override exists).

### Affected modules

| Module | Change |
|--------|--------|
| `data/mill-data-autoconfigure` | Recreate `BackendMetadataProperties`, update conditionals on both metadata source autoconfigs |
| `data/mill-data-source-core` | Add optional `SourceMetadataDescriptor` to `SourceDescriptor` |
| `data/mill-data-metadata` | Add redaction utility to `AbstractInferredMetadataSource` |
| `data/mill-data-backends` | `FlowDescriptorMetadataSource` — resolve per-source effective settings, apply redaction |
| `data/mill-data-autoconfigure` (test) | Update property paths, add resolution and redaction tests |

### Test plan

- [ ] `BackendMetadataProperties` binds correctly from YAML
- [ ] Global `enabled: false` suppresses both `LogicalLayoutMetadataSource` and `FlowDescriptorMetadataSource`
- [ ] Global `enabled: true` (default) registers both sources as before
- [ ] Per-source `metadata.enabled: false` suppresses facets for that source only
- [ ] Per-source `metadata.enabled: true` with global `enabled: false` still suppresses (global wins)
- [ ] Global `redact: basic` strips credential keys from flow storage payloads
- [ ] Global `redact: safe` emits only allow-listed keys
- [ ] Global `redact: none` passes payloads unchanged
- [ ] Per-source `redact: safe` with global `redact: none` results in `safe` (tightening)
- [ ] Per-source `redact: none` with global `redact: basic` results in `basic` (cannot relax)
- [ ] Existing `FlowDescriptorMetadataSourceIT` still passes

### Documentation updates

- `docs/public/src/backends/flow.md` — update properties reference table (remove
  `metadata.enabled` from flow section, add reference to global `mill.data.backend.metadata`)
- `docs/public/src/sources/configuration.md` — document optional `metadata` block in source descriptor
- `docs/public/src/metadata/backend-metadata.md` — document global controls and per-source overrides

---

## Related

- `FlowDescriptorMetadataSourceAutoConfiguration` (`data/mill-data-autoconfigure`)
- `LogicalLayoutMetadataSourceAutoConfiguration` (`data/mill-data-autoconfigure`)
- `AbstractInferredMetadataSource` (`data/mill-data-metadata`)
- `MetadataSource` interface (`metadata/mill-metadata-core`)
- Cloud blob source story — identified the gap (WI-265 GAP-3)
