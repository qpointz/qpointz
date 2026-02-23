# Mill

Mill is a data access platform that provides unified SQL query execution across relational databases and file-based data sources. It includes an AI-assisted natural language interface for querying data without writing SQL, a metadata management layer for enriching data with business context, and client libraries for integration with applications and data tools.

---

## Architecture

Mill exposes data through a service layer (gRPC, HTTP, and a web UI) backed by pluggable backend providers. Each backend handles schema discovery, SQL parsing, and query execution for a specific type of data source.

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

## Features

| Area | Description |
|------|-------------|
| **Natural language queries** | Ask questions in plain language. Mill translates them to SQL, executes the query, and returns tabular results, charts, or explanations. |
| **Unified SQL access** | Query PostgreSQL, MySQL, SQL Server, Oracle, H2, Snowflake, and file formats (CSV, Parquet, Avro, Excel) through a single SQL interface. |
| **Metadata management** | Add descriptions, value mappings, business concepts, and relationships to tables and columns. Metadata improves AI query accuracy and serves as a data catalog. |
| **Access control** | Role-based table access, row-level filtering, and column-level restrictions. Supports OAuth2, JWT, Microsoft Entra ID, and basic authentication. |
| **Web UI (Grinder)** | Browser-based interface with a chat view, data model explorer, and context manager. Supports light and dark themes. |
| **Client libraries** | REST API, gRPC API, JDBC driver, and Python client for integration with applications, BI tools, and notebooks. |
| **Deployment** | Docker, Docker Compose, and Kubernetes. |

---

## Documentation

- [Quickstart](quickstart.md) — run Mill with sample data using Docker
- [Installation](installation.md) — deployment and configuration
- [Backends](backends/index.md) — JDBC, Calcite, and Flow backend configuration
- [Sources](sources/index.md) — file-based data source configuration
- [Grinder UI](grinder-ui.md) — web interface reference
- [Python Client](connect/python/index.md) — Python client library
