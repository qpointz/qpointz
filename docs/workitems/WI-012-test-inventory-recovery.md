# WI-012: JDBC Async API + D-7 BOOL + R-5 Config Key Alignment

**Type:** feature / fix
**Priority:** high
**Status:** completed
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `feat/wi-012-jdbc-async-and-bool`

---

## Goal

Implement two JDBC-focused deliverables:

1. `D-7` fix: map `BOOL` to `java.sql.Types.BOOLEAN` (not `BLOB`).
2. Add an async JDBC client API surface for non-blocking usage patterns.
3. Resolve `R-5`: unify JDBC config key usage on `target-schema` and remove
   active `output-schema` ambiguity.

Python-only client capabilities are explicitly not targets of this work item.

---

## Scope

- Fix `D-7`: correct JDBC BOOL mapping in driver/core JDBC type conversion path.
- Add/adjust JDBC tests to lock BOOL mapping behavior.
- Design and implement JDBC async API entry points (without breaking existing
  synchronous JDBC behavior).
- Add tests for async API behavior (success, error propagation, cancellation
  semantics where applicable).
- Resolve `output-schema` vs `target-schema` inconsistency across active
  configs and bindings; define one canonical key (`target-schema`).

---

## Initial Parity Matrix (Baseline)

Current snapshot of JDBC driver parity versus `mill-py` for scope decisions:

| Capability Area                    | JDBC Driver                                               | `mill-py`                                      | Parity                    |
| ---------------------------------- | --------------------------------------------------------- | ---------------------------------------------- | ------------------------- |
| Transport protocols                | gRPC + HTTP/HTTPS                                         | gRPC + HTTP/HTTPS                              | parity                    |
| Authentication                     | Basic + Bearer (config-driven)                            | Basic + Bearer (API auth objects)              | parity                    |
| TLS support                        | TLS/mTLS on gRPC; HTTP(S) channel support                 | TLS/mTLS supported                             | partial                   |
| Handshake/ListSchemas/GetSchema    | supported                                                 | supported                                      | parity                    |
| SQL query execution                | supported                                                 | supported                                      | parity                    |
| Async client API                   | not available                                             | available (`mill.aio`)                         | missing (**selected**)    |
| DataFrame/Arrow conversion helpers | not available (JDBC result-set only)                      | Arrow/pandas/polars extras                     | missing                   |
| SQL parsing helper exposure        | not exposed via JDBC API                                  | `parse_sql()` available                        | missing                   |
| Metadata discovery surface         | JDBC `DatabaseMetaData` with basic tables/schemas/columns | typed schema/table/field model                 | partial                   |
| Prepared parameters/bind variables | not supported (`Parameters not supported`)                | no DBAPI prepared-style API; query string path | partial                   |
| Type mapping correctness checks    | has JDBC mapping utility (`JdbcUtils`)                    | strong Python/PyArrow type tests               | partial                   |
| BOOL mapping behavior              | currently incorrect (`BOOL -> BLOB`)                      | bool mapped as bool                            | missing (**selected / D-7**) |
| Error model quality                | SQL exceptions via driver, limited taxonomy               | explicit Python exception classes              | partial                   |

Notes:
- WI-012 scope from this matrix is limited to:
  - Async client API
  - BOOL mapping fix (`D-7`)
  - Config key alignment (`R-5`: `output-schema` -> `target-schema`)
- Remaining gaps are follow-up backlog work and are out of scope for WI-012.

---

## Out of Scope

- Complex type feature implementation (`LIST/MAP/OBJECT`) itself.
- Timezone semantic protocol changes (`P-29` / `P-30`).
- Full JDBC test framework rearchitecture.
- Python-specific feature parity work (DataFrame helpers, Python async model,
  and Python-only ergonomics).

---

## Implementation Plan

1. **Implement `D-7` BOOL mapping fix**
   - Identify JDBC mapping location(s) currently mapping BOOL incorrectly.
   - Change mapping to `Types.BOOLEAN`.
   - Verify no regressions in adjacent type mappings.

2. **Add focused test coverage for `D-7`**
   - Unit tests for type mapper behavior (`BOOL -> BOOLEAN`).
   - Integration-level assertion through metadata/introspection path where
     applicable.
   - Negative regression check ensuring BOOL is never reported as BLOB.

3. **Implement JDBC async API**
   - Define async surface compatible with existing client/transport contracts.
   - Ensure error propagation mirrors sync semantics where possible.
   - Define cancellation/timeout behavior clearly.

4. **Add async API tests**
   - Success path: async handshake/schema/query calls.
   - Failure path: transport/query errors propagate predictably.
   - Concurrency sanity checks for parallel async requests.

5. **Resolve `R-5` config key inconsistency**
   - Inventory active runtime/test/deploy configs still using `output-schema`.
   - Migrate active files to `target-schema`.
   - If backward compatibility is needed, implement explicit alias handling
     with deprecation note (temporary).
   - Update docs and samples to show only canonical key.

6. **Validation and reporting**
   - Run targeted JDBC test suites and required module checks.
   - Summarize `D-7`, async API, and `R-5` evidence in PR notes.

---

## Deliverables

- Implemented `D-7` fix in JDBC mapping path.
- Test coverage proving BOOL maps to JDBC BOOLEAN.
- Implemented JDBC async API surface with tests.
- `R-5` completion: active config files standardized on `target-schema`.
- Documentation/update notes for async API usage and constraints.

---

## Verification

1. BOOL is mapped to `java.sql.Types.BOOLEAN` in JDBC-facing conversion.
2. BOOL no longer maps to BLOB in any tested path.
3. Relevant JDBC unit/integration tests pass.
4. Async JDBC API tests pass for success and failure scenarios.
5. Active runtime/test/deploy configs use canonical `target-schema`.
6. Existing synchronous JDBC behavior remains compatible.

## Estimated Effort

Medium. `D-7` is targeted; async API and `R-5` config cleanup add moderate
design and verification effort.
