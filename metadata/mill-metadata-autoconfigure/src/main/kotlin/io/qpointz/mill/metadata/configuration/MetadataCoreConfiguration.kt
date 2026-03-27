package io.qpointz.mill.metadata.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.metadata.domain.DefaultFacetClassResolver
import io.qpointz.mill.metadata.domain.FacetClassResolver
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.domain.facet.PlatformFacetTypeDefinitions
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetContentValidator
import org.springframework.boot.ApplicationRunner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Core metadata beans: facet resolver, facet type repository, and facet catalog. */
@Configuration
@EnableConfigurationProperties(MetadataProperties::class)
open class MetadataCoreConfiguration {

    /**
     * Creates the default [FacetClassResolver] with all five platform facet classes registered.
     *
     * @return default resolver mapping URN type keys to their facet POJO classes
     */
    @Bean
    @ConditionalOnMissingBean
    open fun facetClassResolver(): FacetClassResolver {
        val resolver = DefaultFacetClassResolver()
        resolver.register(MetadataUrns.FACET_TYPE_STRUCTURAL, StructuralFacet::class.java)
        resolver.register(MetadataUrns.FACET_TYPE_DESCRIPTIVE, DescriptiveFacet::class.java)
        resolver.register(MetadataUrns.FACET_TYPE_RELATION, RelationFacet::class.java)
        resolver.register(MetadataUrns.FACET_TYPE_CONCEPT, ConceptFacet::class.java)
        resolver.register(MetadataUrns.FACET_TYPE_VALUE_MAPPING, ValueMappingFacet::class.java)
        log.info("Registered platform facet class resolver with 5 mappings")
        return resolver
    }

    /**
     * Creates an in-memory [FacetTypeRepository] pre-loaded with all five platform facet types.
     *
     * @return repository containing platform facet type descriptors
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "mill.metadata.facet-type-registry",
        name = ["type"],
        havingValue = "inMemory",
        matchIfMissing = true
    )
    open fun facetTypeRepository(objectMapper: ObjectMapper): FacetTypeRepository {
        val repo = InMemoryFacetTypeRepository()
        registerPlatformFacetTypes(repo, objectMapper)
        return repo
    }

    /**
     * Creates the default [FacetCatalog] backed by [facetTypeRepository].
     *
     * @param facetTypeRepository  the underlying facet type store
     * @param contentValidator     optional content validator; omit to skip schema validation
     * @return configured catalog instance
     */
    @Bean
    @ConditionalOnMissingBean
    open fun facetCatalog(
        facetTypeRepository: FacetTypeRepository,
        @Autowired(required = false) contentValidator: FacetContentValidator?
    ): FacetCatalog {
        log.info("Creating DefaultFacetCatalog (content validator: {})",
            if (contentValidator != null) "enabled" else "disabled")
        return DefaultFacetCatalog(facetTypeRepository, contentValidator)
    }

    /**
     * Validates facet type registry strategy selection at startup.
     *
     * `portal` is declared as a contract for future work and intentionally fails fast.
     * `local` expects local persistence-backed descriptor sourcing (typically JPA).
     */
    @Bean
    open fun facetTypeRegistryStrategyGuard(
        properties: MetadataProperties
    ): ApplicationRunner = ApplicationRunner {
        val strategy = properties.facetTypeRegistry.type
        when (strategy) {
            "portal" -> throw IllegalStateException(
                "mill.metadata.facet-type-registry.type=portal is not implemented yet"
            )
            "local" -> {
                if (properties.storage.type != "jpa") {
                    throw IllegalStateException(
                        "mill.metadata.facet-type-registry.type=local requires mill.metadata.storage.type=jpa"
                    )
                }
            }
            "inMemory" -> {
                // default fallback strategy
            }
            else -> throw IllegalStateException(
                "Unsupported mill.metadata.facet-type-registry.type='$strategy'. Supported: inMemory, local, portal"
            )
        }
    }

    private fun registerPlatformFacetTypes(repo: FacetTypeRepository, objectMapper: ObjectMapper) {
        val manifests = PlatformFacetTypeDefinitions.manifests()
        manifests.forEach { manifest ->
            repo.save(
                FacetTypeDescriptor(
                    typeKey = manifest.typeKey,
                    mandatory = manifest.mandatory,
                    targetCardinality = manifest.targetCardinality,
                    enabled = manifest.enabled,
                    displayName = manifest.title,
                    description = manifest.description,
                    applicableTo = manifest.applicableTo?.toSet(),
                    version = manifest.schemaVersion,
                    contentSchema = null,
                    manifestJson = objectMapper.writeValueAsString(manifest)
                )
            )
        }
        log.info(
            "Registered {} platform facet type descriptors (source: PlatformFacetTypeDefinitions)",
            manifests.size
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataCoreConfiguration::class.java)
    }
}
