package io.qpointz.mill.data.odata.service.edm

import com.sdl.odata.api.ODataException
import com.sdl.odata.api.edm.model.EntityDataModel
import com.sdl.odata.api.edm.registry.ODataEdmRegistry
import com.sdl.odata.edm.registry.ODataEdmRegistryImpl
import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel
import io.qpointz.mill.data.odata.annotation.EdmAnnotationProvider
import io.qpointz.mill.data.odata.edm.EntityDataModelFactory
import io.qpointz.mill.data.odata.edm.SchemaEdmPackage
import io.qpointz.mill.metadata.service.MetadataContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Lazily builds and caches per-schema [ODataEdmRegistry] instances.
 *
 * @param factory Mill schema-backed EDM builder
 * @param edmCache optional Caffeine cache for built [SchemaEdmPackage] instances
 */
class ODataEdmRegistryCache(
    private val factory: EntityDataModelFactory,
    private val edmCache: ODataEdmCache,
) : EdmAnnotationProvider {

    private val registries = ConcurrentHashMap<String, ODataEdmRegistry>()
    private val packages = ConcurrentHashMap<String, SchemaEdmPackage>()

    /**
     * @param schemaName physical schema name
     * @return EDM registry scoped to that schema
     */
    fun registryFor(schemaName: String): ODataEdmRegistry =
        registries.computeIfAbsent(schemaName) { SchemaODataEdmRegistry(schemaName, factory, edmCache, packages) }

    /**
     * @param schemaName physical schema name (OData service container name)
     * @return facet-derived CSDL annotations for that schema
     */
    override fun annotationsForSchema(schemaName: String): EdmAnnotationModel =
        packages[schemaName]?.annotations ?: EdmAnnotationModel.Builder().build()

    private class SchemaODataEdmRegistry(
        private val schemaName: String,
        private val factory: EntityDataModelFactory,
        private val edmCache: ODataEdmCache,
        private val packages: ConcurrentHashMap<String, SchemaEdmPackage>,
    ) : ODataEdmRegistryImpl() {

        @Throws(ODataException::class)
        override fun getEntityDataModel(): EntityDataModel {
            val context = MetadataContext.global()
            val key = ODataEdmCache.edmKey(schemaName, context)
            val schemaPackage = edmCache.getSchemaEdmPackage(key) {
                factory.buildPackageForSchema(schemaName, context)
            }
            packages[schemaName] = schemaPackage
            return schemaPackage.entityDataModel
        }
    }
}
