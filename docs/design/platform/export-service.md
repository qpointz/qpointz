# Streaming HTTP export service

## Purpose

The **`mill-export-service`** module exposes **`/services/export`** for **bounded-memory** exports: encoders stream rows to an `OutputStream` using `VectorBlockIterator` batches. Execution lives in **`mill-data-backend-core`** (`ExportVectorBlockSource`); format modules implement **`ExportFormatProvider`** (SPI) and are aggregated by **`ExportFormatRegistry`** in **`mill-data-autoconfigure`**.

## Endpoints

| Method | Path | Role |
|--------|------|------|
| GET | `/services/export/formats` | Effective HTTP format list (after `mill.data.services.export.formats` allowlist). |
| GET | `/services/export/catalog` | Root `formats` plus full schema/table tree with export URLs. |
| GET | `/services/export/schemas` | Physical schemas with table counts and links. |
| GET | `/services/export/schemas/{schema}` | Tables in a schema with per-format URLs. |
| GET | `/services/export/schemas/{schema}/tables/{table}?format=` | Table export (Substrait named scan → plan on dispatcher; no hand-written `SELECT *` string in the HTTP layer). |
| POST | `/services/export/sql?format=` | Ad-hoc SQL export (`Content-Type: text/plain` body). |

## Configuration

Prefix **`mill.data.services.export`** (see **`ExportServiceProperties`**):

| Key | Meaning |
|-----|---------|
| `enable` | Gates all export beans via `@ConditionalOnService(value = "export", group = "data")` → **`mill.data.services.export.enable`**. |
| `external-host` | Logical key into **`mill.application.hosts.externals.&lt;name&gt;`** for absolute URLs in catalog responses (same pattern as HTTP data-plane). |
| `formats` | Allowlist of format ids for HTTP; empty or `*` keeps all SPI formats visible. Unknown ids are logged and skipped. |

## Discovery

**`ExportServiceDescriptor`** (`name`: **`data-export`**) and **`ExportConnectionDescriptor`** (`api-path`: **`/services/export/`**) participate in **`/.well-known/mill`** when export is enabled.

## Security

Export controllers live under **`/services/**`** and follow the same Spring Security rules as other data-plane HTTP surfaces (see **`ServicesSecurityConfiguration`**).

## Formats (SPI)

Bundled JARs register providers via **`META-INF/services/io.qpointz.mill.source.export.ExportFormatProvider`**:

- **`mill-data-format-arrow`** (`io.qpointz.mill.source.format.arrow.export`) — Arrow IPC stream.
- **`mill-data-format-excel`** (`io.qpointz.mill.source.format.excel.export`) — XLSX (SXSSF row window).
- **`mill-data-format-text`** (`io.qpointz.mill.source.format.text.export`) — CSV, TSV.
- **`mill-data-format-avro`** (`io.qpointz.mill.source.format.avro.export`) — Avro OCF.
- **`mill-data-format-json`** — JSON array of objects.

**Parquet** is not wired as an export encoder in this iteration; use Arrow or CSV for wide analytics interchange.

## OpenAPI

Handlers are annotated with **`@Tag("export")`** and **`@Operation`** for SpringDoc; they appear in the main **`api`** OpenAPI group (paths are not under **`/.well-known`**).

## Operational limits

Row caps, statement timeouts, and maximum response sizes are **not** enforced in the export service in this iteration; deployments should use platform limits (reverse proxy, DB session settings, dispatcher timeouts) as needed. Follow-up work can add explicit **`mill.data.services.export.*`** limit keys.
