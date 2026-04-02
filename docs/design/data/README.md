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
| `implementing-backend-metadata-source.md` | **Implementer guide:** `MetadataSource` for a data backend — foundation (`AbstractInferredMetadataSource`, `MetadataOriginIds`), per-backend facet URNs/category/payloads, JDBC vs Flow contrast, checklist |
