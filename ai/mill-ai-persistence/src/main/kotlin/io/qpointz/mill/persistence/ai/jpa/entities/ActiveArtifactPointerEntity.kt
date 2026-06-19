package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Embeddable
data class ActiveArtifactPointerKey(
    @Column(name = "chat_id")
    val chatId: String = "",
    @Column(name = "pointer_key")
    val pointerKey: String = "",
) : Serializable

@Entity
@Table(name = "ai_chat_artifact_pointer")
class ActiveArtifactPointerEntity(
    @EmbeddedId
    val id: ActiveArtifactPointerKey,

    @Column(name = "artifact_id", nullable = false, length = 255)
    val artifactId: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
