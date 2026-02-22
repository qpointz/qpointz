# JDBC Backend

The JDBC backend connects Mill to an existing relational database through a standard JDBC driver. Queries are parsed and planned inside Mill using Apache Calcite, then executed against the target database. This is the natural choice when your data already lives in PostgreSQL, MySQL, SQL Server, Oracle, H2, or any other JDBC-compatible database.

---

## How It Works

1. Mill opens a JDBC connection to the target database using the configured URL and credentials.
2. Apache Calcite introspects the database catalog to discover schemas, tables, and columns.
3. When a query arrives, Mill parses the SQL, converts it to a Substrait plan, and executes it through the JDBC connection.
4. Results are streamed back as columnar vector blocks.

The JDBC backend can expose a single schema (the default) or multiple schemas from the same database. Calcite handles the SQL-to-JDBC translation, so Mill's SQL dialect can differ from the target database's native dialect.

---

## Configuration

Activate the JDBC backend by setting `mill.data.backend.type` to `jdbc`. JDBC-specific properties go under `mill.data.backend.jdbc`.

### Minimal example

```yaml
mill:
  data:
    backend:
      type: jdbc
      jdbc:
        url: "jdbc:postgresql://localhost:5432/mydb"
        driver: "org.postgresql.Driver"
        user: "myuser"
        password: "mypassword"
```

### Full example

```yaml
mill:
  data:
    sql:
      dialect: H2
      conventions:
        quoting: DOUBLE_QUOTE
        caseSensitive: true
        unquotedCasing: UNCHANGED
    backend:
      type: jdbc
      jdbc:
        url: "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM './data/init.sql'"
        driver: "org.h2.Driver"
        user: "sa"
        password: "sa"
        schema: "PUBLIC"
        target-schema: "mydata"
        multi-schema: false
```

Here, tables from the `PUBLIC` database schema are exposed in Mill under the name `mydata` (e.g. `SELECT * FROM mydata.customers`).

### Properties reference

All properties are under the `mill.data.backend.jdbc` prefix.

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `url` | yes | — | JDBC connection URL. |
| `driver` | yes | — | Fully qualified JDBC driver class name. |
| `user` | no | — | Database username. |
| `password` | no | — | Database password. |
| `schema` | no | — | Database schema to read tables from. This is passed as `jdbcSchema` to the underlying JDBC connection and determines which tables are discovered. |
| `catalog` | no | — | Database catalog to read from. Passed as `jdbcCatalog` to the JDBC connection. |
| `target-schema` | no | `jdbc` | The schema name as it appears in Mill. This is the name used in SQL queries to qualify tables (e.g. `SELECT * FROM target_name.my_table`). Defaults to `jdbc` if not set. |
| `multi-schema` | no | `false` | When `true`, discover and expose all schemas from the database. Each database schema is registered under its own name. When `false`, a single schema is exposed under the `target-schema` name. |

---

## Supported Databases

Any database with a JDBC driver should work. Commonly tested databases:

| Database | Driver class | Example URL |
|----------|-------------|-------------|
| PostgreSQL | `org.postgresql.Driver` | `jdbc:postgresql://host:5432/db` |
| MySQL | `com.mysql.cj.jdbc.Driver` | `jdbc:mysql://host:3306/db` |
| H2 (in-memory) | `org.h2.Driver` | `jdbc:h2:mem:test` |
| H2 (file) | `org.h2.Driver` | `jdbc:h2:file:./data/mydb` |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | `jdbc:sqlserver://host:1433;databaseName=db` |
| Oracle | `oracle.jdbc.OracleDriver` | `jdbc:oracle:thin:@host:1521:orcl` |

Make sure the corresponding JDBC driver JAR is on the classpath.

---

## Single Schema vs Multi-Schema

By default, the JDBC backend exposes a single schema. Use `schema` to select which database schema to read from, and `target-schema` to control how it appears in Mill:

```yaml
mill:
  data:
    backend:
      type: jdbc
      jdbc:
        url: "jdbc:h2:mem:test"
        driver: "org.h2.Driver"
        schema: "PUBLIC"
        target-schema: "SALES"
```

In this example, tables are read from the `PUBLIC` database schema but exposed in Mill as `SALES.table_name`.

To expose multiple schemas at once, enable `multi-schema`. In this mode, Mill discovers all schemas from the database and registers each one under its actual database schema name:

```yaml
mill:
  data:
    backend:
      type: jdbc
      jdbc:
        url: "jdbc:postgresql://localhost:5432/mydb"
        driver: "org.postgresql.Driver"
        user: "myuser"
        password: "mypassword"
        multi-schema: true
```

Tables are then qualified as `schema_name.table_name` in queries. The `target-schema` and `schema` properties are ignored when `multi-schema` is `true`.

---

## SQL Dialect

The JDBC backend uses Apache Calcite for SQL parsing. The target database's native SQL dialect may differ from what users write. Configure the dialect to match your database:

```yaml
mill:
  data:
    sql:
      dialect: POSTGRES
      conventions:
        quoting: DOUBLE_QUOTE
        caseSensitive: true
        unquotedCasing: TO_LOWER
```

See [Shared Configuration](index.md#sql-dialect) for the full list of supported dialects and convention properties.

---

## Example: H2 with init script

A self-contained setup using H2's in-memory database with an SQL init script:

```yaml
mill:
  data:
    sql:
      dialect: H2
      conventions:
        quoting: DOUBLE_QUOTE
        caseSensitive: true
        unquotedCasing: UNCHANGED
    backend:
      type: jdbc
      jdbc:
        url: "jdbc:h2:mem:demo;INIT=RUNSCRIPT FROM './data/schema.sql'"
        driver: "org.h2.Driver"
        user: "sa"
        password: "sa"
        schema: "DEMO"
        target-schema: "DEMO"
```

This creates an in-memory H2 database, runs `schema.sql` to create tables and load data, reads tables from the `DEMO` database schema, and exposes them under the `DEMO` name in Mill.
