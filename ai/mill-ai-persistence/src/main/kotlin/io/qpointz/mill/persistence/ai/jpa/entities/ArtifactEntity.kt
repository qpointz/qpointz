package io.qpointz.mill.persistence.ai.jpa.entities

import io.qpointz.mill.persistence.converters.MapJsonConverter
import io.qpointz.mill.persistence.converters.SetJsonConverter
import io.qpointz.mill.persistence.EntityRef
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "ai_artifact")
class ArtifactEntity(
    @Id
    @Column(name = "artifact_id", nullable = false, length = 255)
    val artifactId: String,

    @Column(name = "conversation_id", nullable = false, length = 255)
    val conversationId: String,

    @Column(name = "run_id", length = 255)
    val runId: String?,

    @Column(name = "turn_id", length = 255)
    val turnId: String?,

    @Column(name = "kind", nullable = false, length = 255)
    val kind: String,

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = MapJsonConverter::class)
    val payloadJson: Map<String, Any?>,

    @Column(name = "pointer_keys_json", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = SetJsonConverter::class)
    val pointerKeysJson: Set<String>,

    @Column(name = "urn", nullable = false, length = 1024)
    override val urn: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
) : EntityRef {
    override val id: String get() = artifactId
    override val type: String get() = "agent/artifact"
}
