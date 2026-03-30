package io.qpointz.mill.persistence.metadata.jpa.listeners

import io.qpointz.mill.metadata.domain.AuditEntry
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityFacetEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeDefEntity
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeInstEntity
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import java.time.Instant

/**
 * Shared fail-open audit hook for greenfield metadata JPA entities (SPEC §8.4).
 */
internal object MetadataJpaAuditSupport {
    /**
     * Forwards a row to [MetadataAuditBridge] when a recorder is registered; swallows failures.
     *
     * @param operation audit operation label
     * @param subjectType coarse subject classification
     * @param subjectRef optional stable subject identifier (URN or uuid)
     * @param actorId optional acting principal
     * @param before JSON snapshot before change, when applicable
     * @param after JSON snapshot or payload after change, when applicable
     */
    fun record(
        operation: String,
        subjectType: String,
        subjectRef: String?,
        actorId: String?,
        before: String?,
        after: String?
    ) {
        runCatching {
            MetadataAuditBridge.recorder?.invoke(
                AuditEntry(
                    operation = operation,
                    subjectType = subjectType,
                    subjectRef = subjectRef,
                    actorId = actorId,
                    correlationId = null,
                    occurredAt = Instant.now(),
                    payloadBefore = before,
                    payloadAfter = after
                )
            )
        }
    }

    /** @param ent persisted entity row */
    fun entityJson(ent: MetadataEntityRecord): String =
        """{"entityRes":"${ent.entityRes}","kind":"${ent.entityKind ?: ""}"}"""

    /** @param ent entity-facet assignment row */
    fun facetJson(ent: MetadataEntityFacetEntity): String =
        """{"uuid":"${ent.uuid}","mergeAction":"${ent.mergeAction}"}"""

    /** @param ent runtime facet type catalog row */
    fun typeInstJson(ent: MetadataFacetTypeInstEntity): String =
        """{"typeRes":"${ent.typeRes}","source":"${ent.source}"}"""
}

/** Audit callbacks for [MetadataEntityRecord]. */
class MetadataEntityRecordAuditListener {

    /** @param ent row inserted into `metadata_entity` */
    @PostPersist
    fun onCreate(ent: MetadataEntityRecord) {
        MetadataJpaAuditSupport.record(
            "ENTITY_CREATED",
            "ENTITY",
            ent.entityRes,
            ent.createdBy,
            null,
            MetadataJpaAuditSupport.entityJson(ent)
        )
    }

    @PostUpdate
    fun onUpdate(ent: MetadataEntityRecord) {
        MetadataJpaAuditSupport.record(
            "ENTITY_UPDATED",
            "ENTITY",
            ent.entityRes,
            ent.lastModifiedBy,
            null,
            MetadataJpaAuditSupport.entityJson(ent)
        )
    }

    @PostRemove
    fun onDelete(ent: MetadataEntityRecord) {
        MetadataJpaAuditSupport.record(
            "ENTITY_DELETED",
            "ENTITY",
            ent.entityRes,
            ent.lastModifiedBy,
            MetadataJpaAuditSupport.entityJson(ent),
            null
        )
    }
}

/** Audit callbacks for [MetadataEntityFacetEntity]. */
class MetadataEntityFacetAuditListener {

    @PostPersist
    fun onCreate(ent: MetadataEntityFacetEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_ASSIGNED",
            "FACET",
            ent.uuid,
            ent.createdBy,
            null,
            MetadataJpaAuditSupport.facetJson(ent)
        )
    }

    @PostUpdate
    fun onUpdate(ent: MetadataEntityFacetEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_UPDATED",
            "FACET",
            ent.uuid,
            ent.lastModifiedBy,
            null,
            MetadataJpaAuditSupport.facetJson(ent)
        )
    }

    @PostRemove
    fun onDelete(ent: MetadataEntityFacetEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_DELETED",
            "FACET",
            ent.uuid,
            ent.lastModifiedBy,
            MetadataJpaAuditSupport.facetJson(ent),
            null
        )
    }
}

/** Audit callbacks for [MetadataFacetTypeDefEntity]. */
class MetadataFacetTypeDefAuditListener {

    @PostPersist
    fun onCreate(ent: MetadataFacetTypeDefEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_DEF_CREATED",
            "FACET_TYPE",
            ent.typeRes,
            ent.createdBy,
            null,
            ent.manifestJson
        )
    }

    @PostUpdate
    fun onUpdate(ent: MetadataFacetTypeDefEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_DEF_UPDATED",
            "FACET_TYPE",
            ent.typeRes,
            ent.lastModifiedBy,
            null,
            ent.manifestJson
        )
    }

    @PostRemove
    fun onDelete(ent: MetadataFacetTypeDefEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_DEF_DELETED",
            "FACET_TYPE",
            ent.typeRes,
            ent.lastModifiedBy,
            ent.manifestJson,
            null
        )
    }
}

/** Audit callbacks for [MetadataFacetTypeInstEntity]. */
class MetadataFacetTypeInstAuditListener {

    @PostPersist
    fun onCreate(ent: MetadataFacetTypeInstEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_CREATED",
            "FACET_TYPE",
            ent.typeRes,
            ent.createdBy,
            null,
            MetadataJpaAuditSupport.typeInstJson(ent)
        )
    }

    @PostUpdate
    fun onUpdate(ent: MetadataFacetTypeInstEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_UPDATED",
            "FACET_TYPE",
            ent.typeRes,
            ent.lastModifiedBy,
            null,
            MetadataJpaAuditSupport.typeInstJson(ent)
        )
    }

    @PostRemove
    fun onDelete(ent: MetadataFacetTypeInstEntity) {
        MetadataJpaAuditSupport.record(
            "FACET_TYPE_DELETED",
            "FACET_TYPE",
            ent.typeRes,
            ent.lastModifiedBy,
            MetadataJpaAuditSupport.typeInstJson(ent),
            null
        )
    }
}
