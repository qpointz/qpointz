package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ConversationRecord
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.persistence.ai.jpa.AiV3Urns
import io.qpointz.mill.persistence.ai.jpa.entities.ChatTurnEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatTurnRepository
import io.qpointz.mill.persistence.RelationRecord
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

open class JpaConversationStore(
    private val chatRepo: ChatRepository,
    private val turnRepo: ChatTurnRepository,
    private val relationRepo: AiRelationRepository,
    private val artifactRepo: ArtifactRepository,
) : ConversationStore {

    @Transactional
    override fun ensureExists(conversationId: String, profileId: String) {
        chatRepo.findById(conversationId).ifPresent { chat ->
            chat.updatedAt = Instant.now()
            chatRepo.save(chat)
        }
    }

    @Transactional
    override fun appendTurn(conversationId: String, turn: ConversationTurn) {
        val position = turnRepo.countByChatId(conversationId).toInt()
        turnRepo.save(
            ChatTurnEntity(
                turnId = turn.turnId,
                chatId = conversationId,
                profileId = turn.profileId,
                role = turn.role,
                text = turn.text,
                position = position,
                urn = AiV3Urns.turnUrn(turn.turnId),
                createdAt = turn.createdAt,
            )
        )
        if (turn.artifactIds.isNotEmpty()) {
            attachArtifacts(conversationId, turn.turnId, turn.artifactIds)
        }
        chatRepo.findById(conversationId).ifPresent { chat ->
            chat.updatedAt = Instant.now()
            chatRepo.save(chat)
        }
    }

    @Transactional
    override fun attachArtifacts(conversationId: String, turnId: String, artifactIds: List<String>) {
        val turnOpt = turnRepo.findById(turnId)
        if (turnOpt.isEmpty) return
        val turn = turnOpt.get()
        if (turn.chatId != conversationId) return
        val now = Instant.now()
        artifactIds.forEach { artifactId ->
            val artifact = artifactRepo.findById(artifactId).orElse(null) ?: return@forEach
            if (artifact.chatId != conversationId) return@forEach
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
        val chat = chatRepo.findById(conversationId).orElse(null) ?: return null
        val turns = turnRepo.findByChatIdOrderByPositionAsc(conversationId)
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
                profileId = turn.profileId,
                artifactIds = artifactIds,
                createdAt = turn.createdAt,
            )
        }
        return ConversationRecord(
            conversationId = chat.chatId,
            profileId = chat.profileId,
            turns = turnDomains,
            createdAt = chat.createdAt,
            updatedAt = chat.updatedAt,
        )
    }

    @Transactional
    override fun updateProfileId(conversationId: String, profileId: String) {
        chatRepo.findById(conversationId).ifPresent { chat ->
            chat.profileId = profileId
            chat.updatedAt = Instant.now()
            chatRepo.save(chat)
        }
    }

    /**
     * Removes transcript turns for [conversationId].
     *
     * When [io.qpointz.mill.ai.persistence.ChatRegistry.delete] runs against JPA, turns are
     * also removed via `ON DELETE CASCADE` from `ai_chat`; this supports explicit cleanup and
     * in-memory parity.
     */
    @Transactional
    override fun delete(conversationId: String) {
        turnRepo.deleteByChatId(conversationId)
    }
}
