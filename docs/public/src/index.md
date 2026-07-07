# Mill

Mill is a data access platform that provides unified SQL query execution across relational databases and file-based data sources. It includes an AI-assisted natural language interface for querying data without writing SQL, a metadata management layer for enriching data with business context, and client libraries for integration with applications and data tools.

---

## Architecture

Mill exposes data through a service layer (gRPC, HTTP, and a web UI) backed by pluggable backend providers. Each backend handles schema discovery, SQL parsing, and query execution for a specific type of data source.

```
┌─────────────────────────┐
│    Mill Service Layer    │
│   (gRPC, HTTP, Mill UI)  │
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
| **Natural language queries** | Agentic chat on **`/api/v1/ai/chats`**: ask in plain language, get SQL artefacts with optional **chart visualizations** (Chart / Data / SQL tabs), tables, or explanations. |
| **Unified SQL access** | Query PostgreSQL, MySQL, SQL Server, Oracle, H2, Snowflake, and file formats (CSV, Parquet, Avro, Excel) through a single SQL interface. **OData v4** read endpoints at **`/services/odata/{schema}.svc`** for BI tools. |
| **Metadata management** | Catalog-generic **metadata authoring** in chat, **concepts** and **ai-annotation** facets, facet JSON Schema in admin, value mappings, and relationships. Metadata improves AI query accuracy and serves as a data catalog. |
| **Access control** | Role-based table access, row-level filtering, and column-level restrictions. Supports OAuth2, JWT, Microsoft Entra ID, and basic authentication. |
| **Web UI (Mill UI)** | Browser-based interface with agentic **chat**, **data model** explorer (multi-scope read), **analysis** SQL playground, and context manager. Supports light and dark themes. |
| **Client libraries** | REST API, gRPC API, JDBC driver, and Python client for integration with applications, BI tools, and notebooks. |
| **Deployment** | Docker, Docker Compose, and Kubernetes. |

---

## Documentation

- [Quickstart](quickstart.md) — run Mill with sample data using Docker
- [Installation](installation.md) — Java **21**, Gradle build commands, Docker pointer
- [Security](security/index.md) — OIDC with Authentik and Mill Service / Mill UI
- [Platform runtime](reference/platform-runtime.md) — Spring Boot **4** / Spring AI **2** milestone / Jackson **3** baselines for services
- [Backends](backends/index.md) — JDBC, Calcite, and Flow backend configuration
- [Sources](sources/index.md) — file-based data source configuration
- [Mill UI](mill-ui.md) — web interface (chat, model explorer, analysis playground)
- [Metadata in Mill UI](metadata/mill-ui.md) — facets, authoring, concepts, ai-annotations
- [OData access](data-access/odata.md) — OData v4 read service for BI tools
- [Python Client](connect/python/index.md) — Python client library
