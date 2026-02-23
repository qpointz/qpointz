# Milestones

## 0.0.6

**Target date:** End of March 2026

### Completed

Items delivered since `origin/main` on the current development branch.

#### Refactoring

| # | Item | Type | Status |
|---|------|------|--------|
| R-15 | Implement BackendContextRunner test rig (abstract class + JdbcBackendContextRunner + CalciteBackendContextRunner) | refactoring | done |
| R-16 | Migrate mill-data-backends unit tests from @SpringBootTest to BackendContextRunner (10 test files) | test | done |
| R-17 | Delete obsolete test YAML configs after unit test migration (3 files in mill-data-backends/src/test/resources/) | refactoring | done |
| R-18 | Rename mill-data-service to mill-data-backend-core and update all Gradle references | refactoring | done |
| R-19 | Move mill-data-grpc-service and mill-data-http-service under data/services/ subfolder | refactoring | done |
| R-20 | Move non-autoconfigure tests from mill-data-autoconfigure to backend-core and metadata modules | test | done |
| R-21 | Delete orphaned application-*.yml Spring config files across modules | refactoring | done |

#### Work Items

| # | Item | Type | Status |
|---|------|------|--------|
| WI-001 | Fix package rename leakage outside data backend modules | refactoring | done |
| WI-002 | Split security into service authentication and data authorization | refactoring | done |
| WI-004 | Public documentation tone and formatting cleanup | docs | done |
| WI-003 | Data policy format redesign | refactoring / feature | done |
| WI-005 | Eliminate legacy metadata provider module | refactoring | done |
| WI-006 | Facet type catalog with validation and REST API | feature | done |

### Planned

Items targeted for delivery in this milestone.

#### Source — Data Source Framework

| # | Item | Type | Status | Source |
|---|------|------|--------|--------|
| S-11 | Implement flow backend with SourceDefinitionRepository abstraction | feature | planned | source/flow-backend.md |
| S-12 | Implement SingleFileSourceRepository and MultiFileSourceRepository | feature | planned | source/flow-backend.md |
| S-13 | Implement FlowContextFactory (CalciteContextFactory for source descriptors) | feature | planned | source/flow-backend.md |
| S-14 | Implement FlowBackendContextRunner in testkit | test | planned | source/flow-backend.md |
| S-15 | Implement FlowBackendAutoConfiguration (Spring auto-configuration for flow backend) | feature | planned | source/flow-backend.md |
