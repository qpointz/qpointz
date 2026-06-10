# BackendContextRunner — Lightweight Test Rig for Backend Providers

## 1. Motivation

Backend tests in `mill-data-backends` rely on `@SpringBootTest` with `@ActiveProfiles`,
`@ContextConfiguration`, `@DirtiesContext`, and YAML configuration files to wire providers.
This is heavyweight: each test class spins up a full Spring application context, is slow to
start, and couples tests to configuration that has nothing to do with the logic under test.

`BackendContextRunner` replaces this pattern with a plain-object test rig that:

- Wires providers programmatically (no Spring context)
- Supports mock injection via `withX()` mutation methods
- Cascades dependency rebuilds when an upstream provider is overridden
- Defaults `SecurityProvider` to `NoneSecurityProvider` for test simplicity

## 2. Architecture

### Class Hierarchy

```
BackendContextRunner (abstract)
├── JdbcBackendContextRunner
└── CalciteBackendContextRunner
```

### Core Mechanics

**Template methods** — subclasses implement `buildCalciteContextFactory()`,
`buildExecutionProvider()`, `buildSchemaProvider()`, `buildSqlProvider()`, `buildPlanConverter()`
to define how each provider is constructed from backend-specific infrastructure.

**Override fields** — each provider has an optional override (`executionProviderOverride: T?`)
passed via constructor. When set, the override takes precedence over the template method.

**Lazy properties** — `val executionProvider by lazy { override ?: buildX() }`. Because `lazy`
evaluates on first access, and `buildExecutionProvider()` references `this.planConverter`
(another lazy property), cascading works automatically.

**`derive()` abstract method** — creates a new instance of the same concrete type, copying all
infrastructure state but applying new overrides. Called by `withExecution()`, `withSchema()`, etc.

### Cascade Behavior

When `runner.withCalciteContextFactory(mockCF)` is called:

1. A new runner instance is created with `calciteContextFactoryOverride = mockCF`
2. `runner.calciteContextFactory` resolves to `mockCF`
3. `runner.planConverter` calls `buildPlanConverter()` which references `this.calciteContextFactory` → gets `mockCF`
4. `runner.executionProvider` calls `buildExecutionProvider()` which references `this.planConverter` and/or `this.calciteContextFactory` → both use `mockCF`
5. `runner.schemaProvider` calls `buildSchemaProvider()` which references `this.calciteContextFactory` → gets `mockCF`
6. `runner.sqlProvider` calls `buildSqlProvider()` which references `this.calciteContextFactory` → gets `mockCF`

When `runner.withPlanConverter(mockPC)` is called:

1. A new runner instance is created with `planConverterOverride = mockPC`
2. `runner.planConverter` resolves to `mockPC`
3. `runner.executionProvider` calls `buildExecutionProvider()` which references `this.planConverter` → gets `mockPC`
4. `runner.schemaProvider` is unaffected (no dependency on `planConverter`)

When `runner.withExecution(mockExec)` is called:

1. `runner.executionProvider` resolves to `mockExec` directly
2. All other providers are built normally

### Provider Dependency Graph

```
planConverter ──→ executionProvider
calciteContextFactory ──→ schemaProvider
calciteContextFactory ──→ sqlProvider
calciteContextFactory ──→ planConverter
calciteContextFactory ──→ executionProvider (Calcite variant)
jdbcContextFactory ──→ executionProvider (JDBC variant)
```

## 3. Provider Contract

The runner manages 6 providers:

| Provider | Interface | Purpose |
|----------|-----------|---------|
| `calciteContextFactory` | `CalciteContextFactory` | Creates Calcite `CalciteContext` used by schema, SQL, plan, and execution providers |
| `executionProvider` | `ExecutionProvider` | Executes Substrait plans, returns `VectorBlockIterator` |
| `schemaProvider` | `SchemaProvider` | Lists schemas and tables, returns protobuf `Schema` |
| `sqlProvider` | `SqlProvider` | Parses SQL to Substrait plans |
| `planConverter` | `PlanConverter` | Converts Substrait plans to Calcite `RelNode` or SQL |
| `securityProvider` | `SecurityProvider` | Provides principal name and authorities |

`securityProvider` has a default implementation (`NoneSecurityProvider`) that returns
`"ANONYMOUS"` with empty authorities. The other five are abstract and must be implemented
by each concrete runner.

## 4. Adding a New Provider

To add a new provider (e.g., `MetadataProvider`):

1. **Add override field** to `BackendContextRunner` constructor:
   ```kotlin
   private val metadataProviderOverride: MetadataProvider? = null
   ```

2. **Add template method**:
   ```kotlin
   protected abstract fun buildMetadataProvider(): MetadataProvider
   // or protected open fun ... for a default implementation
   ```

3. **Add lazy property**:
   ```kotlin
   val metadataProvider by lazy { metadataProviderOverride ?: buildMetadataProvider() }
   ```

4. **Add `withX()` method**:
   ```kotlin
   fun withMetadata(mp: MetadataProvider) = derive(metadataProviderOverride = mp)
   ```

5. **Update `derive()` signature** — add the new override parameter with default
   `= this.metadataProviderOverride`.

6. **Update all concrete subclasses** — implement `buildMetadataProvider()` and pass the
   new parameter through `derive()`.

## 5. Implementing a New Backend

To create a new concrete runner (e.g., `InMemoryBackendContextRunner`):

1. **Extend `BackendContextRunner`**, passing override parameters to the super constructor.

2. **Store infrastructure** as constructor parameters (e.g., data stores, connection factories).
   Expose any that tests need directly as `val` properties.

3. **Implement `buildX()` methods** using stored infrastructure. Reference `this.planConverter`
   (not a local variable) in `buildExecutionProvider()` to enable cascade.

4. **Implement `derive()`** — create a new instance of the same class, copying infrastructure
   but forwarding override parameters.

5. **Add companion factory methods** for convenient construction (e.g., `inMemoryContext(...)`).
   Annotate with `@JvmStatic` for Java interop.

## 6. Concrete Runners

### JdbcBackendContextRunner

**Infrastructure**: `defaultCalciteContextFactory`, `JdbcContextFactory`, `ExtensionCollection`,
`ExtensionCollector`, `SubstraitDispatcher`

**Exposed properties**: `calciteContextFactory` (via base class lazy property), `jdbcContextFactory`

**Build methods**:
- `buildCalciteContextFactory()` → returns the `defaultCalciteContextFactory` stored at construction
- `buildPlanConverter()` → `CalcitePlanConverter(this.calciteContextFactory, CALCITE dialect, extensionCollection)`
- `buildExecutionProvider()` → `JdbcExecutionProvider(this.planConverter, jdbcContextFactory)`
- `buildSchemaProvider()` → `CalciteSchemaProvider(this.calciteContextFactory, extensionCollector)`
- `buildSqlProvider()` → `CalciteSqlProvider(this.calciteContextFactory, substraitDispatcher)`

**Factory methods**: `jdbcBackendContext(dialect, url, driver, ...)`, `jdbcH2Context(url, targetSchema, ...)`

### CalciteBackendContextRunner

**Infrastructure**: `defaultCalciteContextFactory` (via `ConnectionContextFactory`),
`CalciteSqlDialectConventions`, `ExtensionCollection`, `ExtensionCollector`, `SubstraitDispatcher`

**Exposed properties**: `calciteContextFactory` (via base class lazy property)

**Build methods**:
- `buildCalciteContextFactory()` → returns the `defaultCalciteContextFactory` stored at construction
- `buildPlanConverter()` → `CalcitePlanConverter(this.calciteContextFactory, sqlDialectConventions.sqlDialect(), extensionCollection)`
- `buildExecutionProvider()` → `CalciteExecutionProvider(this.calciteContextFactory, this.planConverter)`
- `buildSchemaProvider()` → `CalciteSchemaProvider(this.calciteContextFactory, extensionCollector)`
- `buildSqlProvider()` → `CalciteSqlProvider(this.calciteContextFactory, substraitDispatcher)`

**Factory methods**: `calciteContext(modelPath, dialect?, conventionOverrides?)`

## 7. NoneSecurityProvider

Extracted from `SecurityDispatcherImpl` private inner class to public class at
`io.qpointz.mill.data.backend.NoneSecurityProvider`. Returns `"ANONYMOUS"` as principal
and empty authorities. Used as the default by `BackendContextRunner.buildSecurityProvider()`.
`SecurityDispatcherImpl` also uses it as fallback when no `SecurityProvider` bean is present.

## 8. Test Migration Inventory

### Migratable unit tests (mill-data-backends/src/test/)

| Test | Profile | Runner |
|------|---------|--------|
| `jdbc/BaseTest.java` | test-jdbc | `JdbcBackendContextRunner` |
| `jdbc/providers/JdbcSchemaProviderTest.java` | test-jdbc | `JdbcBackendContextRunner` |
| `jdbc/providers/JdbcExecutionProviderTest.java` | test-jdbc | `JdbcBackendContextRunner` |
| `jdbc/providers/JdbcCalciteContextFactoryTest.java` | test-jdbc | `JdbcBackendContextRunner` |
| `jdbc/MultiSchemaBackendTests.java` | (none) | `JdbcBackendContextRunner` |
| `calcite/BaseTest.java` | test-calcite | `CalciteBackendContextRunner` |
| `calcite/SchemaPlusSchemaProviderTest.java` | test-calcite | `CalciteBackendContextRunner` |
| `calcite/providers/CalciteExecutionProviderTest.java` | test-calcite | `CalciteBackendContextRunner` |
| `calcite/providers/CalciteSchemaProviderTest.java` | test-calcite | `CalciteBackendContextRunner` |
| `calcite/providers/CalcitePlanConverterTest.java` | test-calcite | `CalciteBackendContextRunner` |

### Staying as SpringBootTest

| Test | Profile | Reason |
|------|---------|--------|
| `testIT/jdbc/JdbcBackendExecutionTestIT.java` | test-moneta-it | Integration test |
| `testIT/jdbc/JdbcConnectionProviderTestIT.java` | test-moneta-it | Integration test with spy |
| `mill-data-autoconfigure/src/test/` (all) | various | Tests service-layer composition |
| `mill-data-grpc-service/src/test/` (all) | test | Tests gRPC wiring |
| `mill-data-http-service/src/test/` (all) | test-cmart | Tests HTTP controllers |

### YAML configs deletable after unit test migration

- `mill-data-backends/src/test/resources/application-test-jdbc.yaml`
- `mill-data-backends/src/test/resources/application-test-jdbc-multi-schema.yaml`
- `mill-data-backends/src/test/resources/application-test-calcite.yml`

## 9. Usage Examples

### Creating a runner

```java
// JDBC with H2
var ctx = JdbcBackendContextRunner.jdbcH2Context(
    "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM './data.sql'", "ts");

// Calcite with model file
var ctx = CalciteBackendContextRunner.calciteContext("./config/model.yaml");
```

### Swapping a provider with a mock

```java
@Mock PlanConverter mockPc;

@Test void testWithMockPlanConverter() {
    when(mockPc.toSql(any())).thenReturn(new ConvertedPlanSql("SELECT 1", List.of()));
    ctx.withPlanConverter(mockPc).run(runner -> {
        var result = runner.getExecutionProvider().execute(plan, config);
        assertTrue(result.hasNext());
    });
}
```

### Cascade behavior in practice

```java
// Override planConverter → executionProvider is rebuilt using the mock
var mutated = ctx.withPlanConverter(mockPc);
// mutated.getExecutionProvider() uses mockPc internally
// mutated.getSchemaProvider() is unaffected — built from real infrastructure
```

---
*Created: 2026-02-22*
