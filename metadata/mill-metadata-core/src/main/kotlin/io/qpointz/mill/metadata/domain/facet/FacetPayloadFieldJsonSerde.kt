package io.qpointz.mill.metadata.domain.facet

import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.DatabindException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer

/**
 * Jackson [ValueDeserializer] for [FacetPayloadField].
 *
 * Parses `stereotype` from either a JSON string (comma‑separated tags) or a JSON array of strings.
 */
class FacetPayloadFieldDeserializer : ValueDeserializer<FacetPayloadField>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FacetPayloadField {
        val tree = ctxt.readTree(p) as? JsonNode
            ?: throw DatabindException.from(p, "FacetPayloadField: expected object")
        val nameNode = tree.path("name")
        if (nameNode.isMissingNode) throw DatabindException.from(p, "FacetPayloadField: missing name")
        val schemaNode = tree.path("schema")
        if (schemaNode.isMissingNode) throw DatabindException.from(p, "FacetPayloadField: missing schema")
        val name = nameNode.asText()
        val schema = ctxt.readTreeAsValue(schemaNode, FacetPayloadSchema::class.java)
        val requiredNode = tree.path("required")
        val required = when {
            requiredNode.isMissingNode || requiredNode.isNull -> true
            else -> requiredNode.asBoolean()
        }
        val stereotype = parseStereotypeJson(ctxt, tree.path("stereotype"))
        return FacetPayloadField(name, schema, required, stereotype)
    }

    private fun parseStereotypeJson(ctxt: DeserializationContext, node: JsonNode?): List<String>? {
        if (node == null || node.isMissingNode || node.isNull) return null
        return when {
            node.isTextual -> {
                val parts = node.asText().split(',').map { it.trim() }.filter { it.isNotEmpty() }
                parts.takeIf { it.isNotEmpty() }
            }
            node.isArray -> {
                val out = mutableListOf<String>()
                for (e in node) {
                    if (!e.isTextual) {
                        throw DatabindException.from(ctxt.parser, "FacetPayloadField: stereotype array entries must be strings")
                    }
                    val t = e.asText().trim()
                    if (t.isNotEmpty()) out.add(t)
                }
                out.takeIf { it.isNotEmpty() }
            }
            else -> throw DatabindException.from(ctxt.parser, "FacetPayloadField: stereotype must be string or string array")
        }
    }
}

/**
 * Jackson [ValueSerializer] for [FacetPayloadField].
 *
 * Writes `stereotype` as a comma‑separated string when [FacetPayloadField.schema] is not [FacetSchemaType.ARRAY],
 * and as a JSON array when the value schema is an array.
 */
class FacetPayloadFieldSerializer : ValueSerializer<FacetPayloadField>() {
    override fun serialize(value: FacetPayloadField, gen: JsonGenerator, serializers: SerializationContext) {
        gen.writeStartObject()
        gen.writeStringProperty("name", value.name)
        gen.writeName("schema")
        serializers.findValueSerializer(FacetPayloadSchema::class.java).serialize(value.schema, gen, serializers)
        gen.writeBooleanProperty("required", value.required)
        val st = value.stereotype
        if (!st.isNullOrEmpty()) {
            gen.writeName("stereotype")
            if (value.schema.type == FacetSchemaType.ARRAY) {
                gen.writeStartArray()
                for (t in st) {
                    gen.writeString(t)
                }
                gen.writeEndArray()
            } else {
                gen.writeString(st.joinToString(","))
            }
        }
        gen.writeEndObject()
    }
}
