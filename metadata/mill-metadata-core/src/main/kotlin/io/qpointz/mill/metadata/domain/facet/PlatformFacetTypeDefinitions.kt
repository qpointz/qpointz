package io.qpointz.mill.metadata.domain.facet

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Single source of truth for built-in platform facet type manifests.
 *
 * All default seeding flows should consume these manifests:
 * - in-memory repository bootstrap
 * - import fallback bootstrap (`ensurePlatformFacetTypesPresent`)
 * - SQL/JPA alignment migrations when platform defaults evolve
 */
object PlatformFacetTypeDefinitions {
    private const val RESOURCE_PATH = "metadata/platform-facet-types.json"
    private val mapper = jacksonObjectMapper()
    private val cachedManifests: List<FacetTypeManifest> by lazy {
        val stream = PlatformFacetTypeDefinitions::class.java.classLoader
            .getResourceAsStream(RESOURCE_PATH)
            ?: throw IllegalStateException("Missing resource: $RESOURCE_PATH")
        stream.use {
            val loaded: List<FacetTypeManifest> = mapper.readValue(it)
            loaded.map { FacetTypeManifestNormalizer.normalizeStrict(it) }
        }
    }

    /**
     * Canonical platform facet type manifests.
     *
     * Keep ordering stable for readability and deterministic bootstrap logs.
     */
    @JvmStatic
    fun manifests(): List<FacetTypeManifest> = cachedManifests

    /** Platform facet type URN keys derived from [manifests]. */
    @JvmStatic
    fun typeKeys(): Set<String> = manifests().map { it.typeKey }.toSet()
}

