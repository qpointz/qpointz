package io.qpointz.mill.persistence.analysis.jpa.repositories

import io.qpointz.mill.persistence.analysis.jpa.entities.SavedQueryEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data access for {@link SavedQueryEntity}.
 */
interface SavedQueryJpaRepository : JpaRepository<SavedQueryEntity, String> {

    /**
     * @return all rows ordered by {@code updated_at} descending
     */
    fun findAllByOrderByUpdatedAtDesc(): List<SavedQueryEntity>
}
