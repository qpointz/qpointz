package io.qpointz.mill.metadata.repository.file

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/** YAML-backed [FacetTypeRepository] that preloads descriptors from configured resources. */
class FileFacetTypeRepository(
    locations: List<String>,
    private val resourceResolver: ResourceResolver
) : FacetTypeRepository {

    private val store = ConcurrentHashMap<String, FacetTypeDescriptor>()
    private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    init {
        for (location in locations) {
            try {
                val resources = resourceResolver.resolve(location)
                for (resource in resources) loadFromResource(resource)
            } catch (e: Exception) {
                log.warn("Failed to resolve facet type location: {}", location, e)
            }
        }
        log.info("Loaded {} facet type descriptors from {} location(s)", store.size, locations.size)
    }

    private fun loadFromResource(resource: ResolvedResource) {
        try {
            resource.inputStream.use { input ->
                val format = yamlMapper.readValue(input, FacetTypeFileFormat::class.java)
                for (descriptor in format.facetTypes) {
                    store[descriptor.typeKey] = descriptor
                    log.debug("Loaded facet type: {} from {}", descriptor.typeKey, resource.name)
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to load facet types from: {}", resource.name, e)
        }
    }

    override fun save(descriptor: FacetTypeDescriptor) {
        store[descriptor.typeKey] = descriptor
    }

    override fun findByTypeKey(typeKey: String): Optional<FacetTypeDescriptor> =
        Optional.ofNullable(store[typeKey])

    override fun findAll(): Collection<FacetTypeDescriptor> =
        store.values.toList()

    override fun deleteByTypeKey(typeKey: String) {
        store.remove(typeKey)
    }

    override fun existsByTypeKey(typeKey: String): Boolean =
        store.containsKey(typeKey)

    /** YAML wrapper object for `facet-types` array. */
    class FacetTypeFileFormat {
        @JsonProperty("facet-types")
        var facetTypes: List<FacetTypeDescriptor> = emptyList()
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileFacetTypeRepository::class.java)
    }
}
