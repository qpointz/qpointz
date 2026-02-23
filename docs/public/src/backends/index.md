# Backends

A backend is the execution engine that Mill uses to process SQL queries, discover schemas, and return results. You choose a backend by setting `mill.data.backend.type` in your application configuration.

---

## Available Backends

| Backend | Type value | Best for |
|---------|-----------|----------|
| [JDBC](jdbc.md) | `jdbc` | Querying existing relational databases (PostgreSQL, MySQL, H2, etc.) |
| [Calcite](calcite.md) | `calcite` | Federated queries using a Calcite model file |
| [Flow](flow.md) | `flow` | Querying file-based data sources (CSV, TSV, Parquet, Avro, Excel) described by source descriptors |

---

## How It Works

All three backends share the same internal architecture. Regardless of which backend you choose, Mill provides:

- **Schema discovery** — browse schemas, tables, and columns
- **SQL parsing** — parse SQL text into Substrait query plans
- **Query execution** — execute plans and stream results as columnar vector blocks

The backend determines *where* data comes from and *how* it is accessed. The JDBC backend delegates to an external database via a JDBC driver. The Calcite and Flow backends use Apache Calcite's in-process query engine — Calcite reads from a model file, while Flow reads from Mill's own source descriptor files.

```
┌─────────────────────────┐
│    Mill Service Layer    │
│  (gRPC, HTTP, Grinder)  │
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│    Backend Providers     │
│  Schema · SQL · Execute  │
└───────────┬─────────────┘
            │
   ┌────────┼────────┐
   │        │        │
┌──▼──┐ ┌──▼───┐ ┌──▼──┐
│ JDBC│ │Calcite│ │ Flow│
└─────┘ └──────┘ └─────┘
```

---

## Shared Configuration

All backends share a common configuration prefix `mill.data.backend` and inherit SQL dialect settings from `mill.data.sql`.

### Backend selection

```yaml
mill:
  data:
    backend:
      type: jdbc        # jdbc | calcite | flow
```

| Property | Default | Description |
|----------|---------|-------------|
| `mill.data.backend.type` | `calcite` | Backend identifier. Activates the corresponding auto-configuration. |

### SQL dialect

Controls how Mill parses and generates SQL. The dialect determines identifier quoting, casing rules, and function compatibility.

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

| Property | Default | Description |
|----------|---------|-------------|
| `mill.data.sql.dialect` | `CALCITE` | SQL dialect identifier. |
| `mill.data.sql.conventions` | — | Map of connection-level overrides (quoting, casing, etc.). |

#### Supported dialects

| Dialect | Identifier |
|---------|------------|
| Apache Calcite | `CALCITE` |
| PostgreSQL | `POSTGRES` |
| MySQL | `MYSQL` |
| Microsoft SQL Server | `MSSQL` |
| Oracle | `ORACLE` |
| H2 | `H2` |
| Trino | `TRINO` |
| DuckDB | `DUCKDB` |
| Databricks (Spark SQL) | `DATABRICKS` |
| IBM DB2 | `DB2` |

#### Convention properties

Convention properties are passed to the Calcite connection and control how identifiers are handled at the SQL parser level.

| Property | Values | Description |
|----------|--------|-------------|
| `quoting` | `DOUBLE_QUOTE`, `BACK_TICK`, `BRACKET` | How quoted identifiers are delimited. |
| `caseSensitive` | `true`, `false` | Whether identifier matching is case-sensitive. |
| `unquotedCasing` | `UNCHANGED`, `TO_UPPER`, `TO_LOWER` | How unquoted identifiers are cased. |

---

## Choosing a Backend

**Use JDBC** when your data already lives in a relational database. Mill connects via a standard JDBC driver, delegates query execution to the database engine, and returns results. This is the most common setup for production deployments.

**Use Calcite** when you need Apache Calcite's full federation capabilities — custom adapters, in-memory tables, or a hand-crafted Calcite model file. This backend gives you direct control over Calcite's schema definition.

**Use Flow** when your data lives in files (CSV, TSV, Parquet, Avro, Excel) and you want Mill to discover and query them without loading into a database first. Flow uses Mill's [source descriptor](../sources/index.md) format to define where files are, how to read them, and which tables they represent.

---

## Next Steps

- [JDBC Backend](jdbc.md) — connect to relational databases
- [Calcite Backend](calcite.md) — federated queries with Calcite models
- [Flow Backend](flow.md) — query file-based data sources
- [Source Configuration](../sources/index.md) — how to describe file-based data sources (used by Flow)
