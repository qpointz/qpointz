package io.qpointz.mill.data.backend.flow

import com.fasterxml.jackson.core.type.TypeReference
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.qpointz.mill.data.metadata.FlowInferredFacetTypeKeys
import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.data.metadata.source.AbstractInferredMetadataSource
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.metadata.source.MetadataOriginIds
import io.qpointz.mill.source.SourceCatalogProvider
import io.qpointz.mill.source.SourceResolver
import io.qpointz.mill.source.descriptor.ReaderDescriptor
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.descriptor.SourceObjectMapper
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.descriptor.TableAttributeDescriptor
import io.qpointz.mill.source.descriptor.TableDescriptor
import io.qpointz.mill.source.descriptor.TableMappingDescriptor
import io.qpointz.mill.source.factory.SourceMaterializer
import java.time.Duration
import java.util.Locale

private val MAP_TYPE = object : TypeReference<Map<String, Any?>>() {}

private data class FlowFacetSnapshot(
    val schemaPayload: Map<String, Any?>,
    val tablePayloads: Map<String, Map<String, Any?>>,
    val columnPayloads: Map<String, Map<String, Any?>>,
)

/**
 * Read-only [io.qpointz.mill.metadata.source.MetadataSource] that projects Mill flow YAML descriptors
 * onto **`flow-*`** facet rows for the Data Model UI ([MetadataOriginIds.FLOW]).
 *
 * @param catalog source descriptors (typically the flow backend repository)
 * @param facetsCacheEnabled when true, retain per-source inference snapshots in a Caffeine cache
 * @param facetsCacheTTL optional expiration for the snapshot cache; when null entries do not expire by time
 */
class FlowDescriptorMetadataSource(
    private val catalog: SourceCatalogProvider,
    private val facetsCacheEnabled: Boolean,
    private val facetsCacheTTL: Duration?,
) : AbstractInferredMetadataSource(MetadataOriginIds.FLOW) {

    private val cache: Cache<String, FlowFacetSnapshot>? =
        if (facetsCacheEnabled) {
            val builder = Caffeine.newBuilder()
            facetsCacheTTL?.let { builder.expireAfterWrite(it) }
            builder.build()
        } else {
            null
        }

    /** @param entityId canonical model entity URN */
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
        val schemaSegment = path.schema ?: return emptyList()
        val descriptor = findDescriptor(schemaSegment) ?: return emptyList()
        val snapshot = snapshotFor(descriptor)

        return when {
            path.table == null ->
                listOf(inferredFacet(eid, FlowInferredFacetTypeKeys.SCHEMA, snapshot.schemaPayload))
            path.column == null -> {
                val tableName = path.table!!
                val payload = resolveTablePayload(snapshot, tableName) ?: return emptyList()
                listOf(inferredFacet(eid, FlowInferredFacetTypeKeys.TABLE, payload))
            }
            else -> {
                val payload =
                    resolveColumnPayload(
                        snapshot,
                        descriptor.name,
                        path.table!!,
                        path.column!!,
                    )
                        ?: return emptyList()
                listOf(inferredFacet(eid, FlowInferredFacetTypeKeys.COLUMN, payload))
            }
        }
    }

    private fun findDescriptor(schemaName: String): SourceDescriptor? {
        val target = schemaName.trim().lowercase(Locale.ROOT)
        for (d in catalog.getSourceDefinitions()) {
            if (d.name.trim().lowercase(Locale.ROOT) == target) {
                return d
            }
        }
        return null
    }

    private fun snapshotFor(descriptor: SourceDescriptor): FlowFacetSnapshot {
        val key = snapshotKey(descriptor.name)
        cache?.getIfPresent(key)?.let { return it }
        val built = buildSnapshot(descriptor)
        cache?.put(key, built)
        return built
    }

    private fun buildSnapshot(descriptor: SourceDescriptor): FlowFacetSnapshot {
        SourceResolver.resolveDescriptor(descriptor, SourceMaterializer()).use { resolved ->
            val schemaPayload =
                mapOf(
                    "sourceName" to descriptor.name,
                    "storage" to storageToPayload(descriptor.storage),
                )
            val tablePayloads = mutableMapOf<String, Map<String, Any?>>()
            val columnPayloads = mutableMapOf<String, Map<String, Any?>>()
            val attrIndex = attributeIndex(descriptor)

            for ((tableName, sourceTable) in resolved.tables) {
                tablePayloads[tableName] = tablePayload(descriptor, tableName)
                for (field in sourceTable.schema.fields) {
                    val key = columnKey(descriptor.name, tableName, field.name)
                    val binding = attributeBinding(attrIndex[field.name.lowercase(Locale.ROOT)])
                    columnPayloads[key] =
                        mapOf(
                            "schema" to descriptor.name,
                            "table" to tableName,
                            "column" to field.name,
                            "binding" to binding,
                        )
                }
            }
            return FlowFacetSnapshot(schemaPayload, tablePayloads, columnPayloads)
        }
    }

    private fun tablePayload(descriptor: SourceDescriptor, tableName: String): Map<String, Any?> {
        val tableInputs =
            descriptor.readers.mapIndexed { idx, r ->
                val row =
                    mutableMapOf<String, Any?>(
                        "format" to r.type,
                        "readerIndex" to idx,
                    )
                effectiveMapping(r, descriptor)?.let { row["effectiveMapping"] = toParamMap(it) }
                row["params"] = toParamMap(r.format)
                r.label?.let { row["label"] = it }
                row
            }
        return mapOf(
            "schema" to descriptor.name,
            "table" to tableName,
            "tableInputs" to tableInputs,
        )
    }

    private fun effectiveMapping(reader: ReaderDescriptor, source: SourceDescriptor): TableMappingDescriptor? {
        val effectiveTable: TableDescriptor? = reader.table ?: source.table
        return effectiveTable?.mapping
    }

    /**
     * @return map from lower-cased attribute name to descriptor (reader-level table blocks override by name)
     */
    private fun attributeIndex(descriptor: SourceDescriptor): Map<String, TableAttributeDescriptor> {
        val attrs = LinkedHashMap<String, TableAttributeDescriptor>()
        descriptor.table?.attributes?.forEach { attrs[it.name.lowercase(Locale.ROOT)] = it }
        for (r in descriptor.readers) {
            val t = r.table ?: descriptor.table
            t?.attributes?.forEach { attrs[it.name.lowercase(Locale.ROOT)] = it }
        }
        return attrs
    }

    private fun attributeBinding(attr: TableAttributeDescriptor?): Map<String, Any?> =
        if (attr == null) {
            mapOf("type" to "inferred")
        } else {
            mapOf(
                "type" to "attribute",
                "params" to toParamMap(attr),
            )
        }

    private fun storageToPayload(storage: StorageDescriptor): Map<String, Any?> {
        val m = SourceObjectMapper.yaml.convertValue(storage, MAP_TYPE)
        val type = m["type"]?.toString() ?: "unknown"
        val params = m.filterKeys { it != "type" }
        return mapOf("type" to type, "params" to params)
    }

    private fun toParamMap(value: Any): Map<String, Any?> = SourceObjectMapper.yaml.convertValue(value, MAP_TYPE)

    private fun snapshotKey(descriptorName: String): String = descriptorName.trim().lowercase(Locale.ROOT)

    private fun columnKey(schema: String, table: String, column: String): String =
        "${schema.lowercase(Locale.ROOT)}\u0000${table.lowercase(Locale.ROOT)}\u0000${column.lowercase(Locale.ROOT)}"

    private fun resolveTablePayload(snapshot: FlowFacetSnapshot, table: String): Map<String, Any?>? {
        snapshot.tablePayloads[table]?.let { return it }
        val t = table.lowercase(Locale.ROOT)
        return snapshot.tablePayloads.entries.firstOrNull { it.key.lowercase(Locale.ROOT) == t }?.value
    }

    private fun resolveColumnPayload(
        snapshot: FlowFacetSnapshot,
        schema: String,
        table: String,
        column: String,
    ): Map<String, Any?>? {
        val key = columnKey(schema, table, column)
        snapshot.columnPayloads[key]?.let { return it }
        val c = column.lowercase(Locale.ROOT)
        return snapshot.columnPayloads.entries.firstOrNull { (k, _) ->
            val parts = k.split('\u0000')
            parts.size == 3 &&
                parts[0] == schema.lowercase(Locale.ROOT) &&
                parts[1] == table.lowercase(Locale.ROOT) &&
                parts[2] == c
        }?.value
    }
}
