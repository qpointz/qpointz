package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PhysicalCatalogMatchTest {

    @Test
    fun `physicalColumnPresent matches lowercase path to uppercase physical names`() {
        val provider = mock<SchemaProvider>()
        whenever(provider.getSchemaNames()).thenReturn(listOf("SKYMILL"))
        whenever(provider.getTable("SKYMILL", "cities")).thenReturn(null)
        whenever(provider.getSchema("SKYMILL")).thenReturn(
            Schema.newBuilder()
                .addTables(
                    Table.newBuilder()
                        .setSchemaName("SKYMILL")
                        .setName("CITIES")
                        .addFields(Field.newBuilder().setName("STATE").build())
                        .build(),
                )
                .build(),
        )

        val path = CatalogPath("skymill", "cities", "state")
        assertTrue(PhysicalCatalogMatch.physicalColumnPresent(provider, path))
        val ids = PhysicalCatalogMatch.resolvePhysicalIdentifiers(provider, path)
        assertNotNull(ids)
        assertEquals("SKYMILL", ids!!.schema)
        assertEquals("CITIES", ids.table)
        assertEquals("STATE", ids.column)
    }

    @Test
    fun `physicalColumnPresent false when column missing`() {
        val provider = mock<SchemaProvider>()
        whenever(provider.getSchemaNames()).thenReturn(listOf("skymill"))
        whenever(provider.getTable("skymill", "cities")).thenReturn(
            Table.newBuilder()
                .setSchemaName("skymill")
                .setName("cities")
                .addFields(Field.newBuilder().setName("other").build())
                .build(),
        )

        val ok =
            PhysicalCatalogMatch.physicalColumnPresent(
                provider,
                CatalogPath("skymill", "cities", "state"),
            )
        assertFalse(ok)
    }
}
