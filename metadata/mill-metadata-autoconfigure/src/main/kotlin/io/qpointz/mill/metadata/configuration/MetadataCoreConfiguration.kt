package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.domain.DefaultFacetClassResolver
import io.qpointz.mill.metadata.domain.FacetClassResolver
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataTargetType
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

    @Bean
    @ConditionalOnMissingBean
    open fun facetClassResolver(): FacetClassResolver {
        val resolver = DefaultFacetClassResolver()
        resolver.register("structural", StructuralFacet::class.java)
        resolver.register("descriptive", DescriptiveFacet::class.java)
        resolver.register("relation", RelationFacet::class.java)
        resolver.register("concept", ConceptFacet::class.java)
        resolver.register("value-mapping", ValueMappingFacet::class.java)
        log.info("Registered platform facet class resolver with 5 mappings")
        return resolver
    }

    @Bean
    @ConditionalOnMissingBean
    open fun facetTypeRepository(): FacetTypeRepository {
        val repo = InMemoryFacetTypeRepository()
        registerPlatformFacetTypes(repo)
        return repo
    }

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
            typeKey = "structural", mandatory = true, enabled = true,
            displayName = "Structural", description = "Physical schema binding",
            applicableTo = setOf(MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = "descriptive", mandatory = true, enabled = true,
            displayName = "Descriptive", description = "Human-readable metadata",
            applicableTo = setOf(MetadataTargetType.SCHEMA, MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = "relation", mandatory = true, enabled = true,
            displayName = "Relation", description = "Cross-entity relationships",
            applicableTo = setOf(MetadataTargetType.TABLE),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = "concept", mandatory = false, enabled = true,
            displayName = "Concept", description = "Business concept definitions",
            applicableTo = setOf(MetadataTargetType.CONCEPT),
            version = "1.0"))
        repo.save(FacetTypeDescriptor(
            typeKey = "value-mapping", mandatory = false, enabled = true,
            displayName = "Value Mapping", description = "Attribute value mappings for NL2SQL",
            applicableTo = setOf(MetadataTargetType.ATTRIBUTE),
            version = "1.0"))
        log.info("Registered 5 platform facet type descriptors (3 mandatory, 2 optional)")
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataCoreConfiguration::class.java)
    }
}
