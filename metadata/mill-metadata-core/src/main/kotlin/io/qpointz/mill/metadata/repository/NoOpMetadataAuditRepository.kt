package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.AuditEntry

/** No-op [MetadataAuditRepository] (tests / file mode without audit). */
class NoOpMetadataAuditRepository : MetadataAuditRepository {
    override fun record(entry: AuditEntry) = Unit
    override fun findBySubjectRef(subjectRef: String): List<AuditEntry> = emptyList()
    override fun findByActor(actorId: String): List<AuditEntry> = emptyList()
}
