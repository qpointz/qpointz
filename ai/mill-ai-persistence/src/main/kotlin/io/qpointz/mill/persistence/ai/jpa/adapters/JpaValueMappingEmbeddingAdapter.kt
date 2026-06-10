package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.valuemap.AiValueMappingRecord
import io.qpointz.mill.ai.valuemap.EmbeddingVectorBytes
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.persistence.ai.jpa.entities.AiEmbeddingModelEntity
import io.qpointz.mill.persistence.ai.jpa.entities.AiValueMappingEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * JPA implementation of [ValueMappingEmbeddingRepository].
 */
open class JpaValueMappingEmbeddingAdapter(
    private val modelRepo: AiEmbeddingModelRepository,
    private val valueRepo: AiValueMappingRepository,
) : ValueMappingEmbeddingRepository {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun findEmbeddingModelIdByFingerprint(configFingerprint: String): Long? {
        log.debug("findEmbeddingModelIdByFingerprint fingerprint={}", configFingerprint)
        return modelRepo.findByConfigFingerprint(configFingerprint)?.id
    }

    @Transactional
    override fun ensureEmbeddingModel(
        configFingerprint: String,
        provider: String,
        modelId: String,
        dimension: Int,
        paramsJson: String?,
        label: String?,
    ): Long {
        modelRepo.findByConfigFingerprint(configFingerprint)?.id?.let { existingId ->
            log.info(
                "ensureEmbeddingModel reuse fingerprint={} existingId={}",
                configFingerprint,
                existingId,
            )
            return existingId
        }
        log.info(
            "ensureEmbeddingModel fingerprint={} provider={} modelId={} dimension={}",
            configFingerprint,
            provider,
            modelId,
            dimension,
        )
        val now = Instant.now()
        val entity = AiEmbeddingModelEntity(
            configFingerprint = configFingerprint,
            provider = provider,
            modelId = modelId,
            dimension = dimension,
            paramsJson = paramsJson,
            label = label,
            createdAt = now,
            updatedAt = now,
        )
        return try {
            modelRepo.save(entity).id!!
        } catch (ex: DataIntegrityViolationException) {
            // Another transaction inserted the same fingerprint between find and save (23505 / uq_ai_embedding_model_fingerprint).
            modelRepo.findByConfigFingerprint(configFingerprint)?.id?.also { existingId ->
                log.info(
                    "ensureEmbeddingModel resolved duplicate fingerprint={} existingId={}",
                    configFingerprint,
                    existingId,
                )
            } ?: throw ex
        }
    }

    override fun listValueRowsByAttributeUrn(attributeUrn: String): List<AiValueMappingRecord> {
        log.debug("listValueRowsByAttributeUrn attributeUrn={}", attributeUrn)
        return valueRepo.findAllByAttributeUrn(attributeUrn).map { toRecord(it) }
    }

    override fun findValueRow(attributeUrn: String, content: String): AiValueMappingRecord? {
        log.debug("findValueRow attributeUrn={} contentLen={}", attributeUrn, content.length)
        return valueRepo.findByAttributeUrnAndContent(attributeUrn, content)?.let(::toRecord)
    }

    @Transactional
    override fun upsertValueRow(
        stableId: UUID?,
        attributeUrn: String,
        content: String,
        contentHash: String?,
        embedding: FloatArray?,
        embeddingModelId: Long,
        metadataJson: String?,
    ): UUID {
        log.info(
            "upsertValueRow attributeUrn={} contentLen={} embeddingModelId={} hasEmbedding={}",
            attributeUrn,
            content.length,
            embeddingModelId,
            embedding != null,
        )
        val model = modelRepo.getReferenceById(embeddingModelId)
        val now = Instant.now()
        val existing = valueRepo.findByAttributeUrnAndContent(attributeUrn, content)
        if (existing != null) {
            existing.contentHash = contentHash
            existing.embedding = embedding?.let(EmbeddingVectorBytes::encode)
            existing.embeddingModel = model
            existing.metadataJson = metadataJson
            existing.updatedAt = now
            valueRepo.save(existing)
            return existing.stableId
        }
        val sid = stableId ?: UUID.randomUUID()
        val row = AiValueMappingEntity(
            stableId = sid,
            attributeUrn = attributeUrn,
            content = content,
            contentHash = contentHash,
            embedding = embedding?.let(EmbeddingVectorBytes::encode),
            metadataJson = metadataJson,
            embeddingModel = model,
            createdAt = now,
            updatedAt = now,
        )
        valueRepo.save(row)
        return sid
    }

    @Transactional
    override fun deleteValueRow(stableId: UUID) {
        log.info("deleteValueRow stableId={}", stableId)
        valueRepo.deleteById(stableId)
    }

    private fun toRecord(e: AiValueMappingEntity): AiValueMappingRecord = AiValueMappingRecord(
        stableId = e.stableId,
        attributeUrn = e.attributeUrn,
        content = e.content,
        contentHash = e.contentHash,
        embedding = EmbeddingVectorBytes.decode(e.embedding),
        embeddingModelId = e.embeddingModel.id!!,
        metadataJson = e.metadataJson,
    )
}
