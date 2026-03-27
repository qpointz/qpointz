package io.qpointz.mill.metadata.api

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.api.dto.FacetResponseDto
import io.qpointz.mill.metadata.api.dto.MetadataAuditRecordDto
import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.MetadataFacetInstanceRow
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.metadata.service.MetadataEditService
import io.qpointz.mill.metadata.service.MetadataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * Read-only REST controller for metadata entity discovery and facet resolution.
 *
 * Facet type keys in path variables are normalised via [MetadataUrns.normaliseFacetTypePath],
 * accepting prefixed slugs (e.g. `descriptive`), legacy short keys, or full URNs.
 *
 * The `context` query parameter is a comma-separated list of scope prefixed slugs
 * (e.g. `global`, `global,user:alice`). Omitting `context` defaults to the global scope.
 */
@Tag(name = "metadata-entities", description = "Read-only metadata entity and facet resolution endpoints")
@RestController
@RequestMapping("/api/v1/metadata/entities")
class MetadataEntityController(
    private val metadataService: MetadataService,
    private val metadataEditService: MetadataEditService,
    private val metadataRepository: MetadataRepository,
    private val schemaProvider: SchemaProvider? = null
) {

    /**
     * Lists metadata entities with optional filtering by schema and/or table coordinates.
     *
     * @param schema  optional schema name filter
     * @param table   optional table name filter (requires [schema])
     * @param context comma-separated scope slugs for facet resolution; defaults to global
     * @return list of matching entities with scope-resolved facets
     */
    @Operation(
        summary = "List metadata entities",
        description = "Returns all entities, optionally filtered by schema and table. " +
            "Facets are included as stored; use the /facets endpoint for scope-merged views."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Entities returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = MetadataEntityDto::class)))])
    )
    @GetMapping
    fun listEntities(
        @Parameter(description = "Filter by schema name") @RequestParam(required = false) schema: String?,
        @Parameter(description = "Filter by table name") @RequestParam(required = false) table: String?,
        @Parameter(description = "Comma-separated scope slugs, e.g. global,user:alice")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<List<MetadataEntityDto>> {
        val ctx = parseContext(context)
        var entities = metadataService.findAll()
        if (schema != null) entities = entities.filter { it.schemaName == schema }
        if (table != null) entities = entities.filter { it.tableName == table }
        val dtos = entities.map { toDto(it, ctx) }
        return ResponseEntity.ok(dtos)
    }

    /**
     * Returns a single entity by its identifier.
     *
     * @param id      entity identifier string
     * @param context comma-separated scope slugs; defaults to global
     * @return 200 with the entity DTO, or 404 if not found
     */
    @Operation(
        summary = "Get entity by id",
        description = "Returns the entity with the given identifier. " +
            "The context parameter controls which scopes are included in the response."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Entity found",
            content = [Content(schema = Schema(implementation = MetadataEntityDto::class))]),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @GetMapping("/{id}")
    fun getEntityById(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<MetadataEntityDto> {
        val ctx = parseContext(context)
        return metadataService.findById(id)
            .map { ResponseEntity.ok(toDto(it, ctx)) }
            .orElse(ResponseEntity.notFound().build())
    }

    /**
     * Returns all facets of an entity merged across the requested context.
     *
     * For each facet type present on the entity, the last matching scope in the context
     * wins. Facet types with no data under any scope in the context are omitted from the result.
     *
     * The response is a **JSON array** of [FacetResponseDto] (not a map) so clients can preserve
     * order and, in future, surface multiple entries with the same [FacetResponseDto.facetType]
     * when the domain supports several facet instances per type.
     *
     * @param id      entity identifier string
     * @param context comma-separated scope slugs; defaults to global
     * @return ordered list of facet envelopes, or 404 if entity not found
     */
    @Operation(
        summary = "Get all merged facets for an entity",
        description = "Returns one list element per facet type that has data under any scope in the " +
            "requested context. Entries are sorted by facet type URN. " +
            "The last matching scope in the context wins for each facet type."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "JSON array of facet envelopes, sorted by facetType URN. " +
                "Not a map: duplicate facetType values are allowed for future multi-instance facets.",
            content = [Content(array = ArraySchema(schema = Schema(implementation = FacetResponseDto::class)))]
        ),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @GetMapping("/{id}/facets")
    fun getEntityFacets(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<List<FacetResponseDto>> {
        val ctx = parseContext(context)
        val entityOpt = metadataService.findById(id)
        if (entityOpt.isEmpty) return ResponseEntity.notFound().build()
        val instanceRows = metadataRepository.listFacetInstanceRows(id)
        if (instanceRows.isNotEmpty()) {
            return ResponseEntity.ok(buildFacetResponsesFromInstanceRows(instanceRows, ctx))
        }
        val entity = entityOpt.get()
        val result = mutableListOf<FacetResponseDto>()
        for (facetType in entity.facets.keys.sorted()) {
            val payload = resolveForContext(entity.facets[facetType] ?: continue, ctx)
            if (payload != null) {
                result.add(FacetResponseDto(facetType = facetType, uid = null, payload = payload))
            }
        }
        return ResponseEntity.ok(result)
    }

    /**
     * Returns a single facet type's merged payload for an entity.
     *
     * @param id      entity identifier string
     * @param typeKey facet type as a prefixed slug (e.g. `descriptive`) or full URN
     * @param context comma-separated scope slugs; defaults to global
     * @return [FacetResponseDto] with the merged payload, or 404 if entity or facet not found
     */
    @Operation(
        summary = "Get merged facet by type for an entity",
        description = "Returns the merged payload for the specified facet type using the " +
            "given context. typeKey may be a prefixed slug (e.g. descriptive) or a full URN."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Facet returned",
            content = [Content(schema = Schema(implementation = FacetResponseDto::class))]),
        ApiResponse(responseCode = "404", description = "Entity or facet type not found")
    )
    @GetMapping("/{id}/facets/{typeKey}")
    fun getEntityFacetByType(
        @Parameter(description = "Entity identifier") @PathVariable id: String,
        @Parameter(description = "Facet type slug or URN, e.g. descriptive")
        @PathVariable typeKey: String,
        @Parameter(description = "Comma-separated scope slugs")
        @RequestParam(required = false) context: String?
    ): ResponseEntity<FacetResponseDto> {
        val ctx = parseContext(context)
        val normType = MetadataUrns.normaliseFacetTypePath(typeKey)
        val entityOpt = metadataService.findById(id)
        if (entityOpt.isEmpty) return ResponseEntity.notFound().build()
        val entity = entityOpt.get()
        val scopeMap = entity.facets[normType] ?: return ResponseEntity.notFound().build()
        val payload = resolveForContext(scopeMap, ctx) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(FacetResponseDto(facetType = normType, uid = null, payload = payload))
    }

    @PostMapping
    fun createEntity(@RequestBody dto: MetadataEntityDto): ResponseEntity<MetadataEntityDto> {
        val actor = requireAuthenticatedActor()
        val created = metadataEditService.createEntity(toDomain(dto), actor)
        return ResponseEntity.created(URI.create("/api/v1/metadata/entities/${created.id}")).body(toDto(created, MetadataContext.global()))
    }

    @PutMapping("/{id}")
    fun overwriteEntity(
        @PathVariable id: String,
        @RequestBody dto: MetadataEntityDto
    ): ResponseEntity<MetadataEntityDto> {
        val actor = requireAuthenticatedActor()
        val updated = metadataEditService.overwriteEntity(id, toDomain(dto), actor)
        return ResponseEntity.ok(toDto(updated, MetadataContext.global()))
    }

    @PatchMapping("/{id}")
    fun patchEntityOverwrite(
        @PathVariable id: String,
        @RequestBody dto: MetadataEntityDto
    ): ResponseEntity<MetadataEntityDto> = overwriteEntity(id, dto)

    @DeleteMapping("/{id}")
    fun deleteEntity(@PathVariable id: String): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        metadataEditService.deleteEntity(id, actor)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}/facets/{typeKey}")
    fun overwriteFacet(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @RequestParam(required = false, name = "context") context: String?,
        @RequestBody payload: Any?
    ): ResponseEntity<FacetResponseDto> {
        val actor = requireAuthenticatedActor()
        ensureMetadataEntityExistsForFacetWrite(id, actor)
        val scope = parseContext(context).scopes.last()
        requireScopeWriteAllowed(scope, actor)
        val updated = metadataEditService.setFacet(id, typeKey, scope, payload, actor)
        val normalizedType = MetadataUrns.normaliseFacetTypePath(typeKey)
        val normalizedScope = MetadataUrns.normaliseScopePath(scope)
        val scoped = updated.facets[normalizedType] ?: emptyMap()
        val facetPayload = scoped[normalizedScope]
            ?: throw MillStatuses.unprocessableRuntime("Facet payload validation failed")
        return ResponseEntity.ok(FacetResponseDto(facetType = normalizedType, uid = null, payload = facetPayload))
    }

    @DeleteMapping("/{id}/facets/{typeKey}")
    fun deleteFacet(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @RequestParam(required = false, name = "context") context: String?,
        @Parameter(description = "Facet instance row UUID; required when deleting one of several MULTIPLE instances")
        @RequestParam(required = false, name = "uid") uid: String?
    ): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        val ctx = parseContext(context)
        val normType = MetadataUrns.normaliseFacetTypePath(typeKey)
        val entity = metadataService.findById(id).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $id")
        }
        val scopeMap = entity.facets[normType] ?: throw MillStatuses.notFoundRuntime(
            "Facet type not found on entity: $normType"
        )
        val scope = winningScopeForContext(scopeMap, ctx) ?: throw MillStatuses.notFoundRuntime(
            "Facet not found in the requested context"
        )
        requireScopeWriteAllowed(scope, actor)
        val jpaRows = metadataRepository.listFacetInstanceRows(id).isNotEmpty()
        if (jpaRows) {
            when (metadataRepository.resolveFacetTargetCardinality(normType)) {
                FacetTargetCardinality.SINGLE -> metadataEditService.deleteFacet(id, typeKey, scope, actor)
                FacetTargetCardinality.MULTIPLE -> {
                    val count = metadataRepository.countFacetInstancesAtScope(id, normType, scope)
                    when {
                        count > 1 && uid.isNullOrBlank() -> throw MillStatuses.badRequestRuntime(
                            "Query parameter uid is required when multiple facet instances exist"
                        )
                        count >= 1 && !uid.isNullOrBlank() -> {
                            val row = metadataRepository.findFacetInstanceRow(id, uid.trim())
                                ?: throw MillStatuses.notFoundRuntime("Facet instance not found: ${uid.trim()}")
                            if (row.facetTypeKey != normType || row.scopeKey != scope) {
                                throw MillStatuses.notFoundRuntime(
                                    "Facet instance does not match this facet type and context"
                                )
                            }
                            metadataEditService.deleteFacetInstanceByUid(id, uid.trim(), actor)
                        }
                        else -> metadataEditService.deleteFacet(id, typeKey, scope, actor)
                    }
                }
            }
        } else {
            metadataEditService.deleteFacet(id, typeKey, scope, actor)
        }
        return ResponseEntity.noContent().build()
    }

    /**
     * Deletes a single facet instance by stable row UUID (same as `DELETE .../facets/{typeKey}?uid=`).
     *
     * @param id       entity identifier
     * @param facetUid facet row UUID from [FacetResponseDto.uid]
     */
    @DeleteMapping("/{id}/facet-instances/{facetUid}")
    fun deleteFacetInstanceByUid(
        @PathVariable id: String,
        @PathVariable facetUid: String
    ): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        val row = metadataRepository.findFacetInstanceRow(id, facetUid)
            ?: throw MillStatuses.notFoundRuntime("Facet instance not found: $facetUid")
        requireScopeWriteAllowed(row.scopeKey, actor)
        metadataEditService.deleteFacetInstanceByUid(id, facetUid, actor)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/history")
    fun getEntityHistory(@PathVariable id: String): ResponseEntity<List<MetadataAuditRecordDto>> {
        requireAuthenticatedActor()
        val rows = metadataEditService.history(id).map {
            MetadataAuditRecordDto(
                auditId = it.auditId,
                operationType = it.operationType,
                entityId = it.entityId,
                facetType = it.facetType,
                scopeKey = it.scopeKey,
                actorId = it.actorId,
                occurredAt = it.occurredAt,
                payloadBefore = it.payloadBefore,
                payloadAfter = it.payloadAfter,
                changeSummary = it.changeSummary
            )
        }
        return ResponseEntity.ok(rows)
    }

    /**
     * Converts a domain entity to its REST DTO representation.
     *
     * @param entity the domain entity to convert
     * @param ctx    the scope context; unused in this read-only view (full facets are returned)
     * @return [MetadataEntityDto] with all facets
     */
    private fun toDto(
        entity: io.qpointz.mill.metadata.domain.MetadataEntity,
        @Suppress("UNUSED_PARAMETER") ctx: MetadataContext
    ): MetadataEntityDto = MetadataEntityDto(
        id = entity.id,
        type = entity.type,
        schemaName = entity.schemaName,
        tableName = entity.tableName,
        attributeName = entity.attributeName,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        createdBy = entity.createdBy,
        updatedBy = entity.updatedBy,
        facets = entity.facets.mapValues { (_, v) -> v as Any? }
    )

    /**
     * Resolves a scope map using the context's ordered scope list (last wins).
     *
     * @param scopeMap the map of scope URN → payload for a single facet type
     * @param ctx      the scope context to apply
     * @return the resolved payload, or `null` if no scope in the context has data
     */
    private fun resolveForContext(scopeMap: Map<String, Any?>, ctx: MetadataContext): Any? {
        var result: Any? = null
        for (scope in ctx.scopes) {
            scopeMap[scope]?.let { result = it }
        }
        return result
    }

    /**
     * Returns the scope key whose payload [resolveForContext] would choose for the same inputs.
     *
     * Used for facet deletion so the removed row matches what merged GET endpoints surface.
     */
    private fun winningScopeForContext(scopeMap: Map<String, Any?>, ctx: MetadataContext): String? {
        var winningScope: String? = null
        for (scope in ctx.scopes) {
            scopeMap[scope]?.let { winningScope = scope }
        }
        return winningScope
    }

    private fun buildFacetResponsesFromInstanceRows(
        rows: List<MetadataFacetInstanceRow>,
        ctx: MetadataContext
    ): List<FacetResponseDto> {
        val byFacetType = rows.groupBy { it.facetTypeKey }
        val out = mutableListOf<FacetResponseDto>()
        for (facetType in byFacetType.keys.sorted()) {
            val rowsForType = byFacetType[facetType] ?: continue
            val byScope = rowsForType.groupBy { it.scopeKey }
            val winning = winningFacetRowsForContext(byScope, ctx) ?: continue
            for (r in winning.sortedBy { it.sortKey }) {
                out.add(FacetResponseDto(facetType = facetType, uid = r.facetUid, payload = r.payload))
            }
        }
        return out
    }

    private fun winningFacetRowsForContext(
        rowsByScope: Map<String, List<MetadataFacetInstanceRow>>,
        ctx: MetadataContext
    ): List<MetadataFacetInstanceRow>? {
        var winner: List<MetadataFacetInstanceRow>? = null
        for (scope in ctx.scopes) {
            rowsByScope[scope]?.takeIf { it.isNotEmpty() }?.let { winner = it }
        }
        return winner
    }

    /**
     * Parses raw `context` query parameter using metadata-core rules and converts malformed input
     * to a BAD_REQUEST status exception for consistent HTTP 400 mapping.
     *
     * @param context raw comma-separated context query parameter
     * @return parsed [MetadataContext] with normalized scope URNs
     */
    private fun parseContext(context: String?): MetadataContext = try {
        MetadataContext.parse(context)
    } catch (ex: IllegalArgumentException) {
        throw MillStatuses.badRequestRuntime("Malformed context parameter: ${context ?: "<blank>"}")
    }

    private fun toDomain(dto: MetadataEntityDto): io.qpointz.mill.metadata.domain.MetadataEntity =
        io.qpointz.mill.metadata.domain.MetadataEntity(
            id = dto.id,
            type = dto.type,
            schemaName = dto.schemaName,
            tableName = dto.tableName,
            attributeName = dto.attributeName,
            facets = (dto.facets as? Map<String, Map<String, Any?>>)?.mapValues { it.value.toMutableMap() }?.toMutableMap()
                ?: mutableMapOf()
        )

    private fun requireAuthenticatedActor(): String {
        val auth = SecurityContextHolder.getContext().authentication
        val name = auth?.name?.trim().orEmpty()
        if (auth == null || !auth.isAuthenticated || name.isBlank() || name.equals("anonymousUser", true)) {
            throw MillStatuses.unauthorizedRuntime("Authentication required for metadata write operations")
        }
        return name
    }

    private fun requireScopeWriteAllowed(scopeKey: String, actor: String) {
        // WI-090 permissive authorization stub:
        // defer strict scope/role checks to the dedicated authorization story.
        // Keep scope normalization for consistent key shape at write boundaries.
        @Suppress("UNUSED_VARIABLE")
        val normalizedScope = MetadataUrns.normaliseScopePath(scopeKey)
        @Suppress("UNUSED_VARIABLE")
        val currentActor = actor
    }

    private fun ensureMetadataEntityExistsForFacetWrite(entityId: String, actor: String) {
        if (metadataService.findById(entityId).isPresent) return
        if (!existsInPhysicalSchema(entityId)) {
            throw MillStatuses.notFoundRuntime("Entity not found in metadata or physical schema: $entityId")
        }
        val seed = toPhysicalMetadataEntity(entityId, actor)
        metadataEditService.createEntity(seed, actor)
    }

    private fun existsInPhysicalSchema(entityId: String): Boolean {
        val provider = schemaProvider ?: return false
        val parts = entityId.split('.')
        val schemaName = parts.firstOrNull() ?: return false
        if (!provider.isSchemaExists(schemaName)) return false
        if (parts.size == 1) return true
        val schema = provider.getSchema(schemaName)
        val tableName = parts[1]
        val table = schema.tablesList.firstOrNull { it.name == tableName } ?: return false
        if (parts.size == 2) return true
        val columnName = parts.drop(2).joinToString(".")
        return table.fieldsList.any { it.name == columnName }
    }

    private fun toPhysicalMetadataEntity(entityId: String, actor: String): io.qpointz.mill.metadata.domain.MetadataEntity {
        val parts = entityId.split('.')
        val type = when (parts.size) {
            1 -> MetadataType.SCHEMA
            2 -> MetadataType.TABLE
            else -> MetadataType.ATTRIBUTE
        }
        return io.qpointz.mill.metadata.domain.MetadataEntity(
            id = entityId,
            type = type,
            schemaName = parts.getOrNull(0),
            tableName = parts.getOrNull(1),
            attributeName = parts.drop(2).joinToString(".").ifBlank { null },
            facets = mutableMapOf(),
            createdBy = actor,
            updatedBy = actor
        )
    }
}
