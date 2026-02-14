# mill-source-calcite — Using Sources with Apache Calcite

Module `source/mill-source-calcite` exposes Mill file-based sources (CSV, TSV, FWF, Excel, Avro, Parquet) as Apache Calcite schemas and tables. It bridges the Mill source model (`SourceDescriptor` -> `ResolvedSource` -> `SourceTable`) into Calcite's `AbstractSchema` / `ScannableTable` hierarchy so that standard SQL can be executed against any source descriptor.

Package: `io.qpointz.mill.source.calcite`

Dependencies: `mill-source-core`, `mill-core`, `calcite-core 1.41`

---

## Architecture Overview

```
SourceDescriptor (YAML)
        |
  SourceMaterializer  (SPI: storage, format, mapping factories)
        |
  MaterializedSource
        |
  SourceResolver.resolve()
        |
  ResolvedSource { name, tables: Map<String, SourceTable> }
        |
  FlowSchema : AbstractSchema
        |
  FlowTable : ScannableTable   (one per SourceTable)
        |
  CalciteTypeMapper             (DatabaseType -> RelDataType)
```

The pipeline is deterministic:

1. A `SourceDescriptor` YAML file declares storage, format, table mapping, and conflict resolution.
2. `SourceMaterializer` uses SPI to create runtime objects (blob source, format handlers, table mappers).
3. `SourceResolver` discovers blobs, groups them into logical tables, infers schemas, and produces a `ResolvedSource`.
4. `FlowSchema` wraps the resolved source; each logical table becomes a `FlowTable`.
5. Calcite queries scan `FlowTable` instances via the standard `ScannableTable.scan()` contract.

---

## Quick Start

### 1. Write a source descriptor

Create `my-source.yaml`:

```yaml
name: airlines
storage:
  type: local
  rootPath: /data/airlines
table:
  mapping:
    type: directory
    depth: 1
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
```

This declares a local-filesystem source where first-level subdirectories become table names and files are parsed as CSV.

### 2a. Use via Calcite model JSON (standalone / JDBC)

Create a Calcite model file `model.json`:

```json
{
  "version": "1.0",
  "defaultSchema": "airlines",
  "schemas": [
    {
      "name": "airlines",
      "type": "custom",
      "factory": "io.qpointz.mill.source.calcite.FlowSchemaFactory",
      "operand": {
        "descriptorFile": "/path/to/my-source.yaml"
      }
    }
  ]
}
```

Connect via JDBC:

```kotlin
val props = Properties().apply {
    setProperty("model", "/path/to/model.json")
}
DriverManager.getConnection("jdbc:calcite:", props).use { conn ->
    val rs = conn.createStatement().executeQuery(
        "SELECT * FROM airlines.flights WHERE carrier = 'AA' LIMIT 10"
    )
    while (rs.next()) {
        println("${rs.getString("carrier")} - ${rs.getString("origin")}")
    }
}
```

### 2b. Use via programmatic API (embedded)

```kotlin
import io.qpointz.mill.source.calcite.FlowSchemaFactory
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.descriptor.SourceObjectMapper

// Option A: from a YAML file
val descriptor = SourceObjectMapper.yaml.readValue(
    File("/path/to/my-source.yaml"),
    SourceDescriptor::class.java
)
val schema = FlowSchemaFactory.createSchema(descriptor)

// Option B: from a pre-resolved source
val schema = FlowSchemaFactory.createSchema(resolvedSource)

// Use the schema
schema.flowTables().forEach { (name, table) ->
    println("Table: $name, columns: ${table.getRowType(typeFactory).fieldNames}")
}
```

### 2c. Register programmatically with a Calcite connection

```kotlin
import org.apache.calcite.jdbc.CalciteConnection
import java.sql.DriverManager

val conn = DriverManager.getConnection("jdbc:calcite:").unwrap(CalciteConnection::class.java)
val rootSchema = conn.rootSchema

// Create and register
val schema = FlowSchemaFactory.createSchema(descriptor)
rootSchema.add("airlines", schema)

conn.createStatement().executeQuery("SELECT * FROM airlines.flights").use { rs ->
    // ...
}
```

---

## Components

### CalciteTypeMapper

Singleton that converts Mill `DatabaseType` and `RecordSchema` to Calcite `RelDataType`. Uses a `LogicalTypeShuttle` internally to dispatch each Mill logical type to the corresponding `SqlTypeName`.

```kotlin
CalciteTypeMapper.toRelDataType(dbType: DatabaseType, typeFactory: RelDataTypeFactory): RelDataType
CalciteTypeMapper.toRelDataType(schema: RecordSchema, typeFactory: RelDataTypeFactory): RelDataType
```

#### Type mapping reference

| Mill LogicalType | Calcite SqlTypeName | Precision/Scale | Notes |
|---|---|---|---|
| `TinyIntLogical` | `TINYINT` | ignored | |
| `SmallIntLogical` | `SMALLINT` | ignored | |
| `IntLogical` | `INTEGER` | ignored | |
| `BigIntLogical` | `BIGINT` | ignored | |
| `BoolLogical` | `BOOLEAN` | ignored | |
| `FloatLogical` | `FLOAT` | ignored (Calcite FLOAT does not accept prec/scale) | |
| `DoubleLogical` | `DOUBLE` | ignored (Calcite DOUBLE does not accept prec/scale) | |
| `StringLogical` | `VARCHAR` | precision = length if set | |
| `BinaryLogical` | `VARBINARY` | precision = length if set | |
| `DateLogical` | `DATE` | ignored | |
| `TimeLogical` | `TIME` | ignored | |
| `TimestampLogical` | `TIMESTAMP` | ignored | |
| `TimestampTZLogical` | `TIMESTAMP_WITH_LOCAL_TIME_ZONE` | ignored | |
| `IntervalDayLogical` | `INTERVAL_DAY` | uses `createSqlIntervalType()` | |
| `IntervalYearLogical` | `INTERVAL_YEAR` | uses `createSqlIntervalType()` | |
| `UUIDLogical` | `VARCHAR` | no native Calcite UUID in row type | |

**Nullability** is always preserved from the Mill `DatabaseType.nullable()` flag.

**Precision/scale** are applied only when the target `SqlTypeName.allowsPrec()` / `allowsScale()` returns true. Types like `FLOAT` and `DOUBLE` silently ignore precision/scale values from the source.

**Interval types** require Calcite's `createSqlIntervalType(SqlIntervalQualifier)` instead of `createSqlType()`. The mapper handles this transparently.

#### Relationship to RelToDatabaseTypeConverter

The existing `RelToDatabaseTypeConverter` in `mill-data-backends` performs the **reverse** mapping (Calcite `RelDataType` -> Mill `DatabaseType`). `CalciteTypeMapper` provides the **forward** mapping (Mill -> Calcite) needed by `FlowTable.getRowType()`. Both should eventually be consolidated into a shared `mill-calcite` module.

### FlowTable

`AbstractTable + ScannableTable` backed by a Mill `SourceTable`. Each `FlowTable` corresponds to one logical table discovered by the source resolver (which may be backed by multiple files).

```kotlin
class FlowTable(private val sourceTable: SourceTable) : AbstractTable(), ScannableTable
```

**`getRowType(typeFactory)`** — delegates to `CalciteTypeMapper.toRelDataType(sourceTable.schema, typeFactory)`.

**`scan(root)`** — returns an `Enumerable<Object[]>` that iterates over `sourceTable.records()`. Each `Record` is projected into an `Object[]` in schema-field order (field 0 -> index 0, field 1 -> index 1, etc.). Null values are preserved as `null` entries in the array.

The scan is row-oriented: it uses `SourceTable.records()` which internally concatenates all underlying per-file `RecordSource` instances. For columnar-native formats (e.g. Parquet via `FlowVectorSource`), the bridge in `MultiFileSourceTable` converts vector blocks to records on the fly.

### FlowSchema

`AbstractSchema` wrapping a `ResolvedSource`. Each `SourceTable` in the resolved source is exposed as a `FlowTable`.

```kotlin
class FlowSchema(private val resolvedSource: ResolvedSource) : AbstractSchema()
```

**`getTableMap()`** — builds `FlowTable` instances on demand from `resolvedSource.tables`.

**`flowTables()`** — public accessor (Calcite's `getTableMap()` is `protected` in Java, which makes it inaccessible from Kotlin external code).

**`resolvedSource()`** — returns the underlying `ResolvedSource` for diagnostics or further programmatic use.

### FlowSchemaFactory

Calcite `SchemaFactory` implementation. Creates a `FlowSchema` from a source descriptor YAML file referenced by the Calcite model operand.

```kotlin
class FlowSchemaFactory : SchemaFactory
```

#### Operand: `descriptorFile`

The single required operand is `descriptorFile` — an absolute or relative path to a `SourceDescriptor` YAML file.

```json
{
  "operand": {
    "descriptorFile": "/etc/mill/sources/airlines.yaml"
  }
}
```

The factory:
1. Reads and parses the YAML via `SourceObjectMapper.yaml`
2. Materializes runtime components via `SourceMaterializer` (SPI-based factory discovery)
3. Resolves tables via `SourceResolver.resolve()`
4. Returns a `FlowSchema` wrapping the result

#### Programmatic companion API

For embedding scenarios where no descriptor file exists on disk:

```kotlin
// From a SourceDescriptor built in code
FlowSchemaFactory.createSchema(descriptor: SourceDescriptor): FlowSchema
FlowSchemaFactory.createSchema(descriptor: SourceDescriptor, classLoader: ClassLoader): FlowSchema

// From an already-resolved source
FlowSchemaFactory.createSchema(resolvedSource: ResolvedSource): FlowSchema
```

---

## Source Descriptor Reference (for Calcite use)

A source descriptor defines everything Calcite needs to expose tables. Key sections:

```yaml
name: <schema-name>          # becomes the Calcite schema name

storage:
  type: local                 # storage backend (SPI-extensible)
  rootPath: /data/warehouse   # root directory for blob discovery

table:
  mapping:
    type: directory           # how blobs map to tables
    depth: 1                  # directory depth for table names
  attributes:                 # optional computed columns
    - name: pipeline
      source: CONSTANT
      value: "raw-ingest"

conflicts: reject             # or "union", or per-table map

readers:
  - type: csv                 # format handler (SPI-extensible)
    format:
      delimiter: ","
      hasHeader: true
```

### Storage types

| Type | Key properties | Description |
|---|---|---|
| `local` | `rootPath` | Local filesystem directory |

Additional storage types (S3, Azure, etc.) can be added via SPI `StorageFactory` implementations.

### Table mapping types

| Type | Key properties | Description |
|---|---|---|
| `directory` | `depth` (default 1) | Subdirectory name at given depth becomes the table name |
| `regex` | `pattern`, `tableNameGroup` | Named capture group in the regex extracts the table name |

### Format types

| Type | Module | Description |
|---|---|---|
| `csv` | `mill-source-format-text` | Comma-separated values (Univocity parser) |
| `tsv` | `mill-source-format-text` | Tab-separated values (escape-based, Univocity parser) |
| `fwf` | `mill-source-format-text` | Fixed-width format (Univocity parser) |
| `excel` | `mill-source-format-excel` | Excel workbooks (Apache POI) |
| `avro` | `mill-source-format-avro-parquet` | Avro files |
| `parquet` | `mill-source-format-avro-parquet` | Parquet files (columnar) |

### Conflict resolution

When multiple readers produce tables with the same name:

- `reject` (default) — error, ambiguous table name
- `union` — merge files from all readers into one table
- Per-table overrides via map syntax:

```yaml
conflicts:
  default: reject
  orders: union
```

---

## Examples

### CSV files in subdirectories

Directory layout:

```
/data/warehouse/
  customers/
    part-001.csv
    part-002.csv
  orders/
    part-001.csv
```

Descriptor:

```yaml
name: warehouse
storage:
  type: local
  rootPath: /data/warehouse
table:
  mapping:
    type: directory
    depth: 1
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
```

Resulting Calcite tables: `customers` (2 files), `orders` (1 file).

```sql
SELECT * FROM warehouse.customers WHERE country = 'US';
SELECT o.order_id, c.name
  FROM warehouse.orders o
  JOIN warehouse.customers c ON o.customer_id = c.id;
```

### Parquet files with regex mapping

```yaml
name: events
storage:
  type: local
  rootPath: /data/events
table:
  mapping:
    type: regex
    pattern: "(?<table>[^/]+)_\\d{8}\\.parquet$"
    tableNameGroup: table
readers:
  - type: parquet
```

Files like `clicks_20260101.parquet`, `clicks_20260102.parquet`, `views_20260101.parquet` produce tables `clicks` and `views`.

### Mixed formats with labels

```yaml
name: mixed
storage:
  type: local
  rootPath: /data/mixed
table:
  mapping:
    type: directory
readers:
  - type: csv
    label: csv
    format:
      delimiter: ","
  - type: parquet
    label: pq
conflicts: reject
```

If both readers discover an `orders` directory, they produce `orders_csv` and `orders_pq` (label suffix prevents collision).

### Excel workbook with sheet selection

```yaml
name: reports
storage:
  type: local
  rootPath: /data/reports
table:
  mapping:
    type: directory
readers:
  - type: excel
    format:
      allSheets: true
      excludeSheets:
        - Summary
      hasHeader: true
```

All sheets except "Summary" are combined into one table per workbook file.

### Programmatic: build descriptor in code

```kotlin
import io.qpointz.mill.source.calcite.FlowSchemaFactory
import io.qpointz.mill.source.descriptor.*

val descriptor = SourceDescriptor(
    name = "my_data",
    storage = LocalStorageDescriptor(rootPath = "/data/my_data"),
    table = TableDescriptor(
        mapping = DirectoryTableMappingDescriptor(depth = 1)
    ),
    readers = listOf(
        ReaderDescriptor(
            type = "csv",
            format = CsvFormatDescriptor(delimiter = ",", hasHeader = true)
        )
    )
)

val schema = FlowSchemaFactory.createSchema(descriptor)
// schema is now a Calcite AbstractSchema ready for SQL
```

---

## Data Flow: from file bytes to SQL result

```
  File on disk (CSV/Parquet/Excel/...)
       |
  BlobSource.openInputStream(blobPath)
       |
  FormatHandler.createRecordSource(blob, blobSource)
       |
  RecordSource (FlowRecordSource or FlowVectorSource)
       |
  MultiFileSourceTable.records()  -- concatenates all per-file sources
       |
  FlowTable.scan(DataContext)
       |
  Enumerator<Object[]>  -- projects Record values into positional array
       |
  Calcite query engine  -- filter, project, join, aggregate, sort
       |
  JDBC ResultSet / Enumerable result
```

Each step is lazy: blobs are opened on demand, records are streamed through iterators, and Calcite applies push-down where possible.

---

## Limitations and Known Gaps

| Area | Limitation |
|---|---|
| **Table scans only** | `FlowTable` implements `ScannableTable`, not `FilterableTable` or `ProjectableFilterableTable`. Calcite applies filters after the full scan. |
| **Row-oriented scan** | Even for columnar formats (Parquet), the scan currently materializes rows via `SourceTable.records()`. A future `FlowTable` could implement Calcite's `Enumerable` directly from vector blocks. |
| **No schema caching** | `FlowSchema.getTableMap()` rebuilds the table map on each call. Phase 5 adds Caffeine-based caching at the `BlobSource` and `FormatHandler` levels (not at `FlowSchema` — caching belongs where the I/O happens so all consumers benefit). |
| **Single-source schemas** | One `FlowSchema` = one `SourceDescriptor`. Use `SourceSchemaManager` to hold multiple named schemas and register them into a Calcite root schema. |
| **UUID as VARCHAR** | Mill's `UUIDLogical` maps to `VARCHAR` in Calcite since Calcite's native UUID type is not available in all row-type factories. |
| **Interval types** | `IntervalDayLogical` and `IntervalYearLogical` map to Calcite interval types but may not round-trip correctly through all Calcite adapters. |
| **No write path** | `FlowTable` is read-only. Calcite `ModifiableTable` is not implemented. |
| **Type mapper consolidation** | `CalciteTypeMapper` duplicates logic from `RelToDatabaseTypeConverter` (reverse direction). Both should move to a shared `mill-calcite` module. |

---

## SourceSchemaManager

Multi-schema lifecycle manager. Holds named `FlowSchema` instances, supports add/remove/get, and can bulk-register them into a Calcite `SchemaPlus` root schema. `AutoCloseable` — closing it closes all managed resolved sources.

Package: `io.qpointz.mill.source.calcite`

### Usage

```kotlin
val manager = SourceSchemaManager()

// Add from descriptor
manager.add(airlineDescriptor)

// Add from pre-resolved source
manager.add("warehouse", resolvedWarehouse)

// Register into Calcite
val rootSchema = Frameworks.createRootSchema(true)
manager.registerAll(rootSchema)

// Query...

// Clean up
manager.close()
```

### API

| Method | Description |
|---|---|
| `add(descriptor)` | Materializes + resolves + registers. Name comes from `descriptor.name`. |
| `add(name, resolvedSource)` | Registers a pre-resolved source. |
| `remove(name)` | Removes and closes the schema. |
| `get(name)` | Returns `FlowSchema` or `null`. |
| `names()` | Returns all managed schema names. |
| `size` / `isEmpty()` | Size queries. |
| `registerAll(rootSchema)` | Adds all schemas as sub-schemas of the Calcite root. |
| `close()` | Closes all managed resolved sources and clears the manager. |

Replacing a schema (calling `add` with an existing name) closes the previous resolved source automatically.

---

## Source Discovery

Non-failing discovery pipeline in `mill-source-core` (`io.qpointz.mill.source.discovery`).

Runs the full pipeline (materialize, list blobs, map tables, infer schemas) and returns structured findings. Never throws — each step is wrapped in try/catch so partial results are always returned.

Key difference from `SourceVerifier`: verification asks "is this valid?", discovery asks "what did you find?".

### Usage

```kotlin
val result = SourceDiscovery.discover(descriptor)

// What did we find?
result.tables.forEach { table ->
    println("${table.name}: ${table.blobPaths.size} files, ${table.schema?.size ?: 0} columns")
}

// Any issues?
result.issues.forEach { issue ->
    println("[${issue.severity}] ${issue.message}")
}

// With sample records
val result2 = SourceDiscovery.discover(descriptor, DiscoveryOptions(maxSampleRecords = 5))
result2.tables.flatMap { it.sampleRecords }.forEach { println(it) }
```

### Model

- `DiscoveryResult` — top-level: `tables`, `issues`, `blobCount`, `unmappedBlobCount`, `isSuccessful`
- `DiscoveredTable` — per-table: `name`, `schema`, `blobPaths`, `readerType`, `readerLabel`, `sampleRecords`
- `DiscoveryOptions` — configuration: `maxSampleRecords`

---

## Test Coverage

| Test class | Tests | Focus |
|---|---|---|
| `CalciteTypeMapperTest` | 23 | All 16 logical types, nullability, precision/scale, interval types, empty schema, struct type |
| `FlowTableTest` | 4 | Row type derivation, scan with data, empty table, field ordering |
| `FlowSchemaTest` | 3 | Table map construction, empty tables, resolved source exposure |
| `FlowSchemaFactoryTest` | 5 | Missing file, missing operand, directory path, invalid YAML, programmatic creation |
| `SourceSchemaManagerTest` | 10 | Add/remove/replace, register into Calcite, lifecycle/close |
| `SourceDiscoveryTest` | 14 | Happy path, samples, empty/missing storage, partial matches, conflicts, model tests |
