package io.qpointz.mill.persistence.ai.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ai_value_mapping")
class AiValueMappingEntity(
    @Id
    @Column(name = "stable_id", nullable = false)
    var stableId: UUID,

    @Column(name = "attribute_urn", nullable = false, length = 1024)
    var attributeUrn: String = "",

    @Column(name = "content", nullable = false, length = 8192)
    var content: String = "",

    @Column(name = "content_hash", length = 128)
    var contentHash: String? = null,

    @Column(name = "embedding", columnDefinition = "BYTEA")
    var embedding: ByteArray? = null,

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    var metadataJson: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embedding_model_id", nullable = false)
    var embeddingModel: AiEmbeddingModelEntity,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
