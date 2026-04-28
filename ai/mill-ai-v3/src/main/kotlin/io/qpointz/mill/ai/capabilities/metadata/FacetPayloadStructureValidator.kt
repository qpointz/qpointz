package io.qpointz.mill.ai.capabilities.metadata

import io.qpointz.mill.metadata.domain.facet.FacetEnumValue
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * Validates a JSON object map against a facet [FacetTypeManifest] payload schema.
 *
 * Mirrors the structural guarantees enforced server-side without requiring a REST validate endpoint.
 */
object FacetPayloadStructureValidator {

    fun validate(rootPayload: FacetPayloadSchema, value: Any?): List<String> =
        validateNode("payload", rootPayload, value)

    private fun validateNode(path: String, schema: FacetPayloadSchema, value: Any?): List<String> {
        val errs = mutableListOf<String>()
        when (schema.type) {
            FacetSchemaType.OBJECT -> {
                if (value == null || value !is Map<*, *>) {
                    errs += "$path: expected object"
                    return errs
                }
                val map = value.mapKeys { it.key.toString() }.mapValues { it.value }
                val fields = schema.fields ?: emptyList()
                for (f in fields) {
                    val v = map[f.name]
                    if (f.required && v == null) {
                        errs += "$path.${f.name}: required field missing"
                    }
                    if (v != null) {
                        errs += validateNode("$path.${f.name}", f.schema, v)
                    }
                }
            }
            FacetSchemaType.ARRAY -> {
                if (value == null || value !is List<*>) {
                    errs += "$path: expected array"
                    return errs
                }
                val itemSchema = schema.items ?: run {
                    errs += "$path: array schema missing items"
                    return errs
                }
                value.forEachIndexed { i, elt ->
                    errs += validateNode("$path[$i]", itemSchema, elt)
                }
            }
            FacetSchemaType.STRING -> {
                when (value) {
                    is String -> Unit
                    null -> errs += "$path: expected string, got null"
                    else -> errs += "$path: expected string, got ${value::class.simpleName}"
                }
            }
            FacetSchemaType.NUMBER -> {
                when (value) {
                    is Number -> Unit
                    null -> errs += "$path: expected number, got null"
                    else -> errs += "$path: expected number, got ${value::class.simpleName}"
                }
            }
            FacetSchemaType.BOOLEAN -> {
                when (value) {
                    is Boolean -> Unit
                    null -> errs += "$path: expected boolean, got null"
                    else -> errs += "$path: expected boolean, got ${value::class.simpleName}"
                }
            }
            FacetSchemaType.ENUM -> {
                val values = schema.values ?: run {
                    errs += "$path: enum schema missing values"
                    return errs
                }
                if (value !is String) {
                    errs += "$path: expected enum string"
                    return errs
                }
                val ok = values.any { enumVal: FacetEnumValue -> enumVal.value == value }
                if (!ok) {
                    errs += "$path: value must be one of ${values.joinToString { it.value }}"
                }
            }
        }
        return errs
    }

    internal fun manifestForFacetType(all: List<FacetTypeManifest>, facetTypeKey: String): FacetTypeManifest? =
        all.firstOrNull { keyMatches(it.typeKey, facetTypeKey) }

    private fun keyMatches(manifestKey: String, requested: String): Boolean {
        if (manifestKey.equals(requested, ignoreCase = false)) return true
        if (requested.endsWith(manifestKey.removePrefix("urn:"))) return true
        return manifestKey.substringAfterLast(':', "").isNotBlank() &&
            requested.substringAfterLast(':', "").equals(manifestKey.substringAfterLast(':'), ignoreCase = true)
    }
}
