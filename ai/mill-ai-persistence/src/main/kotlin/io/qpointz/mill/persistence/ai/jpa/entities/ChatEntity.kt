package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*
import java.time.Instant

/**
 * Unified AI chat parent row (metadata + current profile for the next message).
 */
@Entity
@Table(
    name = "ai_chat",
    indexes = [
        Index(name = "idx_ai_chat_user", columnList = "user_id"),
    ],
)
class ChatEntity(
    @Id
    @Column(name = "chat_id", nullable = false, length = 255)
    val chatId: String,

    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "profile_id", nullable = false, length = 255)
    var profileId: String,

    @Column(name = "chat_name", nullable = false, length = 512)
    var chatName: String,

    @Column(name = "chat_type", nullable = false, length = 64)
    val chatType: String,

    @Column(name = "is_favorite", nullable = false)
    var isFavorite: Boolean = false,

    @Column(name = "context_type", length = 255)
    val contextType: String? = null,

    @Column(name = "context_id", length = 255)
    val contextId: String? = null,

    @Column(name = "context_label", length = 512)
    var contextLabel: String? = null,

    @Column(name = "context_entity_type", length = 255)
    val contextEntityType: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
