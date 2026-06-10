# Substrait to Calcite RelNode — Internal IR Migration

**Status:** Planned
**Scope:** Execution pipeline across mill-data-service, mill-data-backends, mill-data-autoconfigure, mill-core

---

## Motivation

1. **Wasteful roundtrip**: The SQL execution path currently does SQL -> Calcite -> Substrait -> Calcite -> execute. Substrait adds no value as an internal IR when Calcite is both the parser and the executor.

2. **SQL parameter support unlocked**: The proto defines `SQLStatement` with `repeated Parameter parameters`, but Substrait Plan has no native representation for bind parameters. As a result, the entire service layer drops parameters — `SqlProvider.parseSql(String sql)` takes a bare string, `DataOperationDispatcherImpl` calls `getStatement().getSql()` and discards the parameter list. With Calcite RelNode as the IR, parameterized queries become possible via `RexDynamicParam` and `PreparedStatement` parameter binding at the execution layer.

3. **Independent SQL dialect and operator table management**: Today, Substrait's `SimpleExtension.ExtensionCollection` is threaded through the entire execution pipeline — `SubstraitDispatcher`, `PlanConverter`, `CalciteSqlProvider`, `TableFacetVisitor`, and both backend configurations all depend on it. Every SQL dialect function or operator must be representable in Substrait's extension catalog, coupling dialect evolution to Substrait's schema. With Substrait at the edges only, Calcite's operator table (which natively supports dialect-specific functions via `SqlOperatorTable`, `SqlLibraryOperator`, etc.) governs execution, and Substrait extensions are maintained independently for the Substrait input/ParseSql conversion paths.

4. **Smaller generated proto footprint**: The `proto/substrait/` directory (9 proto files: algebra, plan, type, function, capabilities, extended_expression, parameterized_types, type_expressions, extensions) generates a large number of stubs in every client. For example, mill-py ships 27 generated Python files under `_proto/substrait/` — none of which are used by any client today (all clients send `SQLStatement`, not `substrait.Plan`). On the Java side, `io.substrait:core` pulls in the full Substrait class hierarchy. Once Substrait is an edge-only format, the proto surface exposed to clients can be trimmed — `data_connect_svc.proto` and `statement.proto` can drop the `substrait/plan.proto` import and remove the `substrait.Plan` fields if Substrait input is deprecated from the client-facing API in the future.

5. **Cancelable results and resource lifecycle control**: Today there is no way to cancel an in-progress query or clean up resources. `VectorBlockIterator` extends bare `Iterator<VectorBlock>` — no `close()`, no cancellation. `ResultSetVectorBlockIterator` wraps a JDBC `ResultSet` but never closes it or its parent `Statement`/`Connection`. `ResultAllocatorImpl` caches iterators with a 10-minute TTL but has no eviction listener to release underlying resources. The gRPC streaming handler (`MillGrpcService.streamResult()`) casts to `ServerCallStreamObserver` but never registers an `onCancelHandler` — when a client cancels the stream, the server keeps iterating and JDBC resources are never released. Since we are already changing the `ExecutionProvider` return type in this migration, we can introduce a closeable/cancelable result wrapper that properly cleans up resources when clients cancel, disconnect, or abandon paged results.

6. **Reduced complexity**: Removing Substrait from the internal pipeline eliminates `SubstraitDispatcher`, `PlanHelper`, `DataTypeToSubstrait`, `LogicalFunctionHelper`, and the Substrait-based rewriter visitor from the critical execution path. Substrait stays only as an optional input format.

---

## Target Architecture

Calcite `RelNode` is the internal IR. Substrait and SQL are both input query languages:

- **SQL input**: SQL -> Calcite parse -> `RelNode` -> rewrite -> execute
- **Substrait input**: `substrait.Plan` -> `SubstraitRelNodeConverter` -> `RelNode` -> rewrite -> execute
- **ParseSql RPC** (kept): SQL -> Calcite parse -> Substrait conversion -> return `substrait.Plan`

No proto changes. `QueryRequest` keeps both `substrait.Plan` and `SQLStatement` in the oneof — they are two supported input formats.

---

## Phasing Strategy

Each phase is a self-contained branch/PR. The old Substrait path keeps working until explicitly replaced. No interfaces are changed until their new replacement is proven. Implementation within each phase follows a bottom-up order: execution providers first, then SQL parsing, then dispatcher wiring.

---

## Phase 1 — SQL-Direct Execution Path

**Goal**: SQL queries execute via SQL -> Calcite RelNode -> Execute, bypassing Substrait entirely. Old Substrait path remains untouched for Substrait input and ParseSql RPC.

### Step 1.1: Create `QueryPlan` record

New file: `data/mill-data-service/.../services/QueryPlan.java`

```java
public record QueryPlan(RelNode relNode, List<String> outputNames, List<Parameter> parameters) {}
```

### Step 1.2: Introduce cancelable `QueryResult` wrapper

Replace `VectorBlockIterator` (bare `Iterator<VectorBlock>`) as the execution return type with a new `QueryResult` that implements `AutoCloseable`:

```java
public interface QueryResult extends VectorBlockIterator, AutoCloseable {
    void cancel();
    @Override void close();
}
```

`ResultSetVectorBlockIterator` (or a wrapper around it) implements `QueryResult.close()` to release the underlying `ResultSet`, `Statement`, and `Connection`. Consumers of `QueryResult`:
- `ResultAllocatorImpl`: add a cache eviction listener that calls `close()` on abandoned paged results
- `MillGrpcService.streamResult()`: register `callObserver.setOnCancelHandler(() -> result.cancel())` so client-initiated stream cancellation stops the query and releases resources
- `MillGrpcService.execQuery()`: call `result.close()` after the stream completes normally

### Step 1.3: ExecutionProvider — add RelNode-based execution (bottom layer)

Add `execute(QueryPlan, config)` as a new default method on `ExecutionProvider` returning `QueryResult`. Old `execute(Plan, config)` stays — nothing breaks.

Implement the new method in:
- `CalciteExecutionProvider`: take `plan.relNode()`, run via `RelRunner.prepareStatement()`, bind parameters if present, return `QueryResult` that closes resources on `close()`
- `JdbcExecutionProvider`: take `plan.relNode()`, call `PlanConverter.toSql(RelNode)`, create `PreparedStatement`, bind parameters, execute, return `QueryResult` that closes the JDBC connection on `close()`

### Step 1.4: SqlProvider — produce RelNode from SQL, with parameters (middle layer)

Add `parseSqlDirect(SQLStatement statement)` as a new default method on `SqlProvider` that accepts the full `SQLStatement` (SQL text + parameters). Old `parseSql(String sql)` stays for ParseSql RPC.

Add `QueryPlanParseResult` — same shape as `PlanParseResult` but holds `QueryPlan`.

Implement in `CalciteSqlProvider`: stop at `planner.rel(validated)`, wrap the `RelRoot` in `QueryPlan`, pass through parameters.

### Step 1.5: DataOperationDispatcherImpl — connect SQL path (top layer)

In `execute(QueryRequest)`, change only the `request.hasStatement()` branch to use `sqlProvider.parseSqlDirect(request.getStatement())` and `executionProvider.execute(queryPlan, config)`.

The `parseSql()` method (for ParseSql RPC) remains untouched.

### Step 1.6: Config wiring and tests

Minimal wiring changes — new methods are defaults on existing interfaces. Update/add tests for SQL-direct execution.

**End state**: SQL execution bypasses Substrait. Substrait input, ParseSql RPC, and rewriters still work on old path.

---

## Phase 2 — Substrait Input via RelNode

**Goal**: Substrait input queries also go through `QueryPlan` (RelNode) for execution. Remove old `execute(Plan, config)` from `ExecutionProvider`.

### Step 2.1: Route Substrait path through QueryPlan in dispatcher

In `DataOperationDispatcherImpl.execute(QueryRequest)`, change the `hasPlan()` branch to use `PlanConverter.toRelNode(substraitDispatcher.protoToPlan(proto))` -> `QueryPlan`. Needs `PlanConverter` injected into dispatcher.

### Step 2.2: Remove old `execute(Plan, config)` from ExecutionProvider

Remove from interface and both implementations. Remove `PlanConverter` from `CalciteExecutionProvider`.

### Step 2.3: Update config and tests

Wire `PlanConverter` into `DataOperationDispatcherImpl` via `DefaultServiceConfiguration`. Update gRPC tests that use `QueryRequest.setPlan()`.

**End state**: Both SQL and Substrait inputs flow through `QueryPlan`. `ExecutionProvider` no longer knows about Substrait. Rewriters temporarily skipped.

---

## Phase 3 — Port Rewriters to Calcite

**Goal**: Rewrite the security facet system to operate on Calcite `RelNode` / `RexNode` instead of Substrait types. Re-enable rewriters.

### Step 3.1: Change `PlanRewriter` and `PlanRewriteChain` to `QueryPlan`

### Step 3.2: Change `RecordFacet` and `TableFacet` to Calcite `RexNode`

Replace `io.substrait.expression.Expression` with `org.apache.calcite.rex.RexNode`.

### Step 3.3: Change `SqlProvider.parseSqlExpression()` to return `RexNode`

In `CalciteSqlProvider.parseSqlExpression()`: stop at the `RexNode` instead of converting through `RexExpressionConverter`. Remove Substrait visitor/converter code.

### Step 3.4: Port `TableFacetVisitor` from Substrait to Calcite

- Change from `RelCopyOnWriteVisitor` (Substrait) to `RelShuttleImpl` (Calcite)
- Override `visit(TableScan)` instead of `visit(NamedScan)`
- Use `RelBuilder.push(tableScan).filter(rexNode).build()` instead of `SubstraitBuilder.filter()`

### Step 3.5: Port `TableFacetPlanRewriter` and `TableFacetFactoryImpl`

Remove `SubstraitDispatcher` dependency. Use Calcite `RexBuilder` for combining expressions.

### Step 3.6: Re-enable rewriters in dispatcher

Re-add the rewrite step on `QueryPlan` in `DataOperationDispatcherImpl.execute()`.

### Step 3.7: Update config wiring and tests

Remove `SubstraitDispatcher` from `TableFacetFactory` and `TableFacetPlanRewriter` beans in `DefaultFilterChainConfiguration`.

**End state**: Full pipeline works on RelNode. Rewriters apply to both SQL and Substrait inputs.

---

## Phase 4 — Clean Up Dead Substrait Code

**Goal**: Remove Substrait from core interfaces and unused internal code.

### Step 4.1: Clean `SqlProvider`

- Remove old `parseSql()` method, rename `parseSqlDirect()` to `parseSql()`
- Remove old `PlanParseResult`, keep `QueryPlanParseResult`

### Step 4.2: Clean `CalciteSqlProvider`

- Remove `substraitDispatcher` field
- Move Substrait conversion (for ParseSql RPC) to a separate helper or into the dispatcher

### Step 4.3: Remove unused Substrait internals

- Delete: `PlanHelper`, `PlanDispatcher`, `PlanDispatcherImpl`
- Delete: `DataTypeToSubstrait`, `LogicalFunctionHelper` (mill-core)
- Remove `api(libs.substrait.core)` from `core/mill-core/build.gradle.kts` if no remaining consumers
- Remove `PlanDispatcher` bean from `DefaultServiceConfiguration`
- Remove `SubstraitDispatcher` from `ServiceHandler` if not needed externally

### Step 4.4: Scope remaining Substrait to edges

After cleanup, Substrait only lives in:
- `SubstraitDispatcher` — Substrait input and ParseSql RPC
- `PlanConverter.toRelNode(Plan)` — Substrait-to-RelNode conversion
- `SubstraitRelVisitor` — RelNode-to-Substrait for ParseSql RPC
- `mill-data-backends` Gradle dependency on `substrait-isthmus`
