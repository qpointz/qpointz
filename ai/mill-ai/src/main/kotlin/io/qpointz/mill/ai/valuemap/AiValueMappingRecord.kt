package io.qpointz.mill.ai.valuemap

import java.util.UUID

/**
 * One value row in `ai_value_mapping` (golden-source vector payload for an attribute value).
 */
data class AiValueMappingRecord(
    val stableId: UUID,
    val attributeUrn: String,
    val content: String,
    val contentHash: String?,
    val embedding: FloatArray?,
    val embeddingModelId: Long,
    val metadataJson: String?,
)
