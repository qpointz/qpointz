# Milestones

## 0.0.6

**Target date:** End of March 2026

### Completed

Items delivered since `origin/main` on the current development branch.

#### Refactoring

| # | Item | Type | Status |
|---|------|------|--------|
| R-1 | Move services/mill-metadata-service to metadata/mill-metadata-service | refactoring | done |
| R-2 | Remove Spring contamination from mill-metadata-core | refactoring | done |
| R-3 | Remove Spring contamination from mill-data-backends | refactoring | done |
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
| WI-003 | Data policy format redesign | refactoring / feature | done |
| WI-004 | Public documentation tone and formatting cleanup | docs | done |
| WI-005 | Eliminate legacy metadata provider module | refactoring | done |
| WI-006 | Facet type catalog with validation and REST API | feature | done |
| WI-007 | Relocate SchemaExplorerController to data HTTP service | refactoring | done |
| WI-008 | Migrate metadata modules from Java/Lombok to Kotlin | refactoring | done |
| WI-009 | Spring test configuration cleanup (narrow @ComponentScan, @EnableAutoConfiguration exclusions, delete MainLala) | refactoring | done |
| WI-010 | Clean Spring / pure module separation (SecurityProvider relocation, dependency cleanup, AI config extraction, module inventory) | refactoring | done |
| WI-011 | Arrow format support (module naming alignment, Arrow IPC reader/writer, docs) | feature | done |
| WI-012 | JDBC async API + D-7 BOOL mapping + R-5 target-schema key alignment | feature / fix | done |

#### Source — Data Source Framework

| # | Item | Type | Status |
|---|------|------|--------|
| S-11 | Implement flow backend with SourceDefinitionRepository abstraction | feature | done |
| S-12 | Implement SingleFileSourceRepository and MultiFileSourceRepository | feature | done |
| S-13 | Implement FlowContextFactory (CalciteContextFactory for source descriptors) | feature | done |
| S-14 | Implement FlowBackendContextRunner in testkit | test | done |
| S-15 | Implement FlowBackendAutoConfiguration (Spring auto-configuration for flow backend) | feature | done |
| S-16 | Add Arrow format module in data/formats as phase 1 before Flight/Flight SQL (schema + type mapping + source integration) | feature | done |

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items targeted next after in-progress work is completed.
