package io.qpointz.mill.metadata.domain

import java.time.Instant

/**
 * One append-only metadata operation audit row (`metadata_audit`) — SPEC §6.5 / §8.4.
 *
 * @property operation e.g. ENTITY_CREATED, FACET_ASSIGNED
 * @property subjectType ENTITY, FACET, or FACET_TYPE
 * @property subjectRef entity URN, assignment uuid, or type URN
 * @property actorId optional actor
 * @property correlationId optional trace id
 * @property occurredAt event time
 * @property payloadBefore JSON snapshot before change, or null
 * @property payloadAfter JSON snapshot after change, or null
 */
data class AuditEntry(
    val operation: String,
    val subjectType: String,
    val subjectRef: String?,
    val actorId: String?,
    val correlationId: String?,
    val occurredAt: Instant,
    val payloadBefore: String?,
    val payloadAfter: String?
)
