package io.qpointz.mill.metadata.domain;

import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.StructuralFacet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFacetClassResolverTest {

    private DefaultFacetClassResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DefaultFacetClassResolver();
    }

    @Test
    void shouldRegister_andResolve() {
        resolver.register("structural", StructuralFacet.class);
        assertTrue(resolver.resolve("structural").isPresent());
        assertEquals(StructuralFacet.class, resolver.resolve("structural").get());
    }

    @Test
    void shouldReturnEmpty_forUnregisteredType() {
        assertTrue(resolver.resolve("nonexistent").isEmpty());
    }

    @Test
    void shouldRegisterMultipleTypes() {
        resolver.register("structural", StructuralFacet.class);
        resolver.register("descriptive", DescriptiveFacet.class);

        assertTrue(resolver.resolve("structural").isPresent());
        assertTrue(resolver.resolve("descriptive").isPresent());
    }
}
