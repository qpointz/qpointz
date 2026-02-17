package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.domain.FacetRegistry;
import io.qpointz.mill.metadata.domain.core.ConceptFacet;
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.RelationFacet;
import io.qpointz.mill.metadata.domain.core.StructuralFacet;
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for metadata core module.
 * Registers core facets with FacetRegistry.
 */
@Slf4j
@Configuration
public class MetadataCoreConfiguration {
    
    @PostConstruct
    public void registerCoreFacets() {
        FacetRegistry registry = FacetRegistry.getInstance();
        
        registry.register(StructuralFacet.class);
        registry.register(DescriptiveFacet.class);
        registry.register(RelationFacet.class);
        registry.register(ConceptFacet.class);
        registry.register(ValueMappingFacet.class);
        
        log.info("Registered core metadata facets: structural, descriptive, relation, concept, value-mapping");
    }
}

