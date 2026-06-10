package io.qpointz.mill.ai.dependencies

import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.metadata.MetadataCapabilityDependency
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCapabilityDependency
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.profile.SchemaAuthoringAgentProfile
import io.qpointz.mill.ai.profile.SchemaExplorationAgentProfile
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaFacingCapabilityDependencyFactoryTest {

    private val catalog = object : SchemaCatalogPort {
        override fun listSchemas(): List<ListSchemasItem> = emptyList()
        override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()
        override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> = emptyList()
        override fun listRelations(
            schemaName: String,
            tableName: String,
            direction: RelationDirection,
        ): List<ListRelationsItem> = emptyList()
    }

    private val dialect = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")
    private val metadataPort = EmptyMetadataReadPort()

    @Test
    fun `build fills schema and metadata only for exploration profile`() {
        val c = SchemaFacingCapabilityDependencyFactory.build(
            profile = SchemaExplorationAgentProfile.profile,
            schemaCatalog = catalog,
            metadataReadPort = metadataPort,
            dialectSpec = dialect,
            sqlQueryDependency = SqlQueryCapabilityDependency(MockSqlValidationService()),
            valueMappingResolver = MockValueMappingResolver(),
        )
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.SCHEMA).get(SchemaCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.METADATA).get(MetadataCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.METADATA_AUTHORING).get(MetadataCapabilityDependency::class.java)).isNull()
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.SQL_DIALECT).get(SqlDialectCapabilityDependency::class.java)).isNull()
    }

    @Test
    fun `build fills all slots for authoring profile when collaborators present`() {
        val c = SchemaFacingCapabilityDependencyFactory.build(
            profile = SchemaAuthoringAgentProfile.profile,
            schemaCatalog = catalog,
            metadataReadPort = metadataPort,
            dialectSpec = dialect,
            sqlQueryDependency = SqlQueryCapabilityDependency(MockSqlValidationService()),
            valueMappingResolver = MockValueMappingResolver(),
        )
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.SCHEMA).get(SchemaCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.METADATA).get(MetadataCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.METADATA_AUTHORING).get(MetadataCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.SQL_DIALECT).get(SqlDialectCapabilityDependency::class.java)).isNotNull
        assertThat(c.forCapability(SchemaFacingCapabilityDependencyFactory.SQL_QUERY).get(SqlQueryCapabilityDependency::class.java)).isNotNull
        assertThat(
            c.forCapability(SchemaFacingCapabilityDependencyFactory.VALUE_MAPPING)
                .get(io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingCapabilityDependency::class.java),
        ).isNotNull
    }
}
