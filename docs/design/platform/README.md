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
| `mill-service-editions-and-ai-chat.md` | `apps/mill-service` Gradle feature **`ai-chat-service`** and edition **`ai`** wiring **`mill-ai-v3-autoconfigure`** (starter; pulls **`mill-ai-v3-service`**) + persistence |
| `calcite-dialect-comparison.md` | Calcite dialect functions comparison: PostgreSQL vs SqlLibraryOperators |
| `mill-data-lane-onepager.md` | One-page visual architecture summary of Mill data flow: sources, backends, SQL engine, services, metadata, and clients |
| `query-result-execution-service.md` | Query result **sessions** over `DataOperationDispatcher`: HTTP `/api/v1/query/**`, paging (`pageIndex`/`pageSize`), marshallers, tenant ownership, `epoch`/replace, Skymill `testIT` — maintainer/implementer reference for UI, service, and embedders |
| `arrow-flight-server-design.md` | Arrow Flight server design for Mill query transport and type mapping |
| `arrow-flight-sql-server-design.md` | Arrow Flight SQL server design for SQL metadata and command compatibility |
| `odata-service.md` | OData v4 read service: EDM from physical schema + facets, `$filter`→`RexNode`, RelNode compose + Rel→Substrait adapter, dispatcher execution |
| [`odata-service/COLDSTART.md`](../../workitems/completed/20260623-odata-service/COLDSTART.md) | Implementer cold start: adapter pipeline, module skeletons, Skymill gate tests, verify commands |
| `CODEBASE_ANALYSIS_CURRENT.md` | High-level codebase analysis: metrics, structure, tech stack, status |
| `CONFIGURATION_INVENTORY.md` | Spring configuration inventory: config files, @ConfigurationProperties, consumers |
| `data-export-service.md` | gRPC data provider interface: contract decoupling query engine from data sources |
| `mcp.md` | MCP data provider specification: common objects, API contract |
| `mill-configuration.md` | Mill configuration schema: keys from application*.yml grouped by area; links **observability action** doc for value-mapping metrics gap |
| `cloud-resource-loading.md` | Spring `ResourceLoader` / `ProtocolResolver` for flow `sources` and metadata `seed.resources` (`s3://`, `gs://`, `azure-blob://`); servlet-safe loader composition |
| `local-dev-authentik.md` | Local **Authentik** OIDC IdP in `deploy/local-dev`: compose (server + worker), Postgres DB, blueprints under `/blueprints/custom`, OIDC URLs for Spring Boot, HTTP vs HTTPS; links to public **[Authentik (OIDC)](../../public/src/security/authentik-oidc.md)** operator guide and **[OAuth2 integration](../security/oauth2-oidc-mill-authentik.md)** |
| `general-event-bus.md` | **Implemented (foundation)** Mill-wide event bus: layered ports, two-axis publish/process, multicast subscriptions, transport scale path; **WI-311**–**WI-314** |
| `event-bus-follow-ons.md` | Deferred stories: domain producers, side consumers, global search API, distributed transport |
| [`general-event-bus/COLDSTART.md`](../../workitems/completed/20260619-general-event-bus/COLDSTART.md) | Implementer cold start: Gradle, packages, class map, testIT, verify commands |
| `spring4-migration-plan.md` | Spring Boot 3.5 to 4.0 migration plan |
| `substrait-to-relnode-migration.md` | Gradual migration from Substrait Plan to Calcite RelNode as internal IR |
| `webflux-migration-plan.md` | WebFlux reactive migration plan for selected controllers |
