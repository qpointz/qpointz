package io.qpointz.mill.metadata.api

import io.qpointz.mill.UrnSlug
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.data.metadata.SchemaEntityKinds
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.api.dto.FacetInstanceDto
import io.qpointz.mill.metadata.api.dto.FacetMergeTraceEntryDto
import io.qpointz.mill.metadata.api.dto.FacetMergeTraceResponseDto
import io.qpointz.mill.metadata.api.dto.MetadataAuditRecordDto
import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.repository.FacetReadSide
import io.qpointz.mill.metadata.service.FacetPayloadCoercion
import io.qpointz.mill.metadata.service.FacetService
import io.qpointz.mill.metadata.service.MetadataReadContext
import io.qpointz.mill.metadata.service.MetadataEditService
import io.qpointz.mill.metadata.service.MetadataEntityIdResolver
import io.qpointz.mill.metadata.service.MetadataReader
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
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import io.qpointz.mill.metadata.domain.MetadataEntity as DomainMetadataEntity

/**
 * REST controller for metadata entity discovery, facet resolution, and writes (SPEC §10).
 *
 * Path `{id}` is a **full** `urn:mill/…` entity URN (percent-encoded, `%2F` for `/`), a
 * [UrnSlug.encode] **full** segment (e.g. `mill-metadata-entity:schema.table.column` — hyphens encode `/`),
 * **or** a URN slug (e.g. `mill-model-table:skymill.cargo_shipments`); bare single segments stay invalid.
 * Dot-path catalog keys alone are rejected with **400**.
 *
 * Facet type keys in path variables are normalised via [MetadataUrns.normaliseFacetTypePath].
 * Read endpoints use `?scope=` (comma-separated scope URNs, last wins) and optional `?origin=`; the legacy
 * `?context=` alias is accepted when `scope` is absent. Write endpoints use `?scope=` for the target scope
 * (default: global).
 *
 * **Unassign (DELETE facet):** physical row removal applies only to `merge_action == SET` in the **global** scope;
 * overlay scopes persist a **TOMBSTONE** row instead ([io.qpointz.mill.metadata.service.DefaultFacetService.unassign]).
 */
@Tag(name = "metadata-entities", description = "Metadata entity and facet resolution endpoints")
@RestController
@RequestMapping("/api/v1/metadata/entities")
class MetadataEntityController(
    private val metadataService: MetadataService,
    private val metadataEditService: MetadataEditService,
    private val facetReadSide: FacetReadSide,
    private val facetService: FacetService,
    private val metadataReader: MetadataReader,
    private val urnCodec: MetadataEntityUrnCodec,
    private val schemaProvider: SchemaProvider? = null
) {

    @Operation(
        summary = "List metadata entities",
        description = "Returns entity identities only (no facets). Optional `kind` filters by persisted kind. " +
            "`schema` / `table` query parameters are not supported — use the schema explorer API."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Entities returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = MetadataEntityDto::class)))]
        ),
        ApiResponse(responseCode = "400", description = "Legacy schema/table query parameters are not accepted")
    )
    @GetMapping
    fun listEntities(
        @Parameter(description = "Filter by persisted entity kind (opaque string, equality match)")
        @RequestParam(required = false) kind: String?,
        @Parameter(description = "Rejected — returns 400 if present", deprecated = true)
        @RequestParam(required = false) schema: String?,
        @Parameter(description = "Rejected — returns 400 if present", deprecated = true)
        @RequestParam(required = false) table: String?
    ): ResponseEntity<List<MetadataEntityDto>> {
        rejectLegacyCoordinateParams(schema, table)
        val entities = if (kind.isNullOrBlank()) {
            metadataService.findAll()
        } else {
            metadataService.findByKind(kind.trim())
        }
        return ResponseEntity.ok(entities.map { toDto(it) })
    }

    @Operation(
        summary = "Get entity by id",
        description = "`{id}` must be a full percent-encoded Mill entity URN (`urn:mill/…`). " +
            "Legacy dot-path keys are rejected with 400."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Entity found",
            content = [Content(schema = Schema(implementation = MetadataEntityDto::class))]
        ),
        ApiResponse(responseCode = "400", description = "Path id is not a Mill URN"),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @GetMapping("/{id}")
    fun getEntityById(
        @Parameter(
            description = "Full entity URN (encode `/` as %2F) or UrnSlug path segment (no `/`; see io.qpointz.mill.UrnSlug)"
        )
        @PathVariable id: String
    ): ResponseEntity<MetadataEntityDto> {
        val eid = requireMillMetadataEntityPathId(id)
        return metadataService.findById(eid)
            .map { ResponseEntity.ok(toDto(it)) }
            .orElse(ResponseEntity.notFound().build())
    }

    @Operation(
        summary = "Facet merge trace",
        description = "Returns every persisted facet row for the entity with merge_action and whether it contributes " +
            "to the effective merged view for the given context order (SPEC §10.5)."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "All persisted facet rows for this entity id (may be empty if no metadata row or no assignments)",
            content = [Content(schema = Schema(implementation = FacetMergeTraceResponseDto::class))]
        )
    )
    @GetMapping("/{id}/facets/merge-trace")
    fun getFacetMergeTrace(
        @Parameter(description = "Full entity URN (path segment)") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope URNs / slugs, evaluation order (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids for contribution filtering")
        @RequestParam(required = false) origin: String?
    ): ResponseEntity<FacetMergeTraceResponseDto> {
        val eid = requireMillMetadataEntityPathId(id)
        val ctx = parseReadContext(scope, context, origin)
        val canonicalEntity = MetadataEntityUrn.canonicalize(eid)
        val all = facetReadSide.findByEntity(canonicalEntity)
        val effective = metadataReader.resolveEffective(all, ctx)
        val effectiveKeys = effective.map { it.uid }.toSet()
        val entries = all.map { row ->
            FacetMergeTraceEntryDto(
                uid = row.uid,
                facetTypeUrn = MetadataEntityUrn.canonicalize(row.facetTypeKey),
                scopeUrn = MetadataEntityUrn.canonicalize(row.scopeKey),
                mergeAction = row.mergeAction.name,
                payload = row.payload,
                contributesToEffectiveView = row.uid in effectiveKeys
            )
        }
        return ResponseEntity.ok(
            FacetMergeTraceResponseDto(
                scopes = ctx.scopes.map { MetadataEntityUrn.canonicalize(it) },
                entries = entries
            )
        )
    }

    @Operation(
        summary = "Get merged facets for an entity",
        description = "Returns effective facet rows after scope merge (SPEC §10.2). `?scope=` orders scopes (last wins)."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Merged facet instances (empty array if no entity row or no assignments)",
            content = [Content(array = ArraySchema(schema = Schema(implementation = FacetInstanceDto::class)))]
        )
    )
    @GetMapping("/{id}/facets")
    fun getEntityFacets(
        @Parameter(description = "Full entity URN (path segment)") @PathVariable id: String,
        @Parameter(description = "Comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?
    ): ResponseEntity<List<FacetInstanceDto>> {
        val eid = requireMillMetadataEntityPathId(id)
        val ctx = parseReadContext(scope, context, origin)
        val canonicalEntity = MetadataEntityUrn.canonicalize(eid)
        val merged = facetService.resolve(canonicalEntity, ctx)
        return ResponseEntity.ok(merged.map { toFacetInstanceDto(it) }.sortedBy { it.facetTypeUrn })
    }

    @Operation(
        summary = "Get merged facets by type",
        description = "Returns effective [FacetInstanceDto] rows for one facet type after scope merge."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Assignments for this facet type after merge (empty if none)",
            content = [Content(array = ArraySchema(schema = Schema(implementation = FacetInstanceDto::class)))]
        )
    )
    @GetMapping("/{id}/facets/{typeKey}")
    fun getEntityFacetByType(
        @Parameter(description = "Full entity URN (path segment)") @PathVariable id: String,
        @Parameter(description = "Facet type slug or URN, e.g. descriptive") @PathVariable typeKey: String,
        @Parameter(description = "Comma-separated scope URNs / slugs (preferred)")
        @RequestParam(required = false) scope: String?,
        @Parameter(description = "Deprecated: use `scope` when absent", deprecated = true)
        @RequestParam(required = false) context: String?,
        @Parameter(description = "Optional comma-separated origin ids")
        @RequestParam(required = false) origin: String?
    ): ResponseEntity<List<FacetInstanceDto>> {
        val eid = requireMillMetadataEntityPathId(id)
        val ctx = parseReadContext(scope, context, origin)
        val normType = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKey))
        val canonicalEntity = MetadataEntityUrn.canonicalize(eid)
        val list = facetService.resolveByType(canonicalEntity, normType, ctx)
        return ResponseEntity.ok(list.map { toFacetInstanceDto(it) })
    }

    @Operation(summary = "Create metadata entity", description = "Body: URN `id` (required) and optional `kind`.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Created"),
        ApiResponse(responseCode = "400", description = "Invalid id or body")
    )
    @PostMapping
    fun createEntity(@RequestBody dto: MetadataEntityDto): ResponseEntity<MetadataEntityDto> {
        val actor = requireAuthenticatedActor()
        val domain = toDomainEntity(dto, actor)
        val created = metadataEditService.createEntity(domain, actor)
        val canonical = MetadataEntityUrn.canonicalize(created.id)
        val segment = UriUtils.encodePathSegment(UrnSlug.encode(canonical), StandardCharsets.UTF_8)
        return ResponseEntity
            .created(URI.create("/api/v1/metadata/entities/$segment"))
            .body(toDto(metadataService.findById(canonical).orElse(created)))
    }

    @PutMapping("/{id}")
    fun overwriteEntity(
        @PathVariable id: String,
        @RequestBody dto: MetadataEntityDto
    ): ResponseEntity<MetadataEntityDto> {
        val actor = requireAuthenticatedActor()
        val pathId = requireMillMetadataEntityPathId(id)
        val domain = toDomainEntityForOverwrite(pathId, dto, actor)
        val updated = metadataEditService.overwriteEntity(pathId, domain, actor)
        return ResponseEntity.ok(toDto(updated))
    }

    @PatchMapping("/{id}")
    fun patchEntityOverwrite(
        @PathVariable id: String,
        @RequestBody dto: MetadataEntityDto
    ): ResponseEntity<MetadataEntityDto> = overwriteEntity(id, dto)

    @DeleteMapping("/{id}")
    fun deleteEntity(@PathVariable id: String): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        metadataEditService.deleteEntity(requireMillMetadataEntityPathId(id), actor)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Assign or upsert facet",
        description = "POST payload to assign a facet at `?scope=` (default global). SINGLE cardinality updates in place when a row exists."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Assignment result"),
        ApiResponse(responseCode = "404", description = "Entity not found")
    )
    @PostMapping("/{id}/facets/{typeKey}")
    fun assignFacet(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @Parameter(description = "Target scope URN or slug; defaults to global")
        @RequestParam(required = false) scope: String?,
        @RequestBody payload: Any?
    ): ResponseEntity<FacetInstanceDto> {
        val actor = requireAuthenticatedActor()
        val entityPath = requireMillMetadataEntityPathId(id)
        ensureMetadataEntityExistsForFacetWrite(entityPath, actor)
        val scopeKey = if (scope.isNullOrBlank()) {
            MetadataUrns.SCOPE_GLOBAL
        } else {
            MetadataUrns.normaliseScopePath(scope)
        }
        requireScopeWriteAllowed(scopeKey, actor)
        val updated = metadataEditService.setFacet(entityPath, typeKey, scopeKey, payload, actor)
        return ResponseEntity.ok(toFacetInstanceDto(updated))
    }

    @Operation(summary = "Replace facet assignment payload", description = "PATCH replaces payload for the row identified by `{facetUid}`.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Updated assignment"),
        ApiResponse(responseCode = "404", description = "Entity or persisted facet row not found"),
        ApiResponse(responseCode = "422", description = "Facet uid refers to a read-only inferred row (not persisted)")
    )
    @PatchMapping("/{id}/facets/{typeKey}/{facetUid}")
    fun patchFacetPayload(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @PathVariable facetUid: String,
        @RequestBody payload: Any?
    ): ResponseEntity<FacetInstanceDto> {
        val actor = requireAuthenticatedActor()
        val entityPath = requireMillMetadataEntityPathId(id)
        if (metadataService.findById(entityPath).isEmpty) {
            return ResponseEntity.notFound().build()
        }
        val normType = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKey))
        val canonicalEntity = MetadataEntityUrn.canonicalize(entityPath)
        val inst = requirePersistedAssignmentForFacetMutation(canonicalEntity, facetUid.trim())
        if (MetadataEntityUrn.canonicalize(inst.facetTypeKey) != normType) {
            throw MillStatuses.notFoundRuntime("Facet instance does not match facet type: $normType")
        }
        requireScopeWriteAllowed(inst.scopeKey, actor)
        val map = FacetPayloadCoercion.toPayloadMap(payload)
        val updated = facetService.update(facetUid.trim(), map, actor)
        return ResponseEntity.ok(toFacetInstanceDto(updated))
    }

    @Operation(
        summary = "Delete facet assignment",
        description = "Deletes one assignment by uid. Global SET rows are removed; overlay SET rows become TOMBSTONE (SPEC §10.2)."
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Deleted or tombstoned"),
        ApiResponse(responseCode = "404", description = "Entity or persisted facet row not found"),
        ApiResponse(responseCode = "422", description = "Facet uid refers to a read-only inferred row (not persisted)")
    )
    @DeleteMapping("/{id}/facets/{typeKey}/{facetUid}")
    fun deleteFacetByUid(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @PathVariable facetUid: String
    ): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        val entityPath = requireMillMetadataEntityPathId(id)
        if (metadataService.findById(entityPath).isEmpty) {
            throw MillStatuses.notFoundRuntime("Entity not found: $entityPath")
        }
        val normType = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKey))
        val canonicalEntity = MetadataEntityUrn.canonicalize(entityPath)
        val inst = requirePersistedAssignmentForFacetMutation(canonicalEntity, facetUid.trim())
        if (MetadataEntityUrn.canonicalize(inst.facetTypeKey) != normType) {
            throw MillStatuses.notFoundRuntime("Facet instance does not match facet type: $normType")
        }
        requireScopeWriteAllowed(inst.scopeKey, actor)
        metadataEditService.deleteFacetInstanceByUid(entityPath, facetUid.trim(), actor)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Delete all facet assignments of a type at a scope",
        description = "`scope` query parameter is required. Applies per-row unassign rules (SET+global → delete; overlay → tombstone)."
    )
    @DeleteMapping("/{id}/facets/{typeKey}")
    fun deleteFacetsAtScope(
        @PathVariable id: String,
        @PathVariable typeKey: String,
        @Parameter(description = "Required scope URN or slug", required = true)
        @RequestParam(required = true) scope: String
    ): ResponseEntity<Void> {
        val actor = requireAuthenticatedActor()
        val entityPath = requireMillMetadataEntityPathId(id)
        if (metadataService.findById(entityPath).isEmpty) {
            throw MillStatuses.notFoundRuntime("Entity not found: $entityPath")
        }
        val scopeKey = scope.trim().takeIf { it.isNotEmpty() }
            ?: throw MillStatuses.badRequestRuntime("Query parameter scope is required and must not be blank")
        val normScope = MetadataUrns.normaliseScopePath(scopeKey)
        requireScopeWriteAllowed(normScope, actor)
        metadataEditService.deleteFacet(entityPath, typeKey, normScope, actor)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/history")
    fun getEntityHistory(@PathVariable id: String): ResponseEntity<List<MetadataAuditRecordDto>> {
        requireAuthenticatedActor()
        val eid = requireMillMetadataEntityPathId(id)
        val rows = metadataEditService.history(eid).map {
            MetadataAuditRecordDto(
                auditId = it.auditId,
                operationType = it.operationType,
                entityUrn = it.entityId,
                facetTypeUrn = it.facetType,
                scopeUrn = it.scopeKey,
                actorId = it.actorId,
                occurredAt = it.occurredAt,
                payloadBefore = it.payloadBefore,
                payloadAfter = it.payloadAfter,
                changeSummary = it.changeSummary
            )
        }
        return ResponseEntity.ok(rows)
    }

    private fun rejectLegacyCoordinateParams(schema: String?, table: String?) {
        if (!schema.isNullOrBlank() || !table.isNullOrBlank()) {
            throw MillStatuses.badRequestRuntime(
                "Query parameters 'schema' and 'table' are not supported on this endpoint; use the schema explorer API."
            )
        }
    }

    /**
     * @param raw path variable (may be URL-decoded once by Spring)
     * @return canonical entity URN
     */
    private fun requireMillMetadataEntityPathId(raw: String): String {
        val t = raw.trim()
        if (t.startsWith("urn:mill/", ignoreCase = true)) {
            return MetadataEntityUrn.canonicalize(t)
        }
        if (t.startsWith("urn:", ignoreCase = true)) {
            throw MillStatuses.badRequestRuntime(
                "Entity path id must start with urn:mill/ or be a path segment (UrnSlug). Received: $t"
            )
        }
        val urn = try {
            UrnSlug.decode(t)
        } catch (ex: IllegalArgumentException) {
            throw MillStatuses.badRequestRuntime(
                "Entity path id must be a full Mill URN (urn:mill/…) or a UrnSlug segment. ${ex.message}"
            )
        }
        val canonical = try {
            MetadataEntityUrn.canonicalize(urn)
        } catch (ex: IllegalArgumentException) {
            throw MillStatuses.badRequestRuntime(
                "Entity path id must resolve to a Mill URN (urn:mill/…). ${ex.message}"
            )
        }
        if (!canonical.startsWith("urn:mill/")) {
            throw MillStatuses.badRequestRuntime(
                "Entity path id must resolve to a urn:mill/… URN; got: $canonical"
            )
        }
        return canonical
    }

    private fun toDto(entity: DomainMetadataEntity): MetadataEntityDto =
        MetadataEntityDto(
            entityUrn = entity.id,
            kind = entity.kind,
            createdAt = entity.createdAt,
            lastModifiedAt = entity.lastModifiedAt,
            createdBy = entity.createdBy,
            lastModifiedBy = entity.lastModifiedBy
        )

    private fun toFacetInstanceDto(i: FacetInstance): FacetInstanceDto =
        FacetInstanceDto(
            uid = i.uid,
            facetTypeUrn = MetadataEntityUrn.canonicalize(i.facetTypeKey),
            scopeUrn = MetadataEntityUrn.canonicalize(i.scopeKey),
            origin = i.origin,
            originId = i.originId,
            assignmentUid = i.assignmentUid,
            payload = i.payload,
            createdAt = i.createdAt,
            lastModifiedAt = i.lastModifiedAt
        )

    /**
     * Resolves read query parameters for facet GETs and merge-trace.
     *
     * Effective scope string is `scope` when non-blank; otherwise the legacy `context` parameter (migration).
     * Parsed with [MetadataReadContext.parse] together with optional comma-separated `origin` ids.
     *
     * @param scope preferred comma-separated scope URNs or slugs
     * @param contextLegacy deprecated alias for [scope] when [scope] is null or blank
     * @param origin optional comma-separated origin ids
     */
    private fun parseReadContext(scope: String?, contextLegacy: String?, origin: String?): MetadataReadContext {
        val effectiveScope = scope?.takeIf { it.isNotBlank() } ?: contextLegacy
        return try {
            MetadataReadContext.parse(effectiveScope, origin)
        } catch (ex: IllegalArgumentException) {
            throw MillStatuses.badRequestRuntime("Malformed scope parameter: ${effectiveScope ?: "<blank>"}")
        }
    }

    private fun toDomainEntity(dto: MetadataEntityDto, actorForNew: String): DomainMetadataEntity {
        val urn = resolveMillUrnForCreateBody(dto)
        val kind = dto.kind?.trim()?.takeIf { it.isNotEmpty() }
        val now = Instant.EPOCH
        return DomainMetadataEntity(
            id = urn,
            kind = kind,
            uuid = null,
            createdAt = now,
            createdBy = actorForNew,
            lastModifiedAt = now,
            lastModifiedBy = actorForNew
        )
    }

    private fun toDomainEntityForOverwrite(pathId: String, dto: MetadataEntityDto, actor: String): DomainMetadataEntity {
        val existing = metadataService.findById(pathId).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $pathId")
        }
        val urn = MetadataEntityUrn.canonicalize(existing.id)
        val kind = dto.kind?.trim()?.takeIf { it.isNotEmpty() } ?: existing.kind
        return DomainMetadataEntity(
            id = urn,
            kind = kind,
            uuid = existing.uuid,
            createdAt = existing.createdAt,
            createdBy = existing.createdBy,
            lastModifiedAt = existing.lastModifiedAt,
            lastModifiedBy = actor
        )
    }

    /**
     * @param dto incoming create body; [MetadataEntityDto.entityUrn] must be a full `urn:mill/` string
     * @return canonical entity URN
     */
    private fun resolveMillUrnForCreateBody(dto: MetadataEntityDto): String {
        val rawId = dto.entityUrn?.trim().orEmpty()
        if (rawId.isEmpty()) {
            throw MillStatuses.badRequestRuntime(
                "entityUrn is required: full typed entity URN (e.g. `urn:mill/model/table:schema.table`)"
            )
        }
        if (!rawId.startsWith("urn:mill/", ignoreCase = true)) {
            throw MillStatuses.badRequestRuntime(
                "Entity id must start with urn:mill/ (case-insensitive). Received: $rawId"
            )
        }
        return MetadataEntityUrn.canonicalize(rawId)
    }

    private fun requireAuthenticatedActor(): String {
        val auth = SecurityContextHolder.getContext().authentication
        val name = auth?.name?.trim().orEmpty()
        if (auth == null || !auth.isAuthenticated || name.isBlank() || name.equals("anonymousUser", true)) {
            throw MillStatuses.unauthorizedRuntime("Authentication required for metadata write operations")
        }
        return name
    }

    private fun requireScopeWriteAllowed(scopeKey: String, actor: String) {
        @Suppress("UNUSED_VARIABLE")
        val normalizedScope = MetadataUrns.normaliseScopePath(scopeKey)
        @Suppress("UNUSED_VARIABLE")
        val currentActor = actor
    }

    /**
     * Ensures a metadata entity row exists before facet writes, optionally seeding from the physical schema when
     * the URN maps to an existing catalog object (SPEC §10 — facet writes against physical objects).
     *
     * @param entityId full canonical entity URN from the path
     * @param actor authenticated actor for seeded rows
     */
    private fun ensureMetadataEntityExistsForFacetWrite(entityId: String, actor: String) {
        if (metadataService.findById(entityId).isPresent) {
            return
        }
        val canonicalKey = MetadataEntityIdResolver.canonicalizeEntityId(entityId)
        if (!existsInPhysicalSchema(canonicalKey)) {
            throw MillStatuses.notFoundRuntime("Entity not found in metadata or physical schema: $entityId")
        }
        val seed = toPhysicalMetadataEntity(canonicalKey, actor)
        metadataEditService.createEntity(seed, actor)
    }

    private fun existsInPhysicalSchema(entityId: String): Boolean {
        val provider = schemaProvider ?: return false
        val canonical = MetadataEntityIdResolver.canonicalizeEntityId(entityId)
        val parts = canonical.split('.')
        val schemaSegment = parts.firstOrNull() ?: return false
        val schemaActual = provider.getSchemaNames().firstOrNull { it.equals(schemaSegment, ignoreCase = true) }
            ?: return false
        if (!provider.isSchemaExists(schemaActual)) {
            return false
        }
        if (parts.size == 1) {
            return true
        }
        val schema = provider.getSchema(schemaActual)
        val tableSegment = parts[1]
        val table = schema.tablesList.firstOrNull { it.name.equals(tableSegment, ignoreCase = true) } ?: return false
        if (parts.size == 2) {
            return true
        }
        val columnName = parts.drop(2).joinToString(".")
        return table.fieldsList.any { it.name.equals(columnName, ignoreCase = true) }
    }

    private fun toPhysicalMetadataEntity(entityId: String, actor: String): DomainMetadataEntity {
        val canonical = MetadataEntityIdResolver.canonicalizeEntityId(entityId)
        val parts = canonical.split('.')
        val urn = when (parts.size) {
            1 -> urnCodec.forSchema(parts[0])
            2 -> urnCodec.forTable(parts[0], parts[1])
            else -> urnCodec.forAttribute(parts[0], parts[1], parts.drop(2).joinToString("."))
        }
        val kind = when (parts.size) {
            1 -> SchemaEntityKinds.SCHEMA
            2 -> SchemaEntityKinds.TABLE
            else -> SchemaEntityKinds.ATTRIBUTE
        }
        val now = Instant.EPOCH
        return DomainMetadataEntity(
            id = urn,
            kind = kind,
            uuid = null,
            createdAt = now,
            createdBy = actor,
            lastModifiedAt = now,
            lastModifiedBy = actor
        )
    }

    private fun findFacetAssignment(entityId: String, uid: String): FacetAssignment? {
        val row = facetReadSide.findByUid(uid) ?: return null
        if (MetadataEntityUrn.canonicalize(row.entityId) != MetadataEntityUrn.canonicalize(entityId)) {
            return null
        }
        return row
    }

    /**
     * Facet mutations apply only to persisted [FacetAssignment] rows. Merged read views may include
     * [FacetOrigin.INFERRED] contributions without persistence — those uids must be rejected with **422**.
     *
     * @param canonicalEntity canonical entity URN
     * @param facetUid assignment uid from the path
     * @return persisted row for this entity and uid
     */
    private fun requirePersistedAssignmentForFacetMutation(
        canonicalEntity: String,
        facetUid: String
    ): FacetAssignment {
        findFacetAssignment(canonicalEntity, facetUid)?.let { return it }
        val merged = facetService.resolve(canonicalEntity, MetadataReadContext.global())
        val inferred = merged.any {
            it.uid == facetUid && it.origin == FacetOrigin.INFERRED
        }
        if (inferred) {
            throw MillStatuses.unprocessableRuntime(
                "Cannot modify inferred facet row (read-only); uid=$facetUid"
            )
        }
        throw MillStatuses.notFoundRuntime("Facet instance not found: $facetUid")
    }
}
