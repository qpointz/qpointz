package io.qpointz.mill.metadata.domain;

import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.StructuralFacet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacetRegistryTest {
    
    private FacetRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = FacetRegistry.getInstance();
    }
    
    @Test
    void shouldRegisterFacetType() {
        registry.register(StructuralFacet.class);
        
        assertTrue(registry.isRegistered("structural"));
        assertEquals(StructuralFacet.class, registry.getFacetClass("structural"));
    }
    
    @Test
    void shouldGetFacetType_forClass() {
        String facetType = FacetRegistry.getFacetType(DescriptiveFacet.class);
        assertEquals("descriptive", facetType);
    }
}

