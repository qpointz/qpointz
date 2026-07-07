package io.qpointz.mill.ai.autoconfigure.dependencies

import io.qpointz.mill.ai.autoconfigure.sqlquery.SqlQueryExecutionProperties
import io.qpointz.mill.ai.capabilities.concept.ConceptCatalogPort
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionLimits
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.capabilities.sqlquery.UnsupportedSqlQueryExecutionPort
import io.qpointz.mill.ai.capabilities.sqlquery.asSqlValidationService
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.dependencies.CapabilityDependencyAssembler
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.springframework.beans.factory.ObjectProvider

/**
 * [CapabilityDependencyAssembler] that pulls optional Spring collaborators and delegates to
 * [SchemaFacingCapabilityDependencyFactory].
 */
class SpringCapabilityDependencyAssembler(
    private val schemaCatalog: ObjectProvider<SchemaCatalogPort>,
    private val metadataReadPort: ObjectProvider<MetadataReadPort>,
    private val conceptCatalog: ObjectProvider<ConceptCatalogPort>,
    private val dialectSpec: ObjectProvider<SqlDialectSpec>,
    private val sqlValidator: ObjectProvider<SqlValidator>,
    private val sqlValidationService: ObjectProvider<SqlQueryToolHandlers.SqlValidationService>,
    private val sqlQueryExecutionPort: ObjectProvider<SqlQueryExecutionPort>,
    private val sqlQueryExecutionProperties: ObjectProvider<SqlQueryExecutionProperties>,
    private val valueMappingResolver: ObjectProvider<ValueMappingResolver>,
) : CapabilityDependencyAssembler {

    override fun assemble(profile: AgentProfile, metadata: ChatMetadata): CapabilityDependencyContainer {
        val sqlQueryDependency = buildSqlQueryDependency()
        return SchemaFacingCapabilityDependencyFactory.build(
            profile = profile,
            schemaCatalog = schemaCatalog.getIfAvailable(),
            metadataReadPort = metadataReadPort.getIfAvailable(),
            conceptCatalog = conceptCatalog.getIfAvailable(),
            dialectSpec = dialectSpec.getIfAvailable(),
            sqlQueryDependency = sqlQueryDependency,
            valueMappingResolver = valueMappingResolver.getIfAvailable(),
        )
    }

    private fun buildSqlQueryDependency(): SqlQueryCapabilityDependency? {
        val validator = sqlValidator.getIfAvailable()?.asSqlValidationService()
            ?: sqlValidationService.getIfAvailable()
            ?: return null
        val props = sqlQueryExecutionProperties.getIfAvailable()
        return SqlQueryCapabilityDependency(
            validator = validator,
            execution = sqlQueryExecutionPort.getIfAvailable() ?: UnsupportedSqlQueryExecutionPort,
            defaultMaxRows = props?.maxRowsDefault ?: SqlQueryExecutionLimits.DEFAULT_MAX_ROWS,
            hardMaxRows = props?.maxRowsHard ?: SqlQueryExecutionLimits.HARD_MAX_ROWS,
            dialectSpec = dialectSpec.getIfAvailable(),
        )
    }
}
