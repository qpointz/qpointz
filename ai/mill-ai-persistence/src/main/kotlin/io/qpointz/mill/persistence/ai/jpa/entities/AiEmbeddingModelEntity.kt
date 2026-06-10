package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "ai_embedding_model")
class AiEmbeddingModelEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @Column(name = "config_fingerprint", nullable = false, length = 512, unique = true)
    var configFingerprint: String = "",

    @Column(name = "provider", nullable = false, length = 64)
    var provider: String = "",

    @Column(name = "model_id", nullable = false, length = 256)
    var modelId: String = "",

    @Column(name = "dimension", nullable = false)
    var dimension: Int = 0,

    @Column(name = "params_json", columnDefinition = "TEXT")
    var paramsJson: String? = null,

    @Column(name = "label", length = 256)
    var label: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
