package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ConversationRecord
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.persistence.ai.jpa.AiV3Urns
import io.qpointz.mill.persistence.ai.jpa.entities.ConversationEntity
import io.qpointz.mill.persistence.ai.jpa.entities.ConversationTurnEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ConversationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ConversationTurnRepository
import io.qpointz.mill.persistence.RelationRecord
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

open class JpaConversationStore(
    private val conversationRepo: ConversationRepository,
    private val turnRepo: ConversationTurnRepository,
    private val relationRepo: AiRelationRepository,
    private val artifactRepo: ArtifactRepository,
) : ConversationStore {

    @Transactional
    override fun ensureExists(conversationId: String, profileId: String) {
        if (!conversationRepo.existsById(conversationId)) {
            val now = Instant.now()
            conversationRepo.save(
                ConversationEntity(
                    conversationId = conversationId,
                    profileId = profileId,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }

    @Transactional
    override fun appendTurn(conversationId: String, turn: ConversationTurn) {
        val position = turnRepo.countByConversationId(conversationId).toInt()
        turnRepo.save(
            ConversationTurnEntity(
                turnId = turn.turnId,
                conversationId = conversationId,
                role = turn.role,
                text = turn.text,
                position = position,
                urn = AiV3Urns.turnUrn(turn.turnId),
                createdAt = turn.createdAt,
            )
        )
        // Update conversation updatedAt by mutating the managed entity so Hibernate does not
        // treat existing turns as orphans (creating a new entity with an empty turns list
        // and CascadeType.ALL + orphanRemoval=true would delete all persisted turns).
        conversationRepo.findById(conversationId).ifPresent { conv ->
            conv.updatedAt = Instant.now()
        }
    }

    @Transactional
    override fun attachArtifacts(conversationId: String, turnId: String, artifactIds: List<String>) {
        val turnOpt = turnRepo.findById(turnId)
        if (turnOpt.isEmpty) return
        // Verify the turn belongs to the expected conversation
        val turn = turnOpt.get()
        if (turn.conversationId != conversationId) return
        val now = Instant.now()
        artifactIds.forEach { artifactId ->
            // Guard: artifact must exist and belong to the same conversation
            val artifact = artifactRepo.findById(artifactId).orElse(null) ?: return@forEach
            if (artifact.conversationId != conversationId) return@forEach
            // Skip if relation already exists
            val existing = relationRepo.findByRelationKindAndSourceIdAndSourceType(
                AiV3Urns.RELATION_TURN_TO_ARTIFACT, turnId, AiV3Urns.TYPE_TURN,
            )
            if (existing.none { it.targetId == artifactId }) {
                relationRepo.save(
                    RelationRecord(
                        relationId = UUID.randomUUID().toString(),
                        relationKind = AiV3Urns.RELATION_TURN_TO_ARTIFACT,
                        sourceId = turn.id,
                        sourceType = turn.type,
                        sourceUrn = turn.urn,
                        targetId = artifact.id,
                        targetType = AiV3Urns.TYPE_ARTIFACT,
                        targetUrn = artifact.urn,
                        createdAt = now,
                    )
                )
            }
        }
    }

    override fun load(conversationId: String): ConversationRecord? {
        val entity = conversationRepo.findById(conversationId).orElse(null) ?: return null
        val turns = turnRepo.findByConversationIdOrderByPositionAsc(conversationId)
        val turnDomains = turns.map { turn ->
            val artifactIds = relationRepo
                .findByRelationKindAndSourceIdAndSourceType(
                    AiV3Urns.RELATION_TURN_TO_ARTIFACT,
                    turn.turnId,
                    AiV3Urns.TYPE_TURN,
                )
                .map { it.targetId }
            ConversationTurn(
                turnId = turn.turnId,
                role = turn.role,
                text = turn.text,
                artifactIds = artifactIds,
                createdAt = turn.createdAt,
            )
        }
        return ConversationRecord(
            conversationId = entity.conversationId,
            profileId = entity.profileId,
            turns = turnDomains,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
