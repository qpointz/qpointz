# Data Source Framework

Design documents for the Mill data source provider framework: how file-based and external
data sources are exposed as schemas and tables.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Source provider architecture (Source = Schema, TableMapper = Tables)
- File-based data formats (CSV, FWF, Excel, Avro, Parquet, Arrow) as Mill sources
- Storage abstraction layer (local, S3, etc.)
- Calcite adapter integration (mill-source-calcite, schema/table exposure)
- Source-specific Kotlin/Java design (flow-kt, record bridges)

## Does NOT Belong Here

- Mill type system and vector encoding → `data/`
- Calcite dialect operator comparison (platform concern) → `platform/`
- gRPC data export protocol (platform protocol) → `platform/`
- Client-side consumption of source data → `client/`

## Documents

| File | Description |
|------|-------------|
| [`formats/README.md`](formats/README.md) | Format capability index; per-format design pages with shared feature table |
| `arrow-format-design.md` | Arrow format-first design: source module, type mapping contract, and phased integration before Flight |
| `flow-kt-design.md` | Kotlin design for file-based data as a Mill provider with storage abstraction |
| `mill-source-calcite.md` | How mill-source-calcite exposes Mill file-based sources as Calcite schemas/tables |
| `flow-backend.md` | Flow backend: source-descriptor-driven Calcite backend with repository abstraction |
| `format-statistics-descriptor.md` | Follow-up: `format.statistics.mode` (`none` \| `approximate` \| `exact`) |
| `cloud-blob-flow-sources.md` | Flow + **`SourceDescriptor`**: **`local`** vs cloud **`storage.type`** (**S3 / GCS / Azure**), auth (**GAP-4**), emulator **`testIT`**, facet payload extension |
