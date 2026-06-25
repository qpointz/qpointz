package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [MetadataContentRepository] for unit tests and non-JPA compositions.
 */
class InMemoryMetadataContentRepository : MetadataContentRepository {

    private val byUrn = ConcurrentHashMap<String, MetadataContent>()

    override fun findByContentUrn(contentUrn: String): MetadataContent? =
        byUrn[MetadataEntityUrn.canonicalize(contentUrn)]

    override fun findByTarget(targetUrn: String, contentKind: String?): List<MetadataContent> {
        val target = MetadataEntityUrn.canonicalize(targetUrn)
        return byUrn.values
            .filter { MetadataEntityUrn.canonicalize(it.targetUrn) == target }
            .filter { contentKind == null || it.contentKind == contentKind }
            .sortedWith(compareBy<MetadataContent> { it.sortOrder }.thenBy { it.contentUrn })
    }

    override fun findAll(): List<MetadataContent> =
        byUrn.values.sortedBy { it.contentUrn }

    override fun save(content: MetadataContent): MetadataContent {
        val urn = MetadataEntityUrn.canonicalize(content.contentUrn)
        val saved = content.copy(contentUrn = urn)
        byUrn[urn] = saved
        return saved
    }
}
