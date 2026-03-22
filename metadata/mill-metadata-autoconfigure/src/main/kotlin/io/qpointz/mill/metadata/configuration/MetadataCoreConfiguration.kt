package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.domain.DefaultFacetClassResolver
import io.qpointz.mill.metadata.domain.FacetClassResolver
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetContentValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Core metadata beans: facet resolver, facet type repository, and facet catalog. */
@Configuration
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
    open fun facetTypeRepository(): FacetTypeRepository {
        val repo = InMemoryFacetTypeRepository()
        registerPlatformFacetTypes(repo)
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

    private fun registerPlatformFacetTypes(repo: FacetTypeRepository) {
        repo.save(FacetTypeDescriptor(
            typeKey = MetadataUrns.FACET_TYPE_STRUCTURAL, mandatory = true, enabled = true,
            displayName = "Structural", description = "Physical schema binding",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_ATTRIBUTE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE, mandatory = true, enabled = true,
            displayName = "Descriptive", description = "Human-readable metadata",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_SCHEMA, MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_ATTRIBUTE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = MetadataUrns.FACET_TYPE_RELATION, mandatory = true, enabled = true,
            displayName = "Relation", description = "Cross-entity relationships",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_TABLE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = MetadataUrns.FACET_TYPE_CONCEPT, mandatory = false, enabled = true,
            displayName = "Concept", description = "Business concept definitions",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_CONCEPT),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = MetadataUrns.FACET_TYPE_VALUE_MAPPING, mandatory = false, enabled = true,
            displayName = "Value Mapping", description = "Attribute value mappings for NL2SQL",
            applicableTo = setOf(MetadataUrns.ENTITY_TYPE_ATTRIBUTE),
            version = "1.0"))
        log.info("Registered 5 platform facet type descriptors (3 mandatory, 2 optional)")
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataCoreConfiguration::class.java)
    }
}
