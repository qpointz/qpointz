package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.facet.FacetInstance

/** Write-side metadata orchestration for REST controllers. */
interface MetadataEditService {

    /** @param entity domain entity to insert */
    fun createEntity(entity: MetadataEntity, actor: String): MetadataEntity

    /** @param id canonical entity id being replaced */
    fun overwriteEntity(id: String, entity: MetadataEntity, actor: String): MetadataEntity

    /** @param id canonical entity id */
    fun deleteEntity(id: String, actor: String)

    /**
     * @param id entity URN
     * @param typeKey facet type slug or URN
     * @param scope raw scope slug or URN (caller-normalised separately when needed)
     * @param payload facet JSON payload
     * @param actor authenticated principal
     * @return persisted assignment row
     */
    fun setFacet(id: String, typeKey: String, scope: String, payload: Any?, actor: String): FacetInstance

    /** @param typeKey facet type slug or URN */
    fun deleteFacet(id: String, typeKey: String, scope: String, actor: String)

    /** @param facetUid stable assignment UUID */
    fun deleteFacetInstanceByUid(id: String, facetUid: String, actor: String)

    /**
     * @param id entity URN
     * @return audit rows whose subject matches the entity or its facet assignments
     */
    fun history(id: String): List<MetadataHistoryRecord>
}

/**
 * Normalised audit projection for REST history endpoints.
 *
 * @property auditId synthetic stable string (operation + time + subject) when persistence has no numeric id
 */
data class MetadataHistoryRecord(
    val auditId: String,
    val operationType: String,
    val entityId: String?,
    val facetType: String?,
    val scopeKey: String?,
    val actorId: String,
    val occurredAt: java.time.Instant,
    val payloadBefore: String?,
    val payloadAfter: String?,
    val changeSummary: String?
)
