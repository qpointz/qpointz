package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "chat_memory")
class ChatMemoryEntity(
    @Id
    @Column(name = "conversation_id", nullable = false, length = 255)
    val conversationId: String,

    @Column(name = "profile_id", nullable = false, length = 255)
    val profileId: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,

    @OneToMany(
        mappedBy = "conversationId",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @OrderBy("position ASC")
    val messages: MutableList<ChatMemoryMessageEntity> = mutableListOf(),
)
