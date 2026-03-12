package io.qpointz.mill.ai.langchain4j

import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema
import dev.langchain4j.model.chat.request.json.JsonEnumSchema
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema
import dev.langchain4j.model.chat.request.json.JsonNumberSchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonStringSchema
import io.qpointz.mill.ai.ToolSchema
import io.qpointz.mill.ai.ToolSchemaField
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ToolSchemaConverterTest {

    // ── toJsonObjectSchema — root validation ──────────────────────────────────

    @Test
    fun `should reject non-OBJECT root schema`() {
        val ex = assertThrows<IllegalArgumentException> {
            ToolSchemaConverter.toJsonObjectSchema(ToolSchema.string("not an object"))
        }
        assertTrue(ex.message!!.contains("OBJECT"))
    }

    @Test
    fun `should build empty JsonObjectSchema from empty object`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj())
        assertTrue(json.properties().isNullOrEmpty())
    }

    @Test
    fun `should propagate object description`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(description = "The input."))
        assertEquals("The input.", json.description())
    }

    @Test
    fun `should produce null description when absent`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj())
        assertNull(json.description())
    }

    // ── toJsonObjectSchema — scalar field types ───────────────────────────────

    @Test
    fun `should produce JsonStringSchema for STRING field`() {
        val json = objWith(ToolSchemaField("name", ToolSchema.string("A name.")))
        assertInstanceOf(JsonStringSchema::class.java, json.properties()["name"])
    }

    @Test
    fun `should carry description on string field`() {
        val json = objWith(ToolSchemaField("name", ToolSchema.string("A name.")))
        assertEquals("A name.", (json.properties()["name"] as JsonStringSchema).description())
    }

    @Test
    fun `should produce JsonIntegerSchema for INTEGER field`() {
        val json = objWith(ToolSchemaField("count", ToolSchema.integer("A count.")))
        assertInstanceOf(JsonIntegerSchema::class.java, json.properties()["count"])
    }

    @Test
    fun `should carry description on integer field`() {
        val json = objWith(ToolSchemaField("count", ToolSchema.integer("A count.")))
        assertEquals("A count.", (json.properties()["count"] as JsonIntegerSchema).description())
    }

    @Test
    fun `should produce JsonNumberSchema for NUMBER field`() {
        val json = objWith(ToolSchemaField("ratio", ToolSchema.number("A ratio.")))
        assertInstanceOf(JsonNumberSchema::class.java, json.properties()["ratio"])
    }

    @Test
    fun `should produce JsonBooleanSchema for BOOLEAN field`() {
        val json = objWith(ToolSchemaField("flag", ToolSchema.boolean("A flag.")))
        assertInstanceOf(JsonBooleanSchema::class.java, json.properties()["flag"])
    }

    // ── toJsonObjectSchema — enum ─────────────────────────────────────────────

    @Test
    fun `should produce JsonEnumSchema for STRING field with enum values`() {
        val json = objWith(ToolSchemaField("dir",
            ToolSchema.string("Direction.", enum = listOf("N", "S", "E", "W"))))
        assertInstanceOf(JsonEnumSchema::class.java, json.properties()["dir"])
    }

    @Test
    fun `should carry all enum values`() {
        val json = objWith(ToolSchemaField("dir",
            ToolSchema.string(enum = listOf("N", "S", "E", "W"))))
        assertEquals(listOf("N", "S", "E", "W"),
            (json.properties()["dir"] as JsonEnumSchema).enumValues())
    }

    @Test
    fun `should carry description on enum field`() {
        val json = objWith(ToolSchemaField("dir",
            ToolSchema.string("Pick a direction.", enum = listOf("N", "S"))))
        assertEquals("Pick a direction.",
            (json.properties()["dir"] as JsonEnumSchema).description())
    }

    @Test
    fun `should produce JsonStringSchema for STRING field without enum`() {
        val json = objWith(ToolSchemaField("name", ToolSchema.string("Plain string.")))
        assertInstanceOf(JsonStringSchema::class.java, json.properties()["name"])
    }

    // ── toJsonObjectSchema — nested object ────────────────────────────────────

    @Test
    fun `should produce nested JsonObjectSchema for OBJECT field`() {
        val inner = ToolSchema.obj(description = "Inner.", properties = listOf(
            ToolSchemaField("x", ToolSchema.integer())
        ))
        val json = objWith(ToolSchemaField("nested", inner))
        assertInstanceOf(JsonObjectSchema::class.java, json.properties()["nested"])
    }

    @Test
    fun `should carry properties inside nested object`() {
        val inner = ToolSchema.obj(properties = listOf(ToolSchemaField("x", ToolSchema.integer())))
        val json = objWith(ToolSchemaField("nested", inner))
        val nested = json.properties()["nested"] as JsonObjectSchema
        assertInstanceOf(JsonIntegerSchema::class.java, nested.properties()["x"])
    }

    // ── toJsonObjectSchema — array field ──────────────────────────────────────

    @Test
    fun `should produce JsonArraySchema for ARRAY of strings field`() {
        val json = objWith(ToolSchemaField("tags", ToolSchema.array(ToolSchema.string())))
        assertInstanceOf(JsonArraySchema::class.java, json.properties()["tags"])
    }

    @Test
    fun `should carry string items inside array field`() {
        val json = objWith(ToolSchemaField("tags", ToolSchema.array(ToolSchema.string("A tag."))))
        val arr = json.properties()["tags"] as JsonArraySchema
        assertInstanceOf(JsonStringSchema::class.java, arr.items())
    }

    @Test
    fun `should carry object items inside array field`() {
        val itemSchema = ToolSchema.obj(properties = listOf(ToolSchemaField("id", ToolSchema.string())))
        val json = objWith(ToolSchemaField("rows", ToolSchema.array(itemSchema)))
        val arr = json.properties()["rows"] as JsonArraySchema
        assertInstanceOf(JsonObjectSchema::class.java, arr.items())
    }

    // ── toJsonObjectSchema — required / optional ──────────────────────────────

    @Test
    fun `should list required fields in required`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(properties = listOf(
            ToolSchemaField("req", ToolSchema.string(), required = true),
            ToolSchemaField("opt", ToolSchema.string(), required = false),
        )))
        assertTrue(json.required().contains("req"))
    }

    @Test
    fun `should not list optional fields in required`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(properties = listOf(
            ToolSchemaField("req", ToolSchema.string(), required = true),
            ToolSchemaField("opt", ToolSchema.string(), required = false),
        )))
        assertFalse(json.required().contains("opt"))
    }

    @Test
    fun `should produce null or empty required when no required fields`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(properties = listOf(
            ToolSchemaField("opt", ToolSchema.string(), required = false),
        )))
        assertTrue(json.required().isNullOrEmpty())
    }

    // ── toJsonObjectSchema — additionalProperties ─────────────────────────────

    @Test
    fun `should propagate additionalProperties false`() {
        val json = ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(additionalProperties = false))
        assertFalse(json.additionalProperties())
    }

    // ── toJsonSchemaElement — all types ──────────────────────────────────────

    @Test
    fun `should produce JsonStringSchema for STRING element`() {
        assertInstanceOf(JsonStringSchema::class.java,
            ToolSchemaConverter.toJsonSchemaElement(ToolSchema.string("A string.")))
    }

    @Test
    fun `should carry description on string element`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(ToolSchema.string("Desc.")) as JsonStringSchema
        assertEquals("Desc.", el.description())
    }

    @Test
    fun `should produce JsonEnumSchema for STRING element with enum`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(ToolSchema.string(enum = listOf("A", "B")))
        assertInstanceOf(JsonEnumSchema::class.java, el)
        assertEquals(listOf("A", "B"), (el as JsonEnumSchema).enumValues())
    }

    @Test
    fun `should produce JsonIntegerSchema for INTEGER element`() {
        assertInstanceOf(JsonIntegerSchema::class.java,
            ToolSchemaConverter.toJsonSchemaElement(ToolSchema.integer()))
    }

    @Test
    fun `should produce JsonNumberSchema for NUMBER element`() {
        assertInstanceOf(JsonNumberSchema::class.java,
            ToolSchemaConverter.toJsonSchemaElement(ToolSchema.number()))
    }

    @Test
    fun `should produce JsonBooleanSchema for BOOLEAN element`() {
        assertInstanceOf(JsonBooleanSchema::class.java,
            ToolSchemaConverter.toJsonSchemaElement(ToolSchema.boolean()))
    }

    @Test
    fun `should produce JsonObjectSchema for OBJECT element`() {
        assertInstanceOf(JsonObjectSchema::class.java,
            ToolSchemaConverter.toJsonSchemaElement(ToolSchema.obj()))
    }

    @Test
    fun `should produce JsonArraySchema for ARRAY of strings element`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(ToolSchema.array(ToolSchema.string()))
        assertInstanceOf(JsonArraySchema::class.java, el)
        assertInstanceOf(JsonStringSchema::class.java, (el as JsonArraySchema).items())
    }

    @Test
    fun `should produce JsonArraySchema for ARRAY of objects element`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(
            ToolSchema.array(ToolSchema.obj(properties = listOf(ToolSchemaField("x", ToolSchema.string()))))
        )
        assertInstanceOf(JsonArraySchema::class.java, el)
        assertInstanceOf(JsonObjectSchema::class.java, (el as JsonArraySchema).items())
    }

    @Test
    fun `should produce nested JsonArraySchema for ARRAY of arrays element`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(
            ToolSchema.array(ToolSchema.array(ToolSchema.string()))
        )
        assertInstanceOf(JsonArraySchema::class.java, el)
        assertInstanceOf(JsonArraySchema::class.java, (el as JsonArraySchema).items())
    }

    @Test
    fun `should carry description on array element`() {
        val el = ToolSchemaConverter.toJsonSchemaElement(
            ToolSchema.array(ToolSchema.string(), description = "List of tags.")
        ) as JsonArraySchema
        assertEquals("List of tags.", el.description())
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private fun objWith(vararg fields: ToolSchemaField): JsonObjectSchema =
        ToolSchemaConverter.toJsonObjectSchema(ToolSchema.obj(properties = fields.toList()))
}
