package io.qpointz.mill.persistence.ai.jpa.entities

import io.qpointz.mill.persistence.converters.MapJsonConverter
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "ai_run_event")
class RunEventEntity(
    @Id
    @Column(name = "event_id", nullable = false, length = 255)
    val eventId: String,

    @Column(name = "run_id", nullable = false, length = 255)
    val runId: String,

    @Column(name = "conversation_id", length = 255)
    val conversationId: String?,

    @Column(name = "profile_id", nullable = false, length = 255)
    val profileId: String,

    @Column(name = "runtime_type", nullable = false, length = 255)
    val runtimeType: String,

    @Column(name = "kind", nullable = false, length = 255)
    val kind: String,

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = MapJsonConverter::class)
    val contentJson: Map<String, Any?>,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
