# Infrastructure and Cross-Cutting

Design documents for platform-level concerns that span multiple domains or don't belong
to a single product area.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Spring framework migrations (Boot upgrades, WebFlux migration)
- Configuration schema and configuration key documentation
- Codebase-wide analysis (metrics, structure, tech stack)
- Protocol specifications (MCP data provider, gRPC data export service)
- SQL dialect and Calcite operator analysis (platform-level, not client-specific)
- Cross-cutting infrastructure that affects multiple modules

## Does NOT Belong Here

- Build architecture and CI maintenance runbooks → `build-system/`
- Release flows, Maven publishing, documentation generation → `publish/`
- Codebase refactoring iterations, file inventories, progress tracking → `refactoring/`
- Type system and vector encoding → `data/`
- Test inventories created as refactoring artifacts → `refactoring/`
- Client-specific SQL dialect descriptors → `client/`

## Documents

| File | Description |
|------|-------------|
| `calcite-dialect-comparison.md` | Calcite dialect functions comparison: PostgreSQL vs SqlLibraryOperators |
| `arrow-flight-server-design.md` | Arrow Flight server design for Mill query transport and type mapping |
| `arrow-flight-sql-server-design.md` | Arrow Flight SQL server design for SQL metadata and command compatibility |
| `CODEBASE_ANALYSIS_CURRENT.md` | High-level codebase analysis: metrics, structure, tech stack, status |
| `CONFIGURATION_INVENTORY.md` | Spring configuration inventory: config files, @ConfigurationProperties, consumers |
| `data-export-service.md` | gRPC data provider interface: contract decoupling query engine from data sources |
| `mcp.md` | MCP data provider specification: common objects, API contract |
| `mill-configuration.md` | Mill configuration schema: keys from application*.yml grouped by area |
| `spring4-migration-plan.md` | Spring Boot 3.5 to 4.0 migration plan |
| `substrait-to-relnode-migration.md` | Gradual migration from Substrait Plan to Calcite RelNode as internal IR |
| `webflux-migration-plan.md` | WebFlux reactive migration plan for selected controllers |
