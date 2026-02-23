package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.domain.*;
import io.qpointz.mill.metadata.domain.core.*;
import io.qpointz.mill.metadata.repository.FacetTypeRepository;
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository;
import io.qpointz.mill.metadata.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Slf4j
@Configuration
public class MetadataCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FacetClassResolver facetClassResolver() {
        var resolver = new DefaultFacetClassResolver();
        resolver.register("structural", StructuralFacet.class);
        resolver.register("descriptive", DescriptiveFacet.class);
        resolver.register("relation", RelationFacet.class);
        resolver.register("concept", ConceptFacet.class);
        resolver.register("value-mapping", ValueMappingFacet.class);
        log.info("Registered platform facet class resolver with 5 mappings");
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public FacetTypeRepository facetTypeRepository() {
        var repo = new InMemoryFacetTypeRepository();
        registerPlatformFacetTypes(repo);
        return repo;
    }

    @Bean
    @ConditionalOnMissingBean
    public FacetCatalog facetCatalog(FacetTypeRepository facetTypeRepository,
                                     @Autowired(required = false) FacetContentValidator contentValidator) {
        log.info("Creating DefaultFacetCatalog (content validator: {})",
                contentValidator != null ? "enabled" : "disabled");
        return new DefaultFacetCatalog(facetTypeRepository, contentValidator);
    }

    private void registerPlatformFacetTypes(FacetTypeRepository repo) {
        repo.save(FacetTypeDescriptor.builder()
                .typeKey("structural")
                .mandatory(true)
                .enabled(true)
                .displayName("Structural")
                .description("Physical schema binding")
                .applicableTo(Set.of(MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE))
                .version("1.0")
                .build());

        repo.save(FacetTypeDescriptor.builder()
                .typeKey("descriptive")
                .mandatory(true)
                .enabled(true)
                .displayName("Descriptive")
                .description("Human-readable metadata")
                .applicableTo(Set.of(MetadataTargetType.SCHEMA, MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE))
                .version("1.0")
                .build());

        repo.save(FacetTypeDescriptor.builder()
                .typeKey("relation")
                .mandatory(true)
                .enabled(true)
                .displayName("Relation")
                .description("Cross-entity relationships")
                .applicableTo(Set.of(MetadataTargetType.TABLE))
                .version("1.0")
                .build());

        repo.save(FacetTypeDescriptor.builder()
                .typeKey("concept")
                .mandatory(false)
                .enabled(true)
                .displayName("Concept")
                .description("Business concept definitions")
                .applicableTo(Set.of(MetadataTargetType.CONCEPT))
                .version("1.0")
                .build());

        repo.save(FacetTypeDescriptor.builder()
                .typeKey("value-mapping")
                .mandatory(false)
                .enabled(true)
                .displayName("Value Mapping")
                .description("Attribute value mappings for NL2SQL")
                .applicableTo(Set.of(MetadataTargetType.ATTRIBUTE))
                .version("1.0")
                .build());

        log.info("Registered 5 platform facet type descriptors (3 mandatory, 2 optional)");
    }
}
