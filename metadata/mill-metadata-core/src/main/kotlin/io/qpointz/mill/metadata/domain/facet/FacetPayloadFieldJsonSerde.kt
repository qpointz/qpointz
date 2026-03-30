package io.qpointz.mill.metadata.domain.facet

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * Jackson [JsonDeserializer] for [FacetPayloadField].
 *
 * Parses `stereotype` from either a JSON string (comma‑separated tags) or a JSON array of strings.
 */
class FacetPayloadFieldDeserializer : JsonDeserializer<FacetPayloadField>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FacetPayloadField {
        val codec = p.codec as ObjectMapper
        val tree: JsonNode = codec.readTree(p) ?: throw JsonMappingException.from(p, "FacetPayloadField: expected object")
        val nameNode = tree.path("name")
        if (nameNode.isMissingNode) throw JsonMappingException.from(p, "FacetPayloadField: missing name")
        val schemaNode = tree.path("schema")
        if (schemaNode.isMissingNode) throw JsonMappingException.from(p, "FacetPayloadField: missing schema")
        val name = nameNode.asText()
        val schema = codec.treeToValue(schemaNode, FacetPayloadSchema::class.java)
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
                        throw JsonMappingException.from(ctxt.parser, "FacetPayloadField: stereotype array entries must be strings")
                    }
                    val t = e.asText().trim()
                    if (t.isNotEmpty()) out.add(t)
                }
                out.takeIf { it.isNotEmpty() }
            }
            else -> throw JsonMappingException.from(ctxt.parser, "FacetPayloadField: stereotype must be string or string array")
        }
    }
}

/**
 * Jackson [JsonSerializer] for [FacetPayloadField].
 *
 * Writes `stereotype` as a comma‑separated string when [FacetPayloadField.schema] is not [FacetSchemaType.ARRAY],
 * and as a JSON array when the value schema is an array.
 */
class FacetPayloadFieldSerializer : JsonSerializer<FacetPayloadField>() {
    override fun serialize(value: FacetPayloadField, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeFieldName("schema")
        serializers.findValueSerializer(FacetPayloadSchema::class.java).serialize(value.schema, gen, serializers)
        gen.writeBooleanField("required", value.required)
        val st = value.stereotype
        if (!st.isNullOrEmpty()) {
            gen.writeFieldName("stereotype")
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
