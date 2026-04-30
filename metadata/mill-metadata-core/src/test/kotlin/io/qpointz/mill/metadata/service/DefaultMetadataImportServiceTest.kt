package io.qpointz.mill.metadata.service

import tools.jackson.databind.node.ArrayNode
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataExportFormat
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.utils.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class DefaultMetadataImportServiceTest {

    private val entityRepository: EntityRepository = mock()
    private val entityService: MetadataEntityService = mock()
    private val facetRepository: FacetRepository = mock()
    private val scopeRepository: MetadataScopeRepository = mock()
    private val facetCatalog: FacetCatalog = mock()

    private lateinit var service: DefaultMetadataImportService

    private val teamScopeUrn = MetadataUrns.scopeTeam("eng")
    private val definedTypeUrn = "urn:mill/metadata/facet-type:governance"
    private val observedTypeUrn = "urn:mill/metadata/facet-type:observed-flex"

    private val entityId = "urn:mill/model/schema:acme"

    @BeforeEach
    fun setUp() {
        service = DefaultMetadataImportService(
            entityRepository,
            entityService,
            facetRepository,
            scopeRepository,
            facetCatalog
        )
        val scopeGlobal = MetadataScope(
            res = MetadataUrns.SCOPE_GLOBAL,
            scopeType = "GLOBAL",
            referenceId = null,
            displayName = "Global",
            ownerId = null,
            visibility = "PUBLIC",
            uuid = "s1",
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        val scopeTeam = MetadataScope(
            res = teamScopeUrn,
            scopeType = "TEAM",
            referenceId = "eng",
            displayName = "Eng",
            ownerId = null,
            visibility = "PUBLIC",
            uuid = "s2",
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        whenever(scopeRepository.findAll()).thenReturn(listOf(scopeTeam, scopeGlobal))

        val def = FacetTypeDefinition(
            typeKey = definedTypeUrn,
            displayName = "Gov",
            description = null,
            category = null,
            mandatory = false,
            enabled = true,
            targetCardinality = FacetTargetCardinality.SINGLE,
            applicableTo = null,
            contentSchema = mapOf("type" to "object"),
            schemaVersion = "1",
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        whenever(facetCatalog.listDefinitions()).thenReturn(listOf(def))

        val entity = MetadataEntity(
            id = entityId,
            kind = "schema",
            uuid = "e1",
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        whenever(entityService.findAll()).thenReturn(listOf(entity))

        val fGlobalDefined = FacetAssignment(
            uid = "uid-1",
            entityId = entityId,
            facetTypeKey = definedTypeUrn,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = mapOf("a" to 1),
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        val fGlobalObserved = FacetAssignment(
            uid = "uid-2",
            entityId = entityId,
            facetTypeKey = observedTypeUrn,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = mapOf("b" to 2),
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        val fTeam = FacetAssignment(
            uid = "uid-3",
            entityId = entityId,
            facetTypeKey = definedTypeUrn,
            scopeKey = teamScopeUrn,
            mergeAction = MergeAction.SET,
            payload = mapOf("c" to 3),
            createdAt = Instant.EPOCH,
            createdBy = "t",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "t"
        )
        whenever(facetRepository.findByEntity(entityId)).thenReturn(
            listOf(fGlobalDefined, fGlobalObserved, fTeam)
        )
    }

    @Test
    fun shouldEmitScopeAndCatalogDefinitions_andIncludeObservedFacetRows_whenScopeDefaultsToGlobal() {
        val yaml = service.export(scopeQuery = null, format = MetadataExportFormat.YAML)
        val doc = MetadataYamlSerializer.deserialize(yaml)
        assertThat(doc.scopes).hasSize(2)
        assertThat(doc.definitions.map { it.typeKey }).containsExactly(definedTypeUrn)
        assertThat(doc.entities).hasSize(1)
        val facets = doc.facetsByEntity[entityId].orEmpty()
        assertThat(facets.map { it.facetTypeKey }).containsExactlyInAnyOrder(
            definedTypeUrn,
            observedTypeUrn
        )
    }

    @Test
    fun shouldMatchYamlDocumentCount_whenExportingJson() {
        val yaml = service.export(scopeQuery = null, format = MetadataExportFormat.YAML)
        val json = service.export(scopeQuery = null, format = MetadataExportFormat.JSON)
        val yamlDocs = yaml.split(Regex("^---\\s*$", RegexOption.MULTILINE))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .size
        val arr = JsonUtils.defaultJsonMapper().readTree(json) as ArrayNode
        assertThat(arr.size()).isEqualTo(yamlDocs)
    }

    @Test
    fun shouldIncludeTeamFacet_whenScopeIsAll() {
        val yaml = service.export(scopeQuery = "all", format = MetadataExportFormat.YAML)
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val facets = doc.facetsByEntity[entityId].orEmpty()
        assertThat(facets.map { it.scopeKey }).contains(teamScopeUrn)
    }

    @Test
    fun shouldUnionScopes_whenCommaSeparatedQuery() {
        val yaml = service.export(
            scopeQuery = "global,team:eng",
            format = MetadataExportFormat.YAML
        )
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val facets = doc.facetsByEntity[entityId].orEmpty()
        assertThat(facets).hasSize(3)
    }

    @Test
    fun shouldRejectEmptyScopeSegment() {
        assertThrows<MillStatusRuntimeException> {
            service.export(scopeQuery = "global,,team:eng", format = MetadataExportFormat.YAML)
        }
    }

    @Test
    fun shouldRejectScopeUrnOutsideNamespace() {
        assertThrows<MillStatusRuntimeException> {
            service.export(
                scopeQuery = "urn:mill/metadata/facet-type:descriptive",
                format = MetadataExportFormat.YAML
            )
        }
    }
}
