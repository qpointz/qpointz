package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.TableType
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.source.MetadataOriginIds
import io.qpointz.mill.metadata.source.MetadataSource
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table
import java.time.Instant
import java.util.UUID

/**
 * Read-only [MetadataSource] that projects Mill **logical** physical schema (from [SchemaProvider]
 * / proto [io.qpointz.mill.proto.Schema]) onto metadata entities as **inferred** structural (and
 * a minimal **model** summary) facets — SPEC §3g / WI-138. No secrets or JDBC-specific descriptors.
 *
 * [originId] is [MetadataOriginIds.LOGICAL_LAYOUT]. Contributions use [FacetOrigin.INFERRED] and
 * omit [FacetInstance.assignmentUid].
 *
 * @param schemaProvider supplier of proto catalog snapshots keyed by schema name
 */
class LogicalLayoutMetadataSource(
    private val schemaProvider: SchemaProvider
) : MetadataSource {

    override val originId: String = MetadataOriginIds.LOGICAL_LAYOUT

    override fun fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance> {
        if (!context.isOriginActive(originId)) {
            return emptyList()
        }
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val modelId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)
        if (eid == modelId) {
            return listOf(modelRootFacet(eid))
        }
        val path = RelationalMetadataEntityUrns.parseCatalogPath(eid)
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
        return listOf(columnStructuralFacet(eid, field))
    }

    private fun modelRootFacet(entityId: String): FacetInstance {
        val names = schemaProvider.schemaNames.sorted()
        val description = if (names.isEmpty()) {
            "Logical catalog (no schemas reported by the schema provider)."
        } else {
            "Logical catalog with ${names.size} schema(s): ${names.joinToString(", ")}."
        }
        return inferredFacet(
            entityId = entityId,
            facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            payload = mapOf(
                "displayName" to "Catalog model",
                "description" to description
            )
        )
    }

    private fun schemaStructuralFacet(
        entityId: String,
        schemaName: String
    ): FacetInstance {
        return inferredFacet(
            entityId = entityId,
            facetTypeKey = MetadataUrns.FACET_TYPE_STRUCTURAL,
            payload = mapOf(
                "physicalName" to schemaName,
                "backendType" to LOGICAL_BACKEND,
                "tableType" to TableType.TABLE.name,
                "nullable" to false
            )
        )
    }

    private fun tableStructuralFacet(entityId: String, schemaName: String, table: Table): FacetInstance =
        inferredFacet(
            entityId = entityId,
            facetTypeKey = MetadataUrns.FACET_TYPE_STRUCTURAL,
            payload = mapOf(
                "physicalName" to table.name,
                "backendType" to LOGICAL_BACKEND,
                "tableType" to tableTypeFromProto(table.tableType).name
            )
        )

    private fun columnStructuralFacet(
        entityId: String,
        field: Field
    ): FacetInstance {
        val nullability = field.type.nullability
        val nullable = nullability == DataType.Nullability.NULL ||
            nullability == DataType.Nullability.NOT_SPECIFIED_NULL
        return inferredFacet(
            entityId = entityId,
            facetTypeKey = MetadataUrns.FACET_TYPE_STRUCTURAL,
            payload = mapOf(
                "physicalName" to field.name,
                "physicalType" to logicalTypeLabel(field.type),
                "nullable" to nullable,
                "backendType" to LOGICAL_BACKEND
            )
        )
    }

    private fun inferredFacet(
        entityId: String,
        facetTypeKey: String,
        payload: Map<String, Any?>
    ): FacetInstance {
        val typeCanon = MetadataEntityUrn.canonicalize(facetTypeKey)
        val uid = UUID.nameUUIDFromBytes("$entityId|$typeCanon|$originId".toByteArray(Charsets.UTF_8)).toString()
        val now = Instant.now()
        return FacetInstance(
            assignmentUuid = uid,
            entityId = entityId,
            facetTypeKey = typeCanon,
            scopeKey = MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL),
            mergeAction = MergeAction.SET,
            payload = payload,
            createdAt = now,
            createdBy = null,
            lastModifiedAt = now,
            lastModifiedBy = null,
            origin = FacetOrigin.INFERRED,
            originId = originId,
            assignmentUid = null
        )
    }

    private companion object {
        const val LOGICAL_BACKEND: String = "mill.logical"

        private fun tableTypeFromProto(id: Table.TableTypeId): TableType = when (id) {
            Table.TableTypeId.VIEW -> TableType.VIEW
            Table.TableTypeId.TABLE,
            Table.TableTypeId.NOT_SPECIFIED_TABLE_TYPE,
            Table.TableTypeId.UNRECOGNIZED -> TableType.TABLE
        }

        private fun logicalTypeLabel(dt: DataType): String {
            val lid = dt.type?.typeId ?: return "UNKNOWN"
            return lid.name
        }
    }
}
