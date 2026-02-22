# Calcite Backend

The Calcite backend uses an [Apache Calcite model file](https://calcite.apache.org/docs/model.html) to define schemas and tables. This gives you full access to Calcite's federation capabilities — custom adapters, in-memory tables, JDBC sources, and any other schema factory that Calcite supports.

This is the default backend when no `mill.data.backend.type` is specified.

---

## How It Works

1. Mill reads the Calcite model file (YAML or JSON) to discover schemas and their factories.
2. Apache Calcite creates the schemas and tables described in the model.
3. Queries are parsed, planned, and executed entirely within Calcite's in-process engine.
4. Results are streamed back as columnar vector blocks.

The Calcite backend is the most flexible option — anything Calcite can connect to, Mill can query. The model file is a standard Calcite artifact, so existing Calcite configurations work without modification.

---

## Configuration

Activate the Calcite backend by setting `mill.data.backend.type` to `calcite` (or omitting it, since `calcite` is the default). The model file path goes under `mill.data.backend.calcite.model`.

### Minimal example

```yaml
mill:
  data:
    backend:
      type: calcite
      calcite:
        model: ./config/my-model.yaml
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
      type: calcite
      calcite:
        model: ./config/my-model.yaml
```

### Properties reference

All properties are under the `mill.data.backend.calcite` prefix.

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `model` | yes | — | Path to the Calcite model file (YAML or JSON). Relative paths are resolved from the working directory. |

---

## Calcite Model File

The model file is a standard [Apache Calcite model](https://calcite.apache.org/docs/model.html). It defines one or more schemas, each backed by a schema factory or an inline table list.

### YAML example

```yaml
version: "1.0"
defaultSchema: "myschema"
schemas:
  - name: myschema
    type: custom
    factory: com.example.MySchemaFactory
    operand:
      directory: /data/warehouse
```

### JSON example

```json
{
  "version": "1.0",
  "defaultSchema": "myschema",
  "schemas": [
    {
      "name": "myschema",
      "type": "custom",
      "factory": "com.example.MySchemaFactory",
      "operand": {
        "directory": "/data/warehouse"
      }
    }
  ]
}
```

Refer to the [Apache Calcite documentation](https://calcite.apache.org/docs/model.html) for the full model specification, including JDBC schemas, inline tables, materialized views, and lattices.

---

## When to Use Calcite

The Calcite backend is the right choice when:

- You have an existing Calcite model file and want to expose it through Mill.
- You need custom Calcite adapters or schema factories.
- You want to federate across multiple data sources in a single Calcite model.
- You are prototyping with Calcite's built-in adapters (CSV, file, JDBC).

For querying a single relational database, the [JDBC backend](jdbc.md) is simpler to configure. For querying file-based data using Mill's source descriptor format, the [Flow backend](flow.md) is more convenient.

---

## SQL Dialect

The Calcite backend defaults to the Calcite SQL dialect, which uses back-tick quoting and case-sensitive identifiers. Override this via the shared SQL configuration:

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
