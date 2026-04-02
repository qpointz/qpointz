package io.qpointz.mill.data.metadata.source

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.metadata.LayoutInferredFacetTypeKeys
import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.source.MetadataOriginIds
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table

/**
 * Read-only [io.qpointz.mill.metadata.source.MetadataSource] that projects Mill **logical** physical schema
 * (from [SchemaProvider] / proto [io.qpointz.mill.proto.Schema]) onto metadata entities as **inferred**
 * structural (and a minimal **model** summary) facets — SPEC §3g / WI-138. No secrets or JDBC-specific descriptors.
 *
 * [originId] is [MetadataOriginIds.LOGICAL_LAYOUT]. Contributions use [io.qpointz.mill.metadata.domain.facet.FacetOrigin.INFERRED] and
 * omit [io.qpointz.mill.metadata.domain.facet.FacetInstance.assignmentUid].
 *
 * @param schemaProvider supplier of proto catalog snapshots keyed by schema name
 */
class LogicalLayoutMetadataSource(
    private val schemaProvider: SchemaProvider,
) : AbstractInferredMetadataSource(MetadataOriginIds.LOGICAL_LAYOUT) {

    override fun fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance> {
        if (!context.isOriginActive(originId)) {
            return emptyList()
        }
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val modelId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)
        if (eid == modelId) {
            return emptyList()
        }
        val path = ModelEntityUrn.parseCatalogPath(eid)
        val schemaName = path.schema ?: return emptyList()
        if (!schemaProvider.isSchemaExists(schemaName)) {
            return emptyList()
        }
        val schemaProto = schemaProvider.getSchema(schemaName)

        if (path.table == null) {
            return listOf(schemaStructuralFacet(eid, schemaName))
        }
        val table = schemaProto.tablesList.firstOrNull {
            it.name.equals(path.table, ignoreCase = true)
        } ?: return emptyList()

        if (path.column == null) {
            return listOf(tableStructuralFacet(eid, schemaName, table))
        }
        val field = table.fieldsList.firstOrNull {
            it.name.equals(path.column, ignoreCase = true)
        } ?: return emptyList()
        return listOf(columnStructuralFacet(eid, schemaName, table.name, field))
    }

    private fun schemaStructuralFacet(
        entityId: String,
        schemaName: String,
    ) = inferredFacet(
        entityId = entityId,
        facetTypeKey = LayoutInferredFacetTypeKeys.SCHEMA,
        payload = mapOf(
            "schema" to schemaName,
        ),
    )

    private fun tableStructuralFacet(entityId: String, schemaName: String, table: Table) =
        inferredFacet(
            entityId = entityId,
            facetTypeKey = LayoutInferredFacetTypeKeys.TABLE,
            payload = mapOf(
                "schema" to schemaName,
                "table" to table.name,
            ),
        )

    private fun columnStructuralFacet(
        entityId: String,
        schemaName: String,
        tableName: String,
        field: Field,
    ): FacetInstance {
        val dataType = field.type.type
        val nullable = field.type.nullability == DataType.Nullability.NULL ||
            field.type.nullability == DataType.Nullability.NOT_SPECIFIED_NULL
        return inferredFacet(
            entityId = entityId,
            facetTypeKey = LayoutInferredFacetTypeKeys.COLUMN,
            payload = mapOf(
                "schema" to schemaName,
                "table" to tableName,
                "column" to field.name,
                "type" to mapOf<String, Any>(
                    "name" to dataType.typeId.name,
                    "nullable" to nullable,
                    "precision" to dataType.precision,
                    "scale" to dataType.scale,
                    "length" to 0,
                ),
            ),
        )
    }
}
