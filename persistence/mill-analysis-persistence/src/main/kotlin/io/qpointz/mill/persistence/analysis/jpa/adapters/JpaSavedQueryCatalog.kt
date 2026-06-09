package io.qpointz.mill.persistence.analysis.jpa.adapters

import io.qpointz.mill.analysis.queries.SavedQuery
import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import io.qpointz.mill.persistence.analysis.jpa.entities.SavedQueryEntity
import io.qpointz.mill.persistence.analysis.jpa.repositories.SavedQueryJpaRepository
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/**
 * JPA-backed {@link SavedQueryCatalog} adapter.
 *
 * @param repository Spring Data repository for {@code saved_query}
 * @param jsonMapper JSON mapper for {@code tags_json} column
 */
open class JpaSavedQueryCatalog(
    private val repository: SavedQueryJpaRepository,
    private val jsonMapper: JsonMapper,
) : SavedQueryCatalog {

    override fun findAll(): List<SavedQuery> =
        repository.findAllByOrderByUpdatedAtDesc().map { it.toDomain() }

    override fun findById(id: String): SavedQuery? =
        repository.findById(id).orElse(null)?.toDomain()

    override fun save(query: SavedQuery): SavedQuery {
        val tagsJson = encodeTags(query.tags)
        val entity = repository.findById(query.id).orElse(null)
        val persisted = if (entity == null) {
            SavedQueryEntity(
                id = query.id,
                name = query.name,
                description = query.description,
                sqlText = query.sql,
                createdAt = query.createdAt,
                updatedAt = query.updatedAt,
                tagsJson = tagsJson,
            )
        } else {
            entity.apply {
                name = query.name
                description = query.description
                sqlText = query.sql
                updatedAt = query.updatedAt
                this.tagsJson = tagsJson
            }
        }
        return repository.save(persisted).toDomain()
    }

    override fun deleteById(id: String): Boolean {
        if (!repository.existsById(id)) {
            return false
        }
        repository.deleteById(id)
        return true
    }

    private fun encodeTags(tags: List<String>): String? =
        if (tags.isEmpty()) null else jsonMapper.writeValueAsString(tags)

    private fun SavedQueryEntity.toDomain(): SavedQuery = SavedQuery(
        id = id,
        name = name,
        description = description,
        sql = sqlText,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = parseTags(tagsJson),
    )

    private fun parseTags(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return jsonMapper.readValue<List<String>>(json)
    }
}
