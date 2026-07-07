package io.qpointz.mill.ai.mcp

import io.qpointz.mill.ai.capabilities.DemoCapabilityProvider
import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlQueryExecutionPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionException
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryResultMode
import io.qpointz.mill.ai.capabilities.sqlquery.mockSqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.profile.PlatformProfiles
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CapabilityMcpExecutorTest {

    private val registry = CapabilityRegistry.from(listOf(DemoCapabilityProvider()))
    private val context = AgentContext(
        contextType = "general",
        capabilityDependencies = CapabilityDependencyContainer.empty(),
    )

    private val catalogPort = object : SchemaCatalogPort {
        override fun listSchemas(): List<ListSchemasItem> = emptyList()
        override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()
        override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> = emptyList()
        override fun listRelations(schemaName: String, tableName: String, direction: RelationDirection): List<ListRelationsItem> =
            emptyList()
    }
    private val dialect = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")

    private fun dataAnalysisContext(execution: MockSqlQueryExecutionPort = MockSqlQueryExecutionPort()) = AgentContext(
        contextType = "general",
        capabilityDependencies = SchemaFacingCapabilityDependencyFactory.build(
            profile = PlatformProfiles.require("data-analysis"),
            schemaCatalog = catalogPort,
            metadataReadPort = EmptyMetadataReadPort(),
            dialectSpec = dialect,
            sqlQueryDependency = mockSqlQueryCapabilityDependency(execution = execution),
            valueMappingResolver = MockValueMappingResolver(),
        ),
    )

    @Test
    fun shouldCallDescribeSql_viaMockExecutionPort() {
        val execution = MockSqlQueryExecutionPort()
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("data-analysis"),
        )
        val executor = CapabilityMcpExecutor(CapabilityRegistry.load(), catalog, dataAnalysisContext(execution))
        val result = executor.callTool(
            "sql-query.describe_sql",
            mapOf("sql" to "SELECT country, COUNT(*) FROM clients GROUP BY country"),
        )
        val artifact = result.content as SqlQueryToolHandlers.SqlDescriptionArtifact
        assertThat(artifact.artifactType).isEqualTo("sql-description")
        assertThat(execution.lastDescribeRequest?.maxRows).isEqualTo(1)
    }

    @Test
    fun shouldCallExecuteSql_viaMockExecutionPort() {
        val execution = MockSqlQueryExecutionPort()
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("data-analysis"),
        )
        val executor = CapabilityMcpExecutor(CapabilityRegistry.load(), catalog, dataAnalysisContext(execution))
        val result = executor.callTool(
            "sql-query.execute_sql",
            mapOf("sql" to "SELECT country, COUNT(*) FROM clients GROUP BY country", "max_rows" to 5),
        )
        val artifact = result.content as SqlQueryToolHandlers.SqlExecutionArtifact
        assertThat(artifact.artifactType).isEqualTo("sql-result")
        assertThat(artifact.rows).isNotEmpty
        assertThat(execution.lastExecuteRequest?.resultMode).isEqualTo(SqlQueryResultMode.PAGED)
        assertThat(execution.lastExecuteRequest?.maxRows).isEqualTo(5)
    }

    @Test
    fun shouldSurfaceExecutionFailure_fromMockPort() {
        val execution = MockSqlQueryExecutionPort(failExecute = true)
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("data-analysis"),
        )
        val executor = CapabilityMcpExecutor(CapabilityRegistry.load(), catalog, dataAnalysisContext(execution))
        assertThrows(SqlQueryExecutionException::class.java) {
            executor.callTool(
                "sql-query.execute_sql",
                mapOf("sql" to "SELECT 1", "max_rows" to 1),
            )
        }
    }

    @Test
    fun shouldCallDemoSayHello() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        val result = executor.callTool("demo.say_hello", mapOf("name" to "Mill"))
        assertThat(result.content).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val map = result.content as Map<String, Any?>
        assertThat(map["greeting"]).isEqualTo("Hello, Mill!")
    }

    @Test
    fun shouldRejectUnknownTool() {
        val catalog = CapabilityMcpCatalog(registry = registry)
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.unknown")
        }
    }

    @Test
    fun shouldRejectToolOutsideProfileAtInvoke() {
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("hello-world"),
        )
        val executor = CapabilityMcpExecutor(
            registry = CapabilityRegistry.load(),
            catalog = catalog,
            context = context,
        )
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("schema.list_tables", mapOf("schemaName" to "main"))
        }
    }

    @Test
    fun shouldRejectToolOutsideAllowlistAtInvoke() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("conversation")),
        )
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.say_hello", mapOf("name" to "x"))
        }
    }

    @Test
    fun shouldRejectWhenAdmissionGateDenies() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        val gate = CapabilityAdmissionGate { _, _ -> McpAdmissionDecision.DENY }
        val executor = CapabilityMcpExecutor(registry, catalog, context, gate)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.say_hello", mapOf("name" to "x"))
        }
    }
}
