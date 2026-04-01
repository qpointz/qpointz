package io.qpointz.mill.metadata.domain

import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FacetPayloadUtilsTest {

    @Test
    fun shouldConvert_mapToDescriptiveFacet() {
        val raw = mapOf("description" to "d1", "displayName" to "n1")
        val opt = FacetPayloadUtils.convert(raw, DescriptiveFacet::class.java)
        assertTrue(opt.isPresent)
        assertEquals("d1", opt.get().description)
        assertEquals("n1", opt.get().displayName)
    }

    @Test
    fun shouldReturnInstance_whenAlreadyCorrectType() {
        val f = DescriptiveFacet(description = "x")
        val opt = FacetPayloadUtils.convert(f, DescriptiveFacet::class.java)
        assertTrue(opt.isPresent)
        assertEquals("x", opt.get().description)
    }

    @Test
    fun shouldReturnEmpty_forNull() {
        assertTrue(FacetPayloadUtils.convert(null, DescriptiveFacet::class.java).isEmpty)
    }
}
