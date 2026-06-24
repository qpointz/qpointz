package io.qpointz.mill.data.odata.service.edm

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.sdl.odata.api.edm.model.EntityDataModel
import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel
import io.qpointz.mill.data.odata.edm.SchemaEdmPackage
import io.qpointz.mill.data.odata.resolve.SchemaTableCache
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.metadata.service.MetadataContext
import java.time.Duration

/**
 * Caffeine-backed cache for OData EDM documents and per-table facet metadata.
 *
 * @param enabled when false, all lookups delegate to loaders without retaining entries
 * @param ttl optional write expiration; when null entries do not expire by time
 */
class ODataEdmCache(
    private val enabled: Boolean,
    private val ttl: Duration?,
) {

    private val packageCache: Cache<String, SchemaEdmPackage>? = if (enabled) newCache() else null
    private val tableCache: Cache<String, SchemaTableWithFacets>? = if (enabled) newCache() else null

    /**
     * @param key schema and metadata scope key
     * @param loader EDM builder when the key is absent
     * @return cached or freshly built EDM
     */
    fun getEntityDataModel(key: String, loader: () -> EntityDataModel): EntityDataModel =
        getSchemaEdmPackage(key) { SchemaEdmPackage(loader(), EdmAnnotationModel.Builder().build()) }
            .entityDataModel

    /**
     * @param key schema and metadata scope key
     * @param loader EDM package builder when the key is absent
     * @return cached or freshly built EDM package including CSDL annotations
     */
    fun getSchemaEdmPackage(key: String, loader: () -> SchemaEdmPackage): SchemaEdmPackage {
        val cache = packageCache ?: return loader()
        cache.getIfPresent(key)?.let { return it }
        val built = loader()
        cache.put(key, built)
        return built
    }

    /**
     * @return optional table metadata cache for [io.qpointz.mill.data.odata.resolve.EdmPropertyResolver]
     */
    fun schemaTableCache(): SchemaTableCache? =
        tableCache?.let { cache ->
            SchemaTableCache { key, loader ->
                cache.getIfPresent(key) ?: loader()?.also { cache.put(key, it) }
            }
        }

    private fun <T> newCache(): Cache<String, T> {
        val builder = Caffeine.newBuilder()
        ttl?.let { builder.expireAfterWrite(it) }
        return builder.build()
    }

    companion object {

        /**
         * @param schemaName physical schema name
         * @param context metadata read scope
         * @return stable EDM cache key
         */
        fun edmKey(schemaName: String, context: MetadataContext): String =
            "${schemaName.lowercase()}\u0000${context.scopes.joinToString(",")}\u0000${
                context.origins?.sorted()?.joinToString(",") ?: ""
            }"
    }
}
