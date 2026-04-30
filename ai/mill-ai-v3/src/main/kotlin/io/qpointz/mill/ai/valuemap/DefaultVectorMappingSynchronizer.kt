package io.qpointz.mill.ai.valuemap

import tools.jackson.databind.json.JsonMapper
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Default WI-179 implementation (normative routine).
 */
class DefaultVectorMappingSynchronizer(
    private val repository: ValueMappingEmbeddingRepository,
    private val harness: EmbeddingHarness,
    private val embeddingStore: EmbeddingStore<TextSegment>,
) : VectorMappingSynchronizer {

    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = JsonMapper.builder().build()

    override fun sync(
        attributeUrn: String,
        entries: List<AttributeValueEntry>,
        embeddingModelId: Long,
        progress: ValueMappingSyncProgressCallback?,
    ) {
        val desiredContents = entries.map { it.content }.toSet()
        val existing = repository.listValueRowsByAttributeUrn(attributeUrn)
        for (row in existing) {
            if (row.content !in desiredContents) {
                removeFromStore(row.stableId)
                repository.deleteValueRow(row.stableId)
            }
        }
        var successCount = 0
        var failureCount = 0
        entries.forEachIndexed { index, entry ->
            try {
                processEntry(attributeUrn, entry, embeddingModelId)
                successCount++
                progress?.onElementProcessed(attributeUrn, index, entry.content, true, null)
            } catch (ex: Exception) {
                failureCount++
                log.warn("sync entry failed attributeUrn={} contentLen={}: {}", attributeUrn, entry.content.length, ex.message)
                progress?.onElementProcessed(attributeUrn, index, entry.content, false, ex.message)
            }
        }
        progress?.onRunComplete(attributeUrn, successCount, failureCount)
    }

    private fun processEntry(attributeUrn: String, entry: AttributeValueEntry, embeddingModelId: Long) {
        var row = repository.findValueRow(attributeUrn, entry.content)
        val needReembed = row == null || row.embeddingModelId != embeddingModelId || row.embedding == null
        val stableId: UUID
        val vector: FloatArray
        if (needReembed) {
            vector = harness.embed(entry.content)
            stableId = repository.upsertValueRow(
                row?.stableId,
                attributeUrn,
                entry.content,
                null,
                vector,
                embeddingModelId,
                metadataJson(entry),
            )
        } else {
            stableId = row!!.stableId
            vector = row.embedding!!
        }
        removeFromStore(stableId)
        val metadata = Metadata()
        metadata.put("column_name", attributeUrn)
        metadata.put("original_value", entry.content)
        metadata.put("stable_id", stableId.toString())
        entry.metadata.forEach { (k, v) -> metadata.put(k, v) }
        val segment = TextSegment.from(entry.content, metadata)
        embeddingStore.add(Embedding.from(vector), segment)
    }

    private fun removeFromStore(stableId: UUID) {
        try {
            embeddingStore.remove(stableId.toString())
        } catch (ex: Exception) {
            log.debug("embedding store remove id={}: {}", stableId, ex.message)
        }
    }

    private fun metadataJson(entry: AttributeValueEntry): String? {
        if (entry.metadata.isEmpty()) {
            return null
        }
        return objectMapper.writeValueAsString(entry.metadata)
    }
}
