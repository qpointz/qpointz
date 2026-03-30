package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.AuditEntry

/** Append/read `metadata_audit` — SPEC §6.5; listeners only in JPA mode. */
interface MetadataAuditRepository {
    fun record(entry: AuditEntry)

    fun findBySubjectRef(subjectRef: String): List<AuditEntry>

    fun findByActor(actorId: String): List<AuditEntry>
}
