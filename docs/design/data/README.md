# Data Layer

Design documents for Mill's data layer: the type system, vector wire formats, schema
definitions, and data encoding.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Mill logical/physical type system (scalar types, complex types, type mappings)
- Vector wire format design (protobuf Vector messages, columnar encoding)
- Schema representation (DataType, Field, VectorBlockSchema)
- Data encoding and serialization algorithms (flattening, reconstruction, PathSegment)
- Type mapping tables (proto ↔ Java ↔ JDBC ↔ Python ↔ Arrow)
- Schema aggregation boundaries that merge physical schema with schema-bound metadata
- Flow **facet projection** extensibility when it concerns **schema explorer / metadata payloads** co-located with data-layer aggregation (see also `source/` for descriptor runtime).
- **Backend `MetadataSource` implementation** guide: shared foundation vs per-backend facet families (`originId`, `category`, payloads).
- Future data formats or encoding strategies

## Does NOT Belong Here

- Data source framework design (provider, storage, Calcite adapter) → `source/`
- Client-side type consumption (how mill-py or JDBC reads types) → `client/`
- gRPC/HTTP data export protocol → `platform/`
- Data export service protocol specification → `platform/`

## Documents

| File | Description |
|------|-------------|
| `complex-type-support.md` | Design plan for JSON, LIST, MAP, OBJECT types: proto, wire format, algorithms |
| `mill-type-system.md` | Mill schema and type system reference: protobuf, Java, vector, JDBC/Python mappings |
| `schema-facet-service.md` | `SchemaFacetService` aggregation boundary: domain model, matching logic, module placement |
| `flow-facet-projection-extensibility.md` | Extensible **`flow-*`** facet payloads: polymorphic storage/readers, contributor SPI, facet **context** types, same facets for alternate `SourceDefinitionRepository` implementations |
| `implementing-backend-metadata-source.md` | **Implementer guide:** `MetadataSource` for a data backend — foundation (`AbstractInferredMetadataSource`, `MetadataOriginIds`), per-backend facet URNs/category/payloads, global metadata gating (`BackendMetadataProperties`), redaction modes, per-source overrides (Flow), JDBC vs Flow contrast, checklist |
| `object-storage-emulator-docker.md` | **Docker / Compose** recipes for **blob `testIT` emulators** (**MinIO**, optional **LocalStack**, **fake-gcs-server**, **Azurite**): ports, env, smoke checks, Testcontainers parity |
| `cloud-blob-storage-auth-descriptors.md` | **GAP-4** — frozen **`storage.auth`** for **S3 / GCS / Azure** blob descriptors (delegated bundles, **`preferAmbientCredentials`**, verifier messages, emulator **`testIT` alignment`). **WI-262–264** implement these keys. |
