package io.qpointz.mill.persistence.ai.jpa.entities

import io.qpointz.mill.persistence.EntityRef
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "ai_conversation_turn")
class ConversationTurnEntity(
    @Id
    @Column(name = "turn_id", nullable = false, length = 255)
    val turnId: String,

    @Column(name = "conversation_id", nullable = false, length = 255)
    val conversationId: String,

    @Column(name = "role", nullable = false, length = 32)
    val role: String,

    @Column(name = "text", columnDefinition = "TEXT")
    val text: String?,

    @Column(name = "position", nullable = false)
    val position: Int,

    @Column(name = "urn", nullable = false, length = 1024)
    override val urn: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
) : EntityRef {
    override val id: String get() = turnId
    override val type: String get() = "agent/conversation-turn"
}
