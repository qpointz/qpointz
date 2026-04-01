package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.source.MetadataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class FacetInstanceReadMergeTest {

    private val eid = "urn:mill/metadata/entity:demo.entity"

    private fun catalog(): FacetCatalog = mock<FacetCatalog>().apply {
        whenever(resolveCardinality(any())).thenReturn(FacetTargetCardinality.SINGLE)
        whenever(findDefinition(any())).thenAnswer { inv ->
            val k = inv.getArgument<String>(0)
            FacetTypeDefinition(
                typeKey = k,
                displayName = null,
                description = null,
                mandatory = false,
                enabled = true,
                targetCardinality = FacetTargetCardinality.SINGLE,
                applicableTo = null,
                contentSchema = null,
                schemaVersion = null,
                createdAt = Instant.EPOCH,
                createdBy = null,
                lastModifiedAt = Instant.EPOCH,
                lastModifiedBy = null
            )
        }
    }

    private fun row(
        facetType: String,
        origin: FacetOrigin,
        originId: String,
        displayName: String
    ): FacetInstance = FacetInstance(
        assignmentUuid = "assign-$originId-$facetType",
        entityId = eid,
        facetTypeKey = facetType,
        scopeKey = MetadataUrns.SCOPE_GLOBAL,
        mergeAction = MergeAction.SET,
        payload = mapOf("displayName" to displayName),
        createdAt = Instant.EPOCH,
        createdBy = null,
        lastModifiedAt = Instant.EPOCH,
        lastModifiedBy = null,
        origin = origin,
        originId = originId,
        assignmentUid = if (origin == FacetOrigin.CAPTURED) "assign-$originId-$facetType" else null
    )

    @Test
    fun `captured-only merge from repository-like source returns one row per type`() {
        val src = mock<MetadataSource> {
            on { originId } doReturn "repo-a"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(
                row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.CAPTURED, "repo-a", "Title")
            )
        }
        val merge = FacetInstanceReadMerge(listOf(src), catalog())
        val out = merge.merge(eid, MetadataReadContext.global())
        assertEquals(1, out.size)
        assertEquals("Title", out.single().payload["displayName"])
        assertEquals(FacetOrigin.CAPTURED, out.single().origin)
    }

    @Test
    fun `two sources different facet types both appear`() {
        val srcA = mock<MetadataSource> {
            on { originId } doReturn "a-source"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(
                row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.CAPTURED, "a-source", "D1")
            )
        }
        val structural = FacetInstance(
            assignmentUuid = "assign-struct",
            entityId = eid,
            facetTypeKey = MetadataUrns.FACET_TYPE_STRUCTURAL,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = mapOf("physicalName" to "col_x"),
            createdAt = Instant.EPOCH,
            createdBy = null,
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = null,
            origin = FacetOrigin.CAPTURED,
            originId = "b-source",
            assignmentUid = "assign-struct"
        )
        val srcB = mock<MetadataSource> {
            on { originId } doReturn "b-source"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(structural)
        }
        val merge = FacetInstanceReadMerge(listOf(srcB, srcA), catalog())
        val out = merge.merge(eid, MetadataReadContext.global())
        assertEquals(2, out.size)
        val types = out.map { it.facetTypeKey }.toSet()
        assertEquals(
            setOf(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.FACET_TYPE_STRUCTURAL),
            types
        )
    }

    @Test
    fun `SINGLE cardinality prefers CAPTURED over INFERRED when both sources contribute`() {
        val inferred = mock<MetadataSource> {
            on { originId } doReturn "inferred"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(
                row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.INFERRED, "inferred", "Inferred title")
            )
        }
        val captured = mock<MetadataSource> {
            on { originId } doReturn "repo"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(
                row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.CAPTURED, "repo", "Captured title")
            )
        }
        val merge = FacetInstanceReadMerge(listOf(inferred, captured), catalog())
        val out = merge.merge(eid, MetadataReadContext.global())
        assertEquals(1, out.size)
        assertEquals("Captured title", out.single().payload["displayName"])
        assertEquals(FacetOrigin.CAPTURED, out.single().origin)
    }

    @Test
    fun `non-active origin contributes no rows`() {
        val active = mock<MetadataSource> {
            on { originId } doReturn "active"
            on { fetchForEntity(eq(eid), any()) } doReturn listOf(
                row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.CAPTURED, "active", "Keep")
            )
        }
        val other = mock<MetadataSource> {
            on { originId } doReturn "other"
            on { fetchForEntity(eq(eid), any()) } doAnswer { invocation ->
                val ctx = invocation.getArgument<MetadataReadContext>(1)
                if (ctx.isOriginActive("other")) {
                    listOf(row(MetadataUrns.FACET_TYPE_DESCRIPTIVE, FacetOrigin.INFERRED, "other", "Drop"))
                } else {
                    emptyList()
                }
            }
        }
        val merge = FacetInstanceReadMerge(listOf(other, active), catalog())
        val ctx = MetadataReadContext(
            scopes = listOf(MetadataUrns.SCOPE_GLOBAL),
            origins = setOf("active")
        )
        val out = merge.merge(eid, ctx)
        assertEquals(1, out.size)
        assertEquals("active", out.single().originId)
    }
}
