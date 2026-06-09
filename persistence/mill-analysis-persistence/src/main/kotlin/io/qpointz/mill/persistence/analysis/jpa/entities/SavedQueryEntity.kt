package io.qpointz.mill.persistence.analysis.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA row for {@code saved_query} (WI-256).
 */
@Entity
@Table(name = "saved_query")
class SavedQueryEntity(
    @Id
    @Column(name = "id", nullable = false, length = 128)
    var id: String,

    @Column(name = "name", nullable = false, length = 512)
    var name: String,

    @Column(name = "description", length = 2048)
    var description: String? = null,

    @Column(name = "sql_text", nullable = false, columnDefinition = "TEXT")
    var sqlText: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    /** JSON array of tag strings, e.g. {@code ["revenue","customer"]}. */
    @Column(name = "tags_json", columnDefinition = "TEXT")
    var tagsJson: String? = null,
)
