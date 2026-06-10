package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "ai_conversation")
class ConversationEntity(
    @Id
    @Column(name = "conversation_id", nullable = false, length = 255)
    val conversationId: String,

    @Column(name = "profile_id", nullable = false, length = 255)
    val profileId: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @OneToMany(
        mappedBy = "conversationId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @OrderBy("position ASC")
    val turns: MutableList<ConversationTurnEntity> = mutableListOf(),
)
