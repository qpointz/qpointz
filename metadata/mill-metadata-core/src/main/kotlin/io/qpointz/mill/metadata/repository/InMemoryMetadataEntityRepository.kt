package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [MetadataEntityRepository] for tests and local bootstraps without JPA.
 *
 * Keys are canonical entity URNs ([MetadataEntityUrn.canonicalize]).
 */
class InMemoryMetadataEntityRepository : MetadataEntityRepository {

    private val byId = ConcurrentHashMap<String, MetadataEntity>()

    override fun findById(id: String): MetadataEntity? =
        byId[MetadataEntityUrn.canonicalize(id)]

    override fun findAll(): List<MetadataEntity> =
        byId.values.sortedBy { it.id }

    override fun findByKind(kind: String): List<MetadataEntity> =
        byId.values.filter { it.kind?.equals(kind, ignoreCase = true) == true }

    override fun exists(id: String): Boolean =
        byId.containsKey(MetadataEntityUrn.canonicalize(id))

    override fun save(entity: MetadataEntity): MetadataEntity {
        val k = MetadataEntityUrn.canonicalize(entity.id)
        val row = entity.copy(id = k)
        byId[k] = row
        return row
    }

    override fun delete(id: String) {
        byId.remove(MetadataEntityUrn.canonicalize(id))
    }

    override fun deleteAll() {
        byId.clear()
    }
}
