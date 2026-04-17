package io.qpointz.mill.ai.valuemap

import org.slf4j.LoggerFactory

/**
 * [ValueMappingService] implementation (dedupe + delegate).
 *
 * @param maxContentLength max [String.length] for [AttributeValueEntry.content] before embed (WI-181).
 */
class DefaultValueMappingService(
    private val synchronizer: VectorMappingSynchronizer,
    private val maxContentLength: Int = DEFAULT_MAX_CONTENT_LENGTH,
) : ValueMappingService {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        require(maxContentLength > 0) { "maxContentLength must be positive" }
    }

    override fun syncAttribute(attributeUrn: String, entries: List<AttributeValueEntry>, embeddingModelId: Long) {
        val lastWins = dedupeLastWins(entries.map { truncate(it) })
        log.info(
            "syncAttribute attributeUrn={} uniqueEntries={} embeddingModelId={}",
            attributeUrn,
            lastWins.size,
            embeddingModelId,
        )
        synchronizer.sync(attributeUrn, lastWins, embeddingModelId, null)
    }

    override fun syncFromSource(
        attributeUrn: String,
        source: ValueSource,
        embeddingModelId: Long,
        progress: ValueMappingSyncProgressCallback?,
    ) {
        val raw = source.provideEntries().map { truncate(it) }
        val lastWins = dedupeLastWins(raw)
        log.info(
            "syncFromSource attributeUrn={} uniqueEntries={} embeddingModelId={}",
            attributeUrn,
            lastWins.size,
            embeddingModelId,
        )
        progress?.onBegin(attributeUrn, lastWins.size)
        synchronizer.sync(attributeUrn, lastWins, embeddingModelId, progress)
    }

    private fun dedupeLastWins(entries: List<AttributeValueEntry>): List<AttributeValueEntry> {
        val lastWins = LinkedHashMap<String, AttributeValueEntry>()
        for (e in entries) {
            lastWins[e.content] = e
        }
        return lastWins.values.toList()
    }

    private fun truncate(entry: AttributeValueEntry): AttributeValueEntry {
        if (entry.content.length <= maxContentLength) {
            return entry
        }
        val truncated = entry.content.substring(0, maxContentLength)
        log.debug(
            "truncated embedding line attribute content from {} to {}",
            entry.content.length,
            maxContentLength,
        )
        return AttributeValueEntry(truncated, entry.metadata)
    }

    companion object {
        const val DEFAULT_MAX_CONTENT_LENGTH: Int = 2048
    }
}
