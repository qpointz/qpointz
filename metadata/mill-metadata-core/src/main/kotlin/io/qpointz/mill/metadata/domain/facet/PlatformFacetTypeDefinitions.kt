package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.utils.JsonUtils

/**
 * Reference JSON for **descriptive** and **concept** facet manifests (fixtures, codegen, docs).
 *
 * **Runtime seeding** uses `metadata/platform-bootstrap.yaml` via `mill.metadata.seed.resources` only.
 * Keep this JSON aligned when editing the YAML (or regenerate fixtures from the YAML).
 *
 * JDBC/schema-owned types (**structural**, **relation**, **value-mapping**) are described in the same
 * bootstrap YAML; [io.qpointz.mill.data.schema.DataOwnedFacetTypeManifests] remains for Kotlin tests
 * and tooling that build manifests in code.
 */
object PlatformFacetTypeDefinitions {
    /** Classpath resource for [manifests]. */
    const val RESOURCE_PATH: String = "metadata/platform-facet-types.json"
    private val mapper = JsonUtils.defaultJsonMapper()
    private val cachedManifests: List<FacetTypeManifest> by lazy {
        val stream = PlatformFacetTypeDefinitions::class.java.classLoader
            .getResourceAsStream(RESOURCE_PATH)
            ?: throw IllegalStateException("Missing resource: $RESOURCE_PATH")
        stream.use {
            val javaType = mapper.typeFactory.constructCollectionType(List::class.java, FacetTypeManifest::class.java)
            @Suppress("UNCHECKED_CAST")
            val loaded = mapper.readValue(it, javaType) as List<FacetTypeManifest>
            loaded.map { FacetTypeManifestNormalizer.normalizeStrict(it) }
        }
    }

    /**
     * Reference manifests (metadata-owned types only).
     */
    @JvmStatic
    fun manifests(): List<FacetTypeManifest> = cachedManifests

    /** Facet type URN keys derived from [manifests]. */
    @JvmStatic
    fun typeKeys(): Set<String> = manifests().map { it.typeKey }.toSet()
}
