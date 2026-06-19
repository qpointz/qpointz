package io.qpointz.mill.persistence.ai.jpa.entities

import io.qpointz.mill.persistence.EntityRef
import jakarta.persistence.*
import java.time.Instant

/**
 * Durable transcript turn for an AI chat.
 */
@Entity
@Table(
    name = "ai_chat_turn",
    indexes = [
        Index(name = "idx_ai_chat_turn_chat", columnList = "chat_id, position"),
    ],
)
class ChatTurnEntity(
    @Id
    @Column(name = "turn_id", nullable = false, length = 255)
    val turnId: String,

    @Column(name = "chat_id", nullable = false, length = 255)
    val chatId: String,

    @Column(name = "profile_id", nullable = false, length = 255)
    val profileId: String,

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
