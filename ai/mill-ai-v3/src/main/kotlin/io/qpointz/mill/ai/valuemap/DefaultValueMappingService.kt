package io.qpointz.mill.ai.valuemap

import org.slf4j.LoggerFactory

/**
 * [ValueMappingService] implementation (dedupe + delegate).
 */
class DefaultValueMappingService(
    private val synchronizer: VectorMappingSynchronizer,
) : ValueMappingService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun syncAttribute(attributeUrn: String, entries: List<AttributeValueEntry>, embeddingModelId: Long) {
        val lastWins = LinkedHashMap<String, AttributeValueEntry>()
        for (e in entries) {
            lastWins[e.content] = e
        }
        log.info(
            "syncAttribute attributeUrn={} uniqueEntries={} embeddingModelId={}",
            attributeUrn,
            lastWins.size,
            embeddingModelId,
        )
        synchronizer.sync(attributeUrn, lastWins.values.toList(), embeddingModelId)
    }
}
