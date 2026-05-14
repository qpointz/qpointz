# Flow Backend

The Flow backend queries file-based data sources — CSV, TSV, Parquet, Avro, Excel — described by Mill's [source descriptor](../sources/index.md) YAML files. Each source descriptor defines a schema name, storage location, file format, and table mapping rules. The Flow backend reads these descriptors, materializes the files as Calcite tables, and makes them queryable via SQL.

**Cloud object storage** extends the same descriptor pipeline: `storage.type` selects bucket-backed backends ([S3](../sources/storages/s3.md), [GCS](../sources/storages/gcs.md), [Azure Blob](../sources/storages/azure.md)) instead of `local`, without changing `readers` or `table.mapping`.

This is the backend to use when your data lives in files (or object-store equivalents) and you want to query it without loading into a database first.

---

## How It Works

1. Mill reads one or more source descriptor YAML files listed in the configuration.
2. Each descriptor is parsed into a `SourceDescriptor` — defining storage, readers, table mappings, and conflict resolution.
3. The Flow backend opens a Calcite JDBC connection and registers each descriptor as a schema (named after the descriptor's `name` property).
4. Files are discovered from storage, grouped into tables by the mapping rules, and read using the specified format handlers.
5. Queries are parsed, planned, and executed entirely within Calcite's in-process engine.
6. Results are streamed back as columnar vector blocks.

```
Source Descriptor YAML(s)
        │
        ▼
┌───────────────────┐
│ SourceDefinition  │
│   Repository      │
│ (single/multi)    │
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ FlowContextFactory│
│  (Calcite conn)   │
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│  Calcite Schema   │
│  FlowSchema ×N    │
│  FlowTable  ×M    │
└───────────────────┘
```

---

## Configuration

Activate the Flow backend by setting `mill.data.backend.type` to `flow`. List the paths to your source descriptor YAML files under `mill.data.backend.flow.sources`.

### Minimal example

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - ./config/my-source.yaml
```

### Multiple sources

Each source descriptor produces its own schema. List as many as you need:

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - ./config/sales-data.yaml
          - ./config/reference-data.yaml
          - ./config/logs.yaml
```

### Full example

```yaml
mill:
  data:
    sql:
      dialect: CALCITE
      conventions:
        quoting: BACK_TICK
        caseSensitive: true
        unquotedCasing: UNCHANGED
    backend:
      type: flow
      flow:
        sources:
          - ./config/skymill-source.yaml
          - ./config/skymill-ref-source.yaml
```

### Properties reference

#### Global metadata properties

These properties are under the `mill.data.backend.metadata` prefix and apply to **all** backends (Flow, Calcite, JDBC).

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `enabled` | no | `true` | Global kill-switch for all backend `MetadataSource` beans. When `false`, neither `LogicalLayoutMetadataSource` nor `FlowDescriptorMetadataSource` are registered. Persisted metadata (`repository-local`) is unaffected. |
| `redact` | no | `basic` | Controls hygiene for the **`storage`** object inside **flow-schema** inferred facets. `none` = verbatim (may expose credentials). `basic` = strip secrets from `auth` / `connectionString`, strip SAS queries from `endpoint`, keep safe hints (`accountName`, meaningful `preferAmbientCredentials`). `safe` = allow-list only structural fields per storage type (`bucket`, `container`, `prefix`, …) plus non-secret “configured” flags. |

See [Backend metadata](../metadata/backend-metadata.md) for the full redaction and override model.

#### Flow backend properties

These properties are under the `mill.data.backend.flow` prefix.

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `sources` | yes | `[]` | List of paths to source descriptor YAML files. Relative paths are resolved from the working directory. |
| `cache.schema.enabled` | no | `false` | Reuse resolved Flow schemas across requests. |
| `cache.schema.ttl` | no | unset | Optional cache TTL (for example `1m`, `30s`). When unset, cache does not auto-expire. |
| `cache.facets.enabled` | no | `true` | When `false`, flow facet inference is always computed on demand (no snapshot cache). |
| `cache.facets.ttl` | no | unset | Optional TTL for the facet inference cache (for example `5m`). When unset, cache does not auto-expire. |

### Backend metadata (Data Model)

Mill can attach **read-only inferred** facet rows to catalog entities so operators see backend-specific details alongside captured metadata and logical layout.

The global `mill.data.backend.metadata.enabled` property gates all backend metadata sources across all backends. When `false`, nothing is registered. Individual source descriptors can also carry a `metadata` block to suppress or tighten redaction per source — see [Configuration](../sources/configuration.md#metadata) and [Backend metadata](../metadata/backend-metadata.md).

Concepts and tuning: [Backend metadata](../metadata/backend-metadata.md).

Example:

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
          schema:
            enabled: true
            ttl: 1m
          facets:
            enabled: true
            ttl: 5m
        sources:
          - ./config/skymill-source.yaml
```

---

## Source Descriptors

The Flow backend is powered by Mill's source descriptor format. A source descriptor is a YAML file that tells Mill where to find data files, how to read them, and how to map them to tables.

A complete guide to writing source descriptors is in the [Data Sources](../sources/index.md) section. Here is a quick summary of what a descriptor looks like:

```yaml
name: airline-data
storage:
  type: local
  rootPath: /data/airlines/csv
table:
  mapping:
    type: regex
    pattern: "(?<table>[^/]+)\\.csv"
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
```

This descriptor:

- Creates a schema named `airline-data`
- Scans `/data/airlines/csv` for files
- Reads them as comma-separated CSV with a header row
- Maps each file to a table named after the filename (e.g. `cities.csv` becomes table `cities`)

A descriptor can also use **glob** patterns to match files and assign fixed table names:

```yaml
name: data-lake
storage:
  type: local
  rootPath: /data/lake
readers:
  - type: parquet
    table:
      mapping:
        type: glob
        pattern: "**/sales/**/*.parquet"
        table: sales
  - type: parquet
    table:
      mapping:
        type: glob
        pattern: "**/inventory/**/*.parquet"
        table: inventory
```

This maps all Parquet files under any `sales/` subtree to the `sales` table, and files under `inventory/` to the `inventory` table.

### Key descriptor concepts

| Concept | Description | Documentation |
|---------|-------------|---------------|
| Storage | Where to find files (local filesystem) | [Storage](../sources/configuration.md#storage) |
| Readers | How to read files (CSV, TSV, Parquet, Avro, Excel) | [Readers](../sources/configuration.md#readers) |
| Table mapping | Which files become which tables (regex, directory, glob) | [Table mapping](../sources/configuration.md#table-mapping) |
| Table attributes | Extra columns injected from file paths or constants | [Attributes](../sources/configuration.md#table-attributes) |
| Conflict resolution | How to handle table name collisions across readers | [Conflicts](../sources/configuration.md#conflicts) |

### Supported formats

| Format | Type value | Description |
|--------|-----------|-------------|
| [CSV](../sources/formats/csv.md) | `csv` | Comma/delimiter-separated text files |
| [TSV](../sources/formats/tsv.md) | `tsv` | Tab-separated value files |
| [FWF](../sources/formats/fwf.md) | `fwf` | Fixed-width text files |
| [Excel](../sources/formats/excel.md) | `excel` | Microsoft Excel (.xlsx, .xls) files |
| [Avro](../sources/formats/avro.md) | `avro` | Apache Avro binary data files |
| [Parquet](../sources/formats/parquet.md) | `parquet` | Apache Parquet columnar files |

---

## Examples

### CSV with semicolon delimiter

Query an airline dataset stored as semicolon-separated CSV files:

**Source descriptor** (`config/skymill-source.yaml`):

```yaml
name: skymill
storage:
  type: local
  rootPath: ../../test/datasets/skymill/csv
table:
  mapping:
    type: regex
    pattern: "(?<table>[^/]+)\\.csv"
readers:
  - type: csv
    format:
      delimiter: ";"
      hasHeader: true
```

**Application config:**

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - ./config/skymill-source.yaml
```

This exposes a `skymill` schema with tables like `cities`, `aircraft`, `bookings`, `passenger`, etc.

### Mixed formats (CSV + TSV)

Use multiple source descriptors to combine data in different formats. Each descriptor becomes a separate schema.

**CSV source** (`config/skymill-source.yaml`):

```yaml
name: skymill
storage:
  type: local
  rootPath: ../../test/datasets/skymill/csv
table:
  mapping:
    type: regex
    pattern: "(?<table>[^/]+)\\.csv"
readers:
  - type: csv
    format:
      delimiter: ";"
      hasHeader: true
```

**TSV source** (`config/skymill-ref-source.yaml`):

```yaml
name: skymill_ref
storage:
  type: local
  rootPath: ./config/skymill-ref
table:
  mapping:
    type: regex
    pattern: "(?<table>[^/]+)\\.tsv"
readers:
  - type: tsv
    format:
      hasHeader: true
```

**Application config:**

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - ./config/skymill-source.yaml
          - ./config/skymill-ref-source.yaml
```

This exposes two schemas: `skymill` (CSV tables) and `skymill_ref` (TSV tables). Queries can join across both schemas.

### Multi-reader single schema

A single source descriptor can have multiple readers with different formats. Use conflict resolution to control how tables are merged.

```yaml
name: warehouse
storage:
  type: local
  rootPath: /data/warehouse
table:
  mapping:
    type: directory
    depth: 1
conflicts: union
readers:
  - type: csv
    label: raw
    format:
      delimiter: ","
  - type: parquet
    label: processed
```

See [Configuration](../sources/configuration.md) for the full specification.

---

## Source Definition Repository

Under the hood, the Flow backend uses a `SourceDefinitionRepository` abstraction to load source descriptors. The auto-configuration creates a `MultiFileSourceRepository` from the configured paths. This architecture allows future implementations to load descriptors from other sources (e.g., a database).

| Repository | Description |
|------------|-------------|
| `SingleFileSourceRepository` | Reads a single source descriptor YAML file. |
| `MultiFileSourceRepository` | Reads multiple source descriptor YAML files. Validates that source names are unique across all files. |

---

## SQL Dialect

The Flow backend uses Apache Calcite for query processing. The default dialect is Calcite:

```yaml
mill:
  data:
    sql:
      dialect: CALCITE
      conventions:
        quoting: BACK_TICK
        caseSensitive: true
        unquotedCasing: UNCHANGED
```

See [Shared Configuration](index.md#sql-dialect) for the full list of supported dialects and convention properties.

---

## Next Steps

- [Data Sources](../sources/index.md) — detailed guide to writing source descriptors
- [Configuration](../sources/configuration.md) — full YAML specification for source descriptors
- [Format Reference](../sources/formats/csv.md) — format-specific configuration options
