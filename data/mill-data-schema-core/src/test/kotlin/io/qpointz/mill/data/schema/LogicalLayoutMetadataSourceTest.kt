package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.source.MetadataOriginIds
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LogicalLayoutMetadataSourceTest {

    private val ctx = MetadataReadContext.global()

    private fun provider(): SchemaProvider {
        val p = mock<SchemaProvider>()
        whenever(p.isSchemaExists("db")).thenReturn(true)
        whenever(p.getSchema("db")).thenReturn(
            Schema.newBuilder()
                .addTables(
                    Table.newBuilder()
                        .setSchemaName("db")
                        .setName("orders")
                        .setTableType(Table.TableTypeId.TABLE)
                        .addFields(
                            Field.newBuilder()
                                .setName("id")
                                .setFieldIdx(0)
                                .setType(
                                    DataType.newBuilder()
                                        .setNullability(DataType.Nullability.NOT_NULL)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        whenever(p.schemaNames).thenReturn(listOf("db"))
        return p
    }

    @Test
    fun `model root yields inferred descriptive facet`() {
        val src = LogicalLayoutMetadataSource(provider())
        val eid = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)
        val rows = src.fetchForEntity(eid, ctx)
        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals(FacetOrigin.INFERRED, row.origin)
        assertEquals(MetadataOriginIds.LOGICAL_LAYOUT, row.originId)
        assertEquals(MetadataUrns.FACET_TYPE_DESCRIPTIVE, row.facetTypeKey)
        assertTrue(row.payload["description"].toString().contains("db"))
    }

    @Test
    fun `schema entity yields inferred structural facet`() {
        val codec = DefaultMetadataEntityUrnCodec()
        val src = LogicalLayoutMetadataSource(provider())
        val eid = MetadataEntityUrn.canonicalize(codec.forSchema("db"))
        val rows = src.fetchForEntity(eid, ctx)
        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals(FacetOrigin.INFERRED, row.origin)
        assertEquals(MetadataUrns.FACET_TYPE_STRUCTURAL, row.facetTypeKey)
        assertEquals("db", row.payload["physicalName"])
    }

    @Test
    fun `table entity yields inferred structural facet`() {
        val codec = DefaultMetadataEntityUrnCodec()
        val src = LogicalLayoutMetadataSource(provider())
        val eid = MetadataEntityUrn.canonicalize(codec.forTable("db", "orders"))
        val rows = src.fetchForEntity(eid, ctx)
        assertEquals(1, rows.size)
        assertEquals("orders", rows.single().payload["physicalName"])
    }

    @Test
    fun `column entity yields inferred structural facet`() {
        val codec = DefaultMetadataEntityUrnCodec()
        val src = LogicalLayoutMetadataSource(provider())
        val eid = MetadataEntityUrn.canonicalize(codec.forAttribute("db", "orders", "id"))
        val rows = src.fetchForEntity(eid, ctx)
        assertEquals(1, rows.size)
        assertEquals("id", rows.single().payload["physicalName"])
    }

    @Test
    fun `unknown schema returns empty`() {
        val p = mock<SchemaProvider>()
        whenever(p.isSchemaExists("missing")).thenReturn(false)
        whenever(p.schemaNames).thenReturn(emptyList())
        val src = LogicalLayoutMetadataSource(p)
        val codec = DefaultMetadataEntityUrnCodec()
        val eid = MetadataEntityUrn.canonicalize(codec.forSchema("missing"))
        assertTrue(src.fetchForEntity(eid, ctx).isEmpty())
    }

    @Test
    fun `muted origin returns empty`() {
        val src = LogicalLayoutMetadataSource(provider())
        val codec = DefaultMetadataEntityUrnCodec()
        val eid = MetadataEntityUrn.canonicalize(codec.forSchema("db"))
        val muted = MetadataReadContext(
            scopes = listOf(MetadataUrns.SCOPE_GLOBAL),
            origins = setOf("repository-local")
        )
        assertTrue(src.fetchForEntity(eid, muted).isEmpty())
    }
}
