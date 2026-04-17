package io.qpointz.mill.ai.valuemap.refresh

import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ValueMappingFacetAssembly
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Global value-mapping refresh entry point (WI-182): startup, scheduled tick, and in-process on-demand.
 */
class ValueMappingRefreshOrchestrator(
    private val refreshConfig: ValueMappingRefreshConfigurationBridge,
    private val attributeDiscovery: ValueMappingIndexedAttributeDiscovery,
    private val facetRepository: FacetRepository,
    private val valueMappingService: ValueMappingService,
    private val refreshStateRepository: ValueMappingRefreshStateRepository,
    private val embeddingRepository: ValueMappingEmbeddingRepository,
    private val embeddingHarness: EmbeddingHarness,
    private val columnDistinctValueLoader: ColumnDistinctValueLoader,
    private val schemaProvider: SchemaProvider?,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val attributeLocks = ConcurrentHashMap<String, Any>()

    /**
     * Startup pass: attributes with primary facet `refreshAtStartUp` and global startup enabled.
     */
    fun runStartup() {
        if (!refreshConfig.refreshStartupEnabled) {
            log.info("valueMappingRefresh APP_STARTUP skipped (startup disabled)")
            return
        }
        runGlobal(ValueMappingRefreshRunKind.APP_STARTUP)
    }

    /**
     * Scheduled evaluation pass: due attributes with a `refreshInterval` (WI-182).
     */
    fun runScheduledTick() {
        if (refreshConfig.refreshScheduledDisabled) {
            return
        }
        runGlobal(ValueMappingRefreshRunKind.SCHEDULED_TICK)
    }

    /**
     * In-process single-attribute refresh (WI-182); ignores per-facet `refreshInterval`.
     */
    fun runOnDemand(attributeUrn: String) {
        runOne(attributeUrn, ValueMappingRefreshRunKind.ON_DEMAND)
    }

    private fun runGlobal(kind: ValueMappingRefreshRunKind) {
        val urns = attributeDiscovery.listAttributeUrns()
        val gated = urns.filter { passKindGate(it, kind) }
        log.info("valueMappingRefresh kind={} discovered={} gated={}", kind, urns.size, gated.size)
        if (kind == ValueMappingRefreshRunKind.APP_STARTUP && urns.isNotEmpty() && gated.isEmpty()) {
            log.warn(
                "valueMappingRefresh APP_STARTUP: {} attribute(s) discovered but none passed gates — " +
                    "set mill.ai.value-mapping.refresh.startup-enabled=true and primary facet " +
                    "data.enabled=true and data.refreshAtStartUp=true (WI-182)",
                urns.size,
            )
        }
        for (urn in gated) {
            try {
                runOne(urn, kind)
            } catch (t: Throwable) {
                log.error("valueMappingRefresh unexpected failure entityRes={} kind={}", urn, kind, t)
            }
        }
        log.info("valueMappingRefresh kind={} complete", kind)
    }

    private fun passKindGate(entityRes: String, kind: ValueMappingRefreshRunKind): Boolean {
        when (kind) {
            ValueMappingRefreshRunKind.APP_STARTUP -> if (!refreshConfig.refreshStartupEnabled) return false
            ValueMappingRefreshRunKind.SCHEDULED_TICK -> if (refreshConfig.refreshScheduledDisabled) return false
            ValueMappingRefreshRunKind.ON_DEMAND -> return true
        }
        val facets = facetRepository.findByEntity(entityRes)
        val primary = ValueMappingFacetAssembly.parsePrimaryFromFacets(facets) ?: return false
        if (!primary.enabled) {
            return false
        }
        return when (kind) {
            ValueMappingRefreshRunKind.APP_STARTUP -> primary.refreshAtStartUp
            ValueMappingRefreshRunKind.SCHEDULED_TICK -> {
                val interval = primary.refreshInterval ?: return false
                val state = refreshStateRepository.findByEntityRes(entityRes)
                val last = state?.lastRefreshAt ?: return true
                val dueAt = last.plus(interval)
                !Instant.now().isBefore(dueAt)
            }
            ValueMappingRefreshRunKind.ON_DEMAND -> true
        }
    }

    private fun runOne(entityRes: String, kind: ValueMappingRefreshRunKind) {
        val lock = attributeLocks.computeIfAbsent(entityRes) { Any() }
        synchronized(lock) {
            val facets = facetRepository.findByEntity(entityRes)
            val primary = ValueMappingFacetAssembly.parsePrimaryFromFacets(facets) ?: return
            if (!primary.enabled) {
                return
            }

            val path = ModelEntityUrn.parseCatalogPath(entityRes)
            if (path.column == null || path.schema == null || path.table == null) {
                log.warn("valueMappingRefresh STALE entityRes={} (non-relational URN)", entityRes)
                refreshStateRepository.markStale(entityRes, "non-relational or unparsable model URN")
                return
            }
            if (schemaProvider != null && !physicalColumnPresent(schemaProvider, path)) {
                log.warn("valueMappingRefresh STALE entityRes={} (no physical column)", entityRes)
                refreshStateRepository.markStale(entityRes, "physical table/column missing from SchemaProvider")
                return
            }

            val nextScheduled = primary.refreshInterval?.let { Instant.now().plus(it) }

            val distinct = columnDistinctValueLoader.loadDistinctQuoted(
                path.schema!!,
                path.table!!,
                path.column!!,
                primary.indexNull,
            )
            val source = ValueMappingFacetAssembly.buildValueSource(facets, distinct) ?: run {
                log.warn("valueMappingRefresh no ValueSource entityRes={}", entityRes)
                return
            }

            val bridge = ValueMappingRefreshProgressBridge(refreshStateRepository, nextScheduled)
            val modelId = resolveEmbeddingModelId()
            try {
                valueMappingService.syncFromSource(entityRes, source, modelId, bridge)
            } catch (t: Throwable) {
                log.error("valueMappingRefresh syncFromSource failed entityRes={} kind={}", entityRes, kind, t)
                bridge.markFailed(entityRes, t.message ?: t.javaClass.simpleName)
            }
        }
    }

    private fun resolveEmbeddingModelId(): Long {
        val p = embeddingHarness.persistence
        return embeddingRepository.ensureEmbeddingModel(
            p.configFingerprint,
            p.provider,
            p.modelId,
            p.dimension,
            p.paramsJson,
            p.label ?: "value-mapping-refresh",
        )
    }

    private fun physicalColumnPresent(provider: SchemaProvider, path: CatalogPath): Boolean {
        val schema = path.schema ?: return false
        val tableName = path.table ?: return false
        val column = path.column ?: return false
        if (!provider.isSchemaExists(schema)) {
            return false
        }
        val table: Table = provider.getTable(schema, tableName) ?: return false
        return table.fieldsList.any { field: Field -> field.name == column }
    }
}
