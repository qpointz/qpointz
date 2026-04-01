package io.qpointz.mill.metadata.io

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.utils.YamlUtils
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.UUID

/**
 * Converts domain objects to and from the canonical multi-document YAML format (SPEC §15.4).
 *
 * Uses [YamlUtils.defaultYamlMapper] only — no direct Jackson types in public API.
 */
object MetadataYamlSerializer {

    private val log = LoggerFactory.getLogger(MetadataYamlSerializer::class.java)
    private val docSplit = Regex("^---\\s*$", RegexOption.MULTILINE)
    private val mapper get() = YamlUtils.defaultYamlMapper()

    /**
     * Multi-document files often start with a `#` comment block before the first `---`.
     * That preamble is its own split segment; Jackson cannot map it to a [Map].
     */
    private fun isIgnorableYamlDocument(segment: String): Boolean =
        segment.lineSequence().none { line ->
            val t = line.trim()
            t.isNotEmpty() && !t.startsWith("#")
        }

    /** Same local-part rules as [io.qpointz.mill.data.schema.RelationalMetadataEntityUrns] (kept here to avoid a Gradle cycle). */
    private const val RELATIONAL_ENTITY_PREFIX = "urn:mill/metadata/entity:"

    /**
     * Serialises scopes, definitions, entities, and facet assignments as multi-document YAML.
     *
     * @param scopes persisted scope rows to emit as `kind: MetadataScope` documents
     * @param definitions facet type definitions to emit as `kind: FacetTypeDefinition` documents
     * @param entities entity identity rows to emit as `kind: MetadataEntity` documents
     * @param facetsByEntity facet assignments grouped by canonical entity URN; embedded under each entity doc
     */
    fun serialize(
        scopes: List<MetadataScope> = emptyList(),
        definitions: List<FacetTypeDefinition> = emptyList(),
        entities: List<MetadataEntity> = emptyList(),
        facetsByEntity: Map<String, List<FacetAssignment>> = emptyMap()
    ): String {
        val chunks = mutableListOf<String>()
        for (s in scopes) {
            chunks += mapper.writeValueAsString(scopeToMap(s))
        }
        for (d in definitions) {
            chunks += mapper.writeValueAsString(definitionToMap(d))
        }
        for (e in entities) {
            val eid = MetadataEntityUrn.canonicalize(e.id)
            val facets = facetsByEntity[eid].orEmpty()
            chunks += mapper.writeValueAsString(entityToMap(e, facets))
        }
        return chunks.joinToString("\n---\n")
    }

    /**
     * Parses one YAML file that may contain multiple `---`-separated documents.
     *
     * Supports `kind:`-discriminated documents only (SPEC §15.2).
     *
     * This is a greenfield story: legacy envelope shapes (`metadataFormat: CANONICAL` / `entities:`)
     * and legacy key aliases are intentionally not supported and must fail fast.
     */
    fun deserialize(yaml: String): MetadataYamlDocument {
        val trimmed = yaml.trim()
        if (trimmed.isEmpty()) {
            return MetadataYamlDocument(emptyList(), emptyList(), emptyList(), emptyMap())
        }
        val scopes = mutableListOf<MetadataScope>()
        val definitions = mutableListOf<FacetTypeDefinition>()
        val entitiesById = linkedMapOf<String, MetadataEntity>()
        val facetsByEntity = mutableMapOf<String, MutableList<FacetAssignment>>()

        val docStrings = trimmed.split(docSplit).map { it.trim() }.filter { it.isNotEmpty() }
        for (docStr in docStrings) {
            if (isIgnorableYamlDocument(docStr)) {
                continue
            }
            val root = mapper.readValue(docStr, Map::class.java) as? Map<*, *> ?: continue
            if (root["entities"] is List<*>) {
                error("Unsupported YAML envelope: 'entities:' list is not allowed; use multi-document kind: stream")
            }
            val kind = root["kind"]?.toString() ?: continue
            when (kind) {
                "FacetTypeDefinition" -> definitions += parseDefinition(root)
                "MetadataScope" -> scopes += parseScope(root)
                "MetadataEntity" -> ingestKindEntity(root, entitiesById, facetsByEntity)
                else -> log.warn("Skipping YAML document with unknown kind: {}", kind)
            }
        }
        return MetadataYamlDocument(
            scopes,
            definitions,
            entitiesById.values.toList(),
            facetsByEntity.mapValues { it.value.toList() }
        )
    }

    private fun ingestKindEntity(
        root: Map<*, *>,
        entitiesById: MutableMap<String, MetadataEntity>,
        facetsByEntity: MutableMap<String, MutableList<FacetAssignment>>
    ) {
        val rawEntity = root["entityUrn"]?.toString()
            ?: error("MetadataEntity missing entityUrn")
        val entityRes = MetadataEntityUrn.canonicalize(rawEntity)
        val entityKind = root["entityKind"]?.toString()?.lowercase()
        val entity = MetadataEntity(
            id = entityRes,
            kind = entityKind,
            uuid = null,
            createdAt = Instant.EPOCH,
            createdBy = "import",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "import"
        )
        entitiesById[entityRes] = entity
        val list = facetsByEntity.getOrPut(entityRes) { mutableListOf() }
        val facetNodes = root["facets"] as? List<*> ?: emptyList<Any?>()
        facetNodes.forEachIndexed { index, node ->
            val m = node as? Map<*, *> ?: return@forEachIndexed
            val facetTypeRaw = m["facetTypeUrn"]?.toString()
                ?: error("MetadataEntity.facet missing facetTypeUrn (entityUrn=$entityRes, index=$index)")
            val facetType = normaliseFacetType(facetTypeRaw)
            val scopeRaw = m["scopeUrn"]?.toString()
                ?: error("MetadataEntity.facet missing scopeUrn (entityUrn=$entityRes, index=$index)")
            val scope = MetadataUrns.normaliseScopeKey(scopeRaw)
            val uidRaw = m["uid"]?.toString()
            val uid = when {
                uidRaw.isNullOrBlank() || uidRaw == "~" || uidRaw.equals("null", true) ->
                    stableAssignmentUid(entityRes, facetType, scope, index)
                MetadataEntityUrn.isMillUrn(uidRaw) ->
                    MetadataEntityUrn.canonicalize(uidRaw)
                else ->
                    uidRaw.trim()
            }
            val merge = parseMergeAction(m["mergeAction"]?.toString())
            val payload = deepStringKeyMap(m["payload"])
            list += FacetAssignment(
                uid = uid,
                entityId = entityRes,
                facetTypeKey = facetType,
                scopeKey = MetadataEntityUrn.canonicalize(scope),
                mergeAction = merge,
                payload = payload,
                createdAt = Instant.EPOCH,
                createdBy = "import",
                lastModifiedAt = Instant.EPOCH,
                lastModifiedBy = "import"
            )
        }
    }

    private fun parseDefinition(root: Map<*, *>): FacetTypeDefinition {
        val rawTypeUrn = root["facetTypeUrn"]?.toString()
            ?: error("FacetTypeDefinition missing facetTypeUrn")
        val typeKey = MetadataEntityUrn.canonicalize(rawTypeUrn)
        val card = when (root["targetCardinality"]?.toString()?.uppercase()) {
            "MULTIPLE" -> FacetTargetCardinality.MULTIPLE
            else -> FacetTargetCardinality.SINGLE
        }
        val applicable = (root["applicableTo"] as? List<*>)?.map { it.toString() }
        val contentSchema = (root["contentSchema"] as? Map<*, *>)?.let { deepStringKeyMap(it) }
        val label = root["title"]?.toString()?.takeIf { it.isNotBlank() }
            ?: root["displayName"]?.toString()
        return FacetTypeDefinition(
            typeKey = typeKey,
            displayName = label,
            description = root["description"]?.toString(),
            category = root["category"]?.toString(),
            mandatory = root["mandatory"]?.toString()?.toBooleanStrictOrNull() ?: false,
            enabled = root["enabled"]?.toString()?.toBooleanStrictOrNull() ?: true,
            targetCardinality = card,
            applicableTo = applicable,
            contentSchema = contentSchema,
            schemaVersion = root["schemaVersion"]?.toString(),
            createdAt = Instant.EPOCH,
            createdBy = "import",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "import"
        )
    }

    private fun parseScope(root: Map<*, *>): MetadataScope {
        val rawScope = root["scopeUrn"]?.toString()
            ?: error("MetadataScope missing scopeUrn")
        val res = MetadataEntityUrn.canonicalize(rawScope)
        return MetadataScope(
            res = res,
            scopeType = root["scopeType"]?.toString() ?: "CUSTOM",
            referenceId = root["referenceId"]?.toString()?.takeIf { it != "null" && it != "~" },
            displayName = root["displayName"]?.toString(),
            ownerId = root["ownerId"]?.toString(),
            visibility = root["visibility"]?.toString() ?: "PUBLIC",
            uuid = null,
            createdAt = Instant.EPOCH,
            createdBy = "import",
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = "import"
        )
    }

    private fun scopeToMap(s: MetadataScope): Map<String, Any?> = linkedMapOf(
        "kind" to "MetadataScope",
        "scopeUrn" to s.res,
        "scopeType" to s.scopeType,
        "referenceId" to (s.referenceId ?: "~"),
        "displayName" to s.displayName,
        "ownerId" to s.ownerId,
        "visibility" to s.visibility
    )

    private fun definitionToMap(d: FacetTypeDefinition): Map<String, Any?> {
        val m = linkedMapOf<String, Any?>(
            "kind" to "FacetTypeDefinition",
            "facetTypeUrn" to d.typeKey,
            "title" to d.displayName,
            "description" to d.description,
            "category" to d.category,
            "mandatory" to d.mandatory,
            "enabled" to d.enabled,
            "targetCardinality" to d.targetCardinality.name
        )
        d.applicableTo?.let { m["applicableTo"] = it }
        d.contentSchema?.let { m["contentSchema"] = it }
        d.schemaVersion?.let { m["schemaVersion"] = it }
        return m
    }

    private fun entityToMap(e: MetadataEntity, facets: List<FacetAssignment>): Map<String, Any?> {
        val facetList = facets.map { f ->
            linkedMapOf(
                "uid" to f.uid,
                "facetTypeUrn" to f.facetTypeKey,
                "scopeUrn" to f.scopeKey,
                "mergeAction" to f.mergeAction.name,
                "payload" to f.payload
            )
        }
        return linkedMapOf(
            "kind" to "MetadataEntity",
            "entityUrn" to MetadataEntityUrn.canonicalize(e.id),
            "entityKind" to e.kind,
            "facets" to facetList
        )
    }

    private fun normaliseFacetType(key: String): String {
        val trimmed = key.trim()
        return if (trimmed.startsWith("urn:")) {
            MetadataEntityUrn.canonicalize(trimmed)
        } else {
            MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypeKey(trimmed))
        }
    }

    private fun parseMergeAction(raw: String?): MergeAction {
        if (raw.isNullOrBlank()) return MergeAction.SET
        return try {
            MergeAction.valueOf(raw.uppercase())
        } catch (_: IllegalArgumentException) {
            MergeAction.SET
        }
    }

    private fun deepStringKeyMap(node: Any?): Map<String, Any?> {
        if (node !is Map<*, *>) return emptyMap()
        return node.entries.associate { (k, v) ->
            k.toString() to deepValue(v)
        }
    }

    private fun deepValue(v: Any?): Any? = when (v) {
        is Map<*, *> -> deepStringKeyMap(v)
        is List<*> -> v.map { deepValue(it) }
        else -> v
    }

    /**
     * Stable assignment id when YAML omits `uid` (SPEC §15.3).
     *
     * @param index zero-based position within that entity's facet list in the document
     */
    fun stableAssignmentUid(entityId: String, facetTypeKey: String, scopeKey: String, index: Int): String {
        val basis = "$entityId\u0000$facetTypeKey\u0000$scopeKey\u0000$index"
        val digest = MessageDigest.getInstance("SHA-256").digest(basis.toByteArray(Charsets.UTF_8))
        return UUID.nameUUIDFromBytes(digest.copyOf(16)).toString()
    }
}
