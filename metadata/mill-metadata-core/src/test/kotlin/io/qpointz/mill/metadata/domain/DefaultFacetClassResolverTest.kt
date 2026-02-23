package io.qpointz.mill.metadata.domain

import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultFacetClassResolverTest {

    private lateinit var resolver: DefaultFacetClassResolver

    @BeforeEach
    fun setUp() {
        resolver = DefaultFacetClassResolver()
    }

    @Test
    fun shouldRegister_andResolve() {
        resolver.register("structural", StructuralFacet::class.java)
        assertTrue(resolver.resolve("structural").isPresent)
        assertEquals(StructuralFacet::class.java, resolver.resolve("structural").get())
    }

    @Test
    fun shouldReturnEmpty_forUnregisteredType() {
        assertTrue(resolver.resolve("nonexistent").isEmpty)
    }

    @Test
    fun shouldRegisterMultipleTypes() {
        resolver.register("structural", StructuralFacet::class.java)
        resolver.register("descriptive", DescriptiveFacet::class.java)
        assertTrue(resolver.resolve("structural").isPresent)
        assertTrue(resolver.resolve("descriptive").isPresent)
    }
}
