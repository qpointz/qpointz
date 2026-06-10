# Release 0.6.0

Release date: 2026-02-27  
Compare: [v0.5.0...v0.6.0](https://gitlab.qpointz.io/qpointz/qpointz/compare/v0.5.0...v0.6.0)

## Highlights

This release completes the 0.0.6 milestone wave and finalizes key delivery tracks across
data backends, metadata, and source framework foundations. It includes JDBC alignment
fixes, flow backend rollout, Arrow IPC support, and multiple milestone work item closures.

## Bug Fixes

- declare metadata dependency in ai-v1 core ([d21b23d](https://gitlab.qpointz.io/qpointz/qpointz/commit/d21b23de760bf8cd483f97e9c0b43e6151adf316))
- implement WI-012 JDBC bool/async/config alignment ([45863e7](https://gitlab.qpointz.io/qpointz/qpointz/commit/45863e77445640d8289d8fa898ce17f89a7696b3))
- JDBC HTTP client does not return schema on empty results ([029c1e7](https://gitlab.qpointz.io/qpointz/qpointz/commit/029c1e703de6114605f51107c0bb6d54b442a66a))

## Features

- add `Record`, `RecordSchema`, `SchemaField` data classes (WI 1.1) ([ccb30c0](https://gitlab.qpointz.io/qpointz/qpointz/commit/ccb30c0cfa6c1d1cf39a47942de83fe0b4241dbf))
- add `RecordSource` interfaces and in-memory implementations (WI 1.2) ([57c3c71](https://gitlab.qpointz.io/qpointz/qpointz/commit/57c3c71e6c70ee0997649723f637659e6bbce7de))
- implement Arrow IPC format module and wire renamed format projects ([7db6e48](https://gitlab.qpointz.io/qpointz/qpointz/commit/7db6e48c79e19ee20bbb557d4cb22b1bd8c80feb))
- implement flow backend, autoconfiguration, tests, and documentation ([563285a](https://gitlab.qpointz.io/qpointz/qpointz/commit/563285a71e740ac4ff0518168f770ac870f7feee))
- JDBC backend allows custom JDBC connection setup via `JdbcConnectionProvider` ([d606ab7](https://gitlab.qpointz.io/qpointz/qpointz/commit/d606ab7e7d84889654bdf87c545fdba3778b043c))
- redesign data policy format with expression AST, wildcard matching, and import/export (WI-003) ([8c03e81](https://gitlab.qpointz.io/qpointz/qpointz/commit/8c03e81243e9b7c3be6e7d049a869c7ac4310298))
- scaffold `source/` module structure for `mill-source-provider` ([043b9c7](https://gitlab.qpointz.io/qpointz/qpointz/commit/043b9c74cae4d9a8cea510ace08aab220fa9963a))
- WI-006: implement facet type catalog with validation and REST API ([d39cf9b](https://gitlab.qpointz.io/qpointz/qpointz/commit/d39cf9b813b79e54e3a70979778faa14a94a1add))

## Milestone 0.0.6 Completed Scope (Consolidated)

### Refactoring and Test Infrastructure

- R-1, R-2, R-3: metadata/data module decoupling and Spring contamination cleanup
- R-15 to R-21: backend context test runner adoption, test migration, module renames, and service relocation

### Closed Work Items

- WI-001 to WI-012 completed, including:
  - WI-003 data policy redesign
  - WI-006 facet type catalog with validation and REST API
  - WI-011 Arrow format support
  - WI-012 JDBC async API plus BOOL and schema-key alignment fixes

### Source Framework Deliverables

- S-11 to S-16 completed:
  - flow backend and repository abstractions
  - context factory and testkit runner
  - Spring auto-configuration for flow backend
  - Arrow format module introduction
