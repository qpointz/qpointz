package io.qpointz.mill.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "mill_schema_info")
class SchemaInfoEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 64)
    val lane: String,

    @Column(nullable = false, length = 255)
    val description: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)
