package io.qpointz.mill.persistence.metadata.jpa.listeners

import io.qpointz.mill.metadata.domain.AuditEntry

/**
 * Static bridge so JPA [jakarta.persistence.EntityListeners] (no Spring injection) can append
 * `metadata_audit` rows via a lazily wired callback.
 */
object MetadataAuditBridge {
    @Volatile
    var recorder: ((AuditEntry) -> Unit)? = null
}
