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
