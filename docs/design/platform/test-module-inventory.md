# Test Module Inventory - Post-Refactoring Analysis

This document identifies tests that may be in wrong modules or have issues after refactoring where classes were moved between modules.

## 1. Completely Commented-Out Tests (Disabled)

These test files have their entire content commented out, suggesting they were disabled after classes were moved or dependencies changed.

### AI Module (`ai/mill-ai-core`)

| File | Reason |
|------|--------|
| `src/test/java/io/qpointz/mill/ai/nlsql/components/QueryExecutorTest.java` | Depends on `DataOperationDispatcher` from service-core |
| `src/test/java/io/qpointz/mill/ai/nlsql/components/tasks/ExecQueryTaskTest.java` | Depends on `MetadataProvider` from service-core |
| `src/testIT/java/.../tasks/GetDataTaskTestIT.java` | Depends on `DataOperationDispatcher`, `MetadataProvider` |
| `src/testIT/java/.../tasks/ReasonTaskTestIT.java` | Commented out integration test |
| `src/testIT/java/.../ChatTaskWorkflowTestIT.java` | Depends on `DataOperationDispatcher`, `MetadataProvider` |

### JDBC Driver Module (`clients/mill-jdbc-driver`)

| File | Reason |
|------|--------|
| `src/testIT/java/io/qpointz/mill/BaseTest.java` | Base test class - disabled |
| `src/testIT/java/io/qpointz/mill/BaseDriverTestIT.java` | Disabled driver test |
| `src/testIT/java/io/qpointz/mill/DriverTestIT.java` | Disabled driver test |
| `src/testIT/java/io/qpointz/mill/MillDatabaseMetadataTest.java` | Disabled metadata test |
| `src/testIT/java/io/qpointz/mill/JetGrpcDriverTestIT.java` | Disabled gRPC test |
| `src/testIT/java/io/qpointz/mill/JetHttpDriverTestIT.java` | Disabled HTTP test |

## 2. Security Class in Service-Core (Architectural Question)

| Test | Class Tested | Current Module | Notes |
|------|--------------|----------------|-------|
| `core/mill-service-core/src/test/java/io/qpointz/mill/security/authorization/policy/GrantedAuthoritiesPolicySelectorTest.java` | `GrantedAuthoritiesPolicySelector` | `mill-service-core` | Implements `PolicySelector` (security-core) but depends on `SecurityDispatcher` (service-core) |

The class is in `io.qpointz.mill.security.*` package but lives in `mill-service-core` because it depends on `SecurityDispatcher`. This is intentional but creates a split where security policy code exists in two modules.

## 3. Cross-Module Test Dependencies (Expected but Notable)

These tests correctly test integration points but have significant cross-module dependencies:

| Test Module | Imports From |
|-------------|-------------|
| `ai/mill-ai-core` tests | `mill-service-core` (MetadataProvider, DataOperationDispatcher) |
| `services/mill-metadata-service` tests | `mill-metadata-core` (MetadataEntity, MetadataService) |
| `core/mill-service-core` tests | `mill-metadata-core` (MetadataEntity, various facets) |

## Recommendations

### Commented-out Tests
Review and either:
- **Delete** if obsolete
- **Move** to appropriate modules if classes moved
- **Fix dependencies** and re-enable

### GrantedAuthoritiesPolicySelector
Consider whether this class should move to `mill-security-core` with a new abstraction to avoid depending on `SecurityDispatcher` directly.

### Integration Test Infrastructure
The JDBC driver integration tests seem systematically disabled - this may need a test infrastructure fix rather than module reorganization.

---
*Generated: 2026-02-05*
