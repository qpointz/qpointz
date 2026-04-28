package io.qpointz.mill.metadata.service

import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataExportFormat
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import java.io.InputStream
import java.time.Instant
import java.util.UUID

/**
 * @param entityRepository used for existence checks and bulk replace
 * @param entityService entity create/update with audit-friendly timestamps
 * @param facetRepository direct saves preserve assignment uids from YAML
 * @param scopeRepository upserts declared scopes
 * @param facetCatalog registers facet type definitions (persists def + runtime `metadata_facet_type` rows)
 */
class DefaultMetadataImportService(
    private val entityRepository: EntityRepository,
    private val entityService: MetadataEntityService,
    private val facetRepository: FacetRepository,
    private val scopeRepository: MetadataScopeRepository,
    private val facetCatalog: FacetCatalog
) : MetadataImportService {

    override fun import(
        inputStream: InputStream,
        mode: ImportMode,
        actorId: String
    ): ImportResult {
        val text = inputStream.bufferedReader().use { it.readText() }
        val doc = MetadataYamlSerializer.deserialize(text)
        val offendingEntityIds = doc.entities
            .map { it.id.trim() }
            .filter { !it.startsWith("urn:mill/", ignoreCase = true) }
            .distinct()
        if (offendingEntityIds.isNotEmpty()) {
            throw MillStatuses.badRequestRuntime(
                "Every entity id must start with urn:mill/. Offending ids: ${offendingEntityIds.joinToString(", ")}"
            )
        }
        val errors = mutableListOf<String>()
        var entitiesImported = 0
        var facetTypesImported = 0

        if (mode == ImportMode.REPLACE) {
            entityRepository.findAll().forEach { entityService.delete(it.id, actorId) }
        }

        val now = Instant.now()

        for (s in doc.scopes) {
            val res = MetadataEntityUrn.canonicalize(s.res)
            val existing = scopeRepository.findByRes(res)
            val toSave = MetadataScope(
                res = res,
                scopeType = s.scopeType,
                referenceId = s.referenceId,
                displayName = s.displayName,
                ownerId = s.ownerId,
                visibility = s.visibility,
                uuid = existing?.uuid ?: UUID.randomUUID().toString(),
                createdAt = existing?.createdAt ?: now,
                createdBy = existing?.createdBy ?: actorId,
                lastModifiedAt = now,
                lastModifiedBy = actorId
            )
            runCatching { scopeRepository.save(toSave) }
                .onFailure { errors += "Scope $res: ${it.message}" }
        }

        for (d in doc.definitions) {
            val key = MetadataEntityUrn.canonicalize(d.typeKey)
            val existing = facetCatalog.findDefinition(key)
            val def = if (existing != null) {
                d.copy(
                    typeKey = key,
                    createdAt = existing.createdAt,
                    createdBy = existing.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = actorId
                )
            } else {
                d.copy(
                    typeKey = key,
                    createdAt = now,
                    createdBy = actorId,
                    lastModifiedAt = now,
                    lastModifiedBy = actorId
                )
            }
            runCatching {
                facetCatalog.registerDefinition(def)
            }
                .onSuccess { facetTypesImported++ }
                .onFailure { errors += "Definition ${d.typeKey}: ${it.message}" }
        }

        for (e in doc.entities) {
            val id = MetadataEntityUrn.canonicalize(e.id)
            runCatching {
                if (entityRepository.exists(id)) {
                    val existing = entityRepository.findById(id)!!
                    entityService.update(
                        MetadataEntity(
                            id = id,
                            kind = e.kind,
                            uuid = existing.uuid,
                            createdAt = existing.createdAt,
                            createdBy = existing.createdBy,
                            lastModifiedAt = now,
                            lastModifiedBy = actorId
                        ),
                        actorId
                    )
                } else {
                    entityService.create(
                        MetadataEntity(
                            id = id,
                            kind = e.kind,
                            uuid = null,
                            createdAt = Instant.EPOCH,
                            createdBy = actorId,
                            lastModifiedAt = Instant.EPOCH,
                            lastModifiedBy = actorId
                        ),
                        actorId
                    )
                }
                entitiesImported++
            }.onFailure { errors += "Entity $id: ${it.message}" }
        }

        for ((rawEid, facets) in doc.facetsByEntity) {
            val eid = MetadataEntityUrn.canonicalize(rawEid)
            if (!entityRepository.exists(eid)) {
                errors += "Skipping facets for missing entity: $eid"
                continue
            }
            for (f in facets) {
                val facetType = MetadataEntityUrn.canonicalize(f.facetTypeKey)
                val scope = MetadataEntityUrn.canonicalize(f.scopeKey)
                runCatching {
                    val existing = facetRepository.findByUid(f.uid)
                    val row = if (existing != null) {
                        f.copy(
                            entityId = eid,
                            facetTypeKey = facetType,
                            scopeKey = scope,
                            createdAt = existing.createdAt,
                            createdBy = existing.createdBy,
                            lastModifiedAt = now,
                            lastModifiedBy = actorId
                        )
                    } else {
                        f.copy(
                            entityId = eid,
                            facetTypeKey = facetType,
                            scopeKey = scope,
                            createdAt = now,
                            createdBy = actorId,
                            lastModifiedAt = now,
                            lastModifiedBy = actorId
                        )
                    }
                    facetRepository.save(row)
                }.onFailure { errors += "Facet ${f.uid} on $eid: ${it.message}" }
            }
        }

        return ImportResult(entitiesImported, facetTypesImported, errors)
    }

    override fun export(scopeQuery: String?, format: MetadataExportFormat): String {
        val facetScopeFilter = MetadataExportFacetScopeFilter.parse(scopeQuery)
        val scopes = scopeRepository.findAll().sortedBy { it.res }
        val definitions = facetCatalog.listDefinitions().sortedBy { it.typeKey }
        val entities = entityService.findAll().sortedBy { MetadataEntityUrn.canonicalize(it.id) }
        val facetsByEntity = entities.associate { e ->
            val id = MetadataEntityUrn.canonicalize(e.id)
            val rows = facetRepository.findByEntity(id)
            val filtered = when (facetScopeFilter) {
                MetadataExportFacetScopeFilter.AllScopes -> rows
                is MetadataExportFacetScopeFilter.Union ->
                    rows.filter { f ->
                        MetadataEntityUrn.canonicalize(f.scopeKey) in facetScopeFilter.scopeUrns
                    }
            }
            id to filtered
        }
        return when (format) {
            MetadataExportFormat.YAML ->
                MetadataYamlSerializer.serialize(
                    scopes = scopes,
                    definitions = definitions,
                    entities = entities,
                    facetsByEntity = facetsByEntity
                )
            MetadataExportFormat.JSON ->
                MetadataYamlSerializer.serializeJson(
                    scopes = scopes,
                    definitions = definitions,
                    entities = entities,
                    facetsByEntity = facetsByEntity
                )
        }
    }
}
