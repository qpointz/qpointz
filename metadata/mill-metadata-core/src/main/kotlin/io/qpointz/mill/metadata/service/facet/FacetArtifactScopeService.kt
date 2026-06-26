package io.qpointz.mill.metadata.service.facet

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.CardinalityAwareFacetProposalMerger
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetProposalMerger
import io.qpointz.mill.metadata.domain.facet.FacetProposalMergerInput
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

/**
 * Applies facet proposals from chat artefacts into metadata scopes (WI-360).
 *
 * @param facetRepository facet assignment persistence
 * @param facetTypeDefinitionRepository catalog definitions for cardinality
 * @param facetTypeRepository runtime facet type rows required by JPA facet saves
 * @param entityRepository metadata entity identity rows required by JPA facet saves
 * @param merger plans scope rows from proposals
 */
class FacetArtifactScopeService(
    private val facetRepository: FacetRepository,
    private val facetTypeDefinitionRepository: FacetTypeDefinitionRepository,
    private val facetTypeRepository: FacetTypeRepository,
    private val entityRepository: EntityRepository,
    private val merger: FacetProposalMerger = CardinalityAwareFacetProposalMerger(),
) {

    private val log = LoggerFactory.getLogger(FacetArtifactScopeService::class.java)

    /**
     * Materializes a captured facet proposal into each writable scope.
     *
     * @param sourceArtifactId originating chat artefact id
     * @param metadataEntityId target entity URN
     * @param facetTypeKey facet type key or URN
     * @param payload facet JSON body
     * @param writeScopeUrns scopes to write into
     * @param actorId optional actor for audit columns
     */
    fun assignFromProposal(
        sourceArtifactId: String,
        metadataEntityId: String,
        facetTypeKey: String,
        payload: Map<String, Any?>,
        writeScopeUrns: List<String>,
        actorId: String? = null,
    ) {
        if (writeScopeUrns.isEmpty()) return
        val existing = facetRepository.findBySourceArtifactId(sourceArtifactId)
        if (existing.any { it.mergeAction == MergeAction.SET }) {
            log.debug("Skipping duplicate facet assign for artefactId={}", sourceArtifactId)
            return
        }
        val entityId = MetadataEntityUrn.canonicalize(metadataEntityId)
        val facetTypeUrn = MetadataUrns.normaliseFacetTypePath(facetTypeKey)
        val definition = facetTypeDefinitionRepository.findByKey(facetTypeUrn)
            ?: error("Unknown facet type: $facetTypeKey")
        ensureEntity(entityId, actorId)
        ensureRuntimeFacetType(facetTypeUrn, definition, actorId)
        val plans = merger.planAssignments(
            FacetProposalMergerInput(
                metadataEntityId = entityId,
                facetTypeKey = facetTypeUrn,
                payload = payload,
                writeScopeUrns = writeScopeUrns.map(MetadataUrns::normaliseScopePath),
                sourceArtifactId = sourceArtifactId,
            ),
            definition.targetCardinality,
        )
        val now = Instant.now()
        plans.forEach { plan ->
            facetRepository.save(
                FacetAssignment(
                    uid = UUID.randomUUID().toString(),
                    entityId = plan.entityId,
                    facetTypeKey = plan.facetTypeKey,
                    scopeKey = plan.scopeUrn,
                    mergeAction = plan.mergeAction,
                    payload = plan.payload,
                    createdAt = now,
                    createdBy = actorId,
                    lastModifiedAt = now,
                    lastModifiedBy = actorId,
                    sourceArtifactId = sourceArtifactId,
                ),
            )
        }
        log.info(
            "Assigned {} facet row(s) for artefactId={} entityId={} scopes={}",
            plans.size,
            sourceArtifactId,
            entityId,
            writeScopeUrns,
        )
    }

    /**
     * Tombstones scope rows created for a retracted chat artefact.
     *
     * @param sourceArtifactId originating chat artefact id
     * @param actorId optional actor for audit columns
     */
    fun retractBySourceArtifactId(sourceArtifactId: String, actorId: String? = null) {
        val now = Instant.now()
        facetRepository.findBySourceArtifactId(sourceArtifactId).forEach { row ->
            facetRepository.save(
                row.copy(
                    mergeAction = MergeAction.TOMBSTONE,
                    lastModifiedAt = now,
                    lastModifiedBy = actorId,
                ),
            )
        }
    }

    private fun ensureEntity(entityId: String, actorId: String?) {
        if (entityRepository.findById(entityId) != null) return
        val now = Instant.now()
        entityRepository.save(
            MetadataEntity(
                id = entityId,
                kind = entityKindFromUrn(entityId),
                uuid = UUID.randomUUID().toString(),
                createdAt = now,
                createdBy = actorId,
                lastModifiedAt = now,
                lastModifiedBy = actorId,
            ),
        )
        log.debug("Created metadata entity row for {}", entityId)
    }

    private fun ensureRuntimeFacetType(
        typeKey: String,
        definition: io.qpointz.mill.metadata.domain.FacetTypeDefinition,
        actorId: String?,
    ) {
        if (facetTypeRepository.findByKey(typeKey) != null) return
        val now = Instant.now()
        facetTypeRepository.save(
            FacetType(
                typeKey = typeKey,
                source = FacetTypeSource.DEFINED,
                definition = definition,
                createdAt = now,
                createdBy = actorId,
                lastModifiedAt = now,
                lastModifiedBy = actorId,
            ),
        )
        log.debug("Created runtime facet type row for {}", typeKey)
    }

    private fun entityKindFromUrn(entityUrn: String): String? =
        ENTITY_KIND_PATTERN.find(entityUrn)?.groupValues?.get(1)

    private companion object {
        private val ENTITY_KIND_PATTERN = Regex("urn:mill/model/([^:]+):")
    }
}
