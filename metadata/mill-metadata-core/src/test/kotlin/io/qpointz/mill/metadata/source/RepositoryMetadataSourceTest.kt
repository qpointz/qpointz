package io.qpointz.mill.metadata.source

import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.service.FacetCatalog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

/**
 * Unit tests for [RepositoryMetadataSource]: origin muting and CAPTURED read mapping.
 */
class RepositoryMetadataSourceTest {

    private val entityId = "urn:mill/metadata/entity:test.schema"

    private val assignment = FacetAssignment(
        uid = "facet-assign-1",
        entityId = entityId,
        facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        scopeKey = MetadataUrns.SCOPE_GLOBAL,
        mergeAction = MergeAction.SET,
        payload = mapOf("displayName" to "X"),
        createdAt = Instant.EPOCH,
        createdBy = null,
        lastModifiedAt = Instant.EPOCH,
        lastModifiedBy = null
    )

    @Test
    fun `fetchForEntity returns empty when repository origin is muted`() {
        val facetRepo = mock<FacetRepository>()
        val catalog = mock<FacetCatalog>().apply {
            whenever(resolveCardinality(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).thenReturn(FacetTargetCardinality.SINGLE)
        }
        val reader = MetadataReader(catalog)
        val source = RepositoryMetadataSource(facetRepo, reader)
        val ctx = MetadataReadContext(
            scopes = listOf(MetadataUrns.SCOPE_GLOBAL),
            origins = setOf("other-origin")
        )

        val out = source.fetchForEntity(entityId, ctx)

        assertTrue(out.isEmpty())
    }

    @Test
    fun `fetchForEntity maps merged assignments to CAPTURED read rows with repository originId`() {
        val facetRepo = mock<FacetRepository>().apply {
            whenever(findByEntity(entityId)).thenReturn(listOf(assignment))
        }
        val catalog = mock<FacetCatalog>().apply {
            whenever(resolveCardinality(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).thenReturn(FacetTargetCardinality.SINGLE)
        }
        val reader = MetadataReader(catalog)
        val source = RepositoryMetadataSource(facetRepo, reader)
        val ctx = MetadataReadContext.global()

        val out = source.fetchForEntity(entityId, ctx)

        assertEquals(1, out.size)
        val row = out.single()
        assertEquals(FacetOrigin.CAPTURED, row.origin)
        assertEquals(MetadataOriginIds.REPOSITORY_LOCAL, row.originId)
        assertEquals(assignment.uid, row.assignmentUid)
        assertEquals(assignment.uid, row.uid)
        assertEquals(assignment.payload, row.payload)
    }
}
