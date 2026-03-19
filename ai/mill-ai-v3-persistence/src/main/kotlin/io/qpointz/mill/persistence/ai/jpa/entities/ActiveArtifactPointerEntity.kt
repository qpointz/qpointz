package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Embeddable
data class ActiveArtifactPointerKey(
    val conversationId: String = "",
    val pointerKey: String = "",
) : Serializable

@Entity
@Table(name = "ai_active_artifact_pointer")
class ActiveArtifactPointerEntity(
    @EmbeddedId
    val id: ActiveArtifactPointerKey,

    @Column(name = "artifact_id", nullable = false, length = 255)
    val artifactId: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
