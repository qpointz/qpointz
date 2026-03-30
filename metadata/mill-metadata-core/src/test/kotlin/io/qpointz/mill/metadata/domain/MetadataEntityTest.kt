package io.qpointz.mill.metadata.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class MetadataEntityTest {

    @Test
    fun shouldHoldIdentityFields() {
        val t = Instant.EPOCH
        val entity = MetadataEntity(
            id = "urn:mill/metadata/entity:app.s1.t1",
            kind = "table",
            uuid = "u1",
            createdAt = t,
            createdBy = "a",
            lastModifiedAt = t,
            lastModifiedBy = "b"
        )
        assertEquals("urn:mill/metadata/entity:app.s1.t1", entity.id)
        assertEquals("table", entity.kind)
        assertEquals("u1", entity.uuid)
    }

    @Test
    fun shouldAllowNullKind() {
        val t = Instant.EPOCH
        val entity = MetadataEntity(
            id = "urn:mill/metadata/entity:concept:x",
            kind = null,
            uuid = null,
            createdAt = t,
            createdBy = null,
            lastModifiedAt = t,
            lastModifiedBy = null
        )
        assertNull(entity.kind)
    }
}
