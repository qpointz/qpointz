package io.qpointz.mill.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface RelationRecordRepository : JpaRepository<RelationRecord, String> {
    fun findByRelationKindAndSourceIdAndSourceType(
        relationKind: String,
        sourceId: String,
        sourceType: String,
    ): List<RelationRecord>

    fun findByRelationKindAndTargetIdAndTargetType(
        relationKind: String,
        targetId: String,
        targetType: String,
    ): List<RelationRecord>
}
