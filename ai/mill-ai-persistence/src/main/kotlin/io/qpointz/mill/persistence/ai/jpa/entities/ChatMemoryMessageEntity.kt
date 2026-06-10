package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*

@Entity
@Table(name = "chat_memory_message")
class ChatMemoryMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "conversation_id", nullable = false, length = 255)
    val conversationId: String,

    @Column(name = "position", nullable = false)
    val position: Int,

    @Column(name = "role", nullable = false, length = 32)
    val role: String,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "tool_call_id", length = 255)
    val toolCallId: String? = null,

    @Column(name = "tool_name", length = 255)
    val toolName: String? = null,
)
