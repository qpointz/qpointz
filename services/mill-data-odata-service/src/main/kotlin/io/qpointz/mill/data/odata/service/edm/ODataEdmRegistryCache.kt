package io.qpointz.mill.data.odata.service.edm

import com.sdl.odata.api.ODataException
import com.sdl.odata.api.edm.model.EntityDataModel
import com.sdl.odata.api.edm.registry.ODataEdmRegistry
import com.sdl.odata.edm.registry.ODataEdmRegistryImpl
import io.qpointz.mill.data.odata.edm.EntityDataModelFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Lazily builds and caches per-schema [ODataEdmRegistry] instances.
 */
class ODataEdmRegistryCache(
    private val factory: EntityDataModelFactory,
) {

    private val registries = ConcurrentHashMap<String, ODataEdmRegistry>()

    /**
     * @param schemaName physical schema name
     * @return EDM registry scoped to that schema
     */
    fun registryFor(schemaName: String): ODataEdmRegistry =
        registries.computeIfAbsent(schemaName) { SchemaODataEdmRegistry(schemaName, factory) }

    private class SchemaODataEdmRegistry(
        private val schemaName: String,
        private val factory: EntityDataModelFactory,
    ) : ODataEdmRegistryImpl() {

        @Throws(ODataException::class)
        override fun getEntityDataModel(): EntityDataModel = factory.buildForSchema(schemaName)
    }
}
