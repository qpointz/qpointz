package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CapabilityManifestTest {

    private val manifest = CapabilityManifest.load("capabilities/test-full.yaml")

    // ── Top-level metadata ────────────────────────────────────────────────────

    @Test
    fun `should load manifest name`() {
        assertEquals("test-full", manifest.name)
    }

    @Test
    fun `should load manifest description`() {
        assertEquals("Comprehensive test capability covering all schema types.", manifest.description)
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    @Test
    fun `should load all declared prompts`() {
        assertEquals(2, manifest.allPrompts.size)
    }

    @Test
    fun `should load prompt id, description and content`() {
        val p = manifest.promptAsset("test.system")
        assertEquals("test.system", p.id)
        assertEquals("System prompt for testing.", p.description)
        assertEquals("Test system prompt content.", p.content)
    }

    @Test
    fun `should trim and preserve multi-line prompt content`() {
        val p = manifest.promptAsset("test.secondary")
        assertTrue(p.content.contains("Multi-line"))
        assertTrue(p.content.contains("prompt content."))
    }

    @Test
    fun `should throw on unknown prompt id`() {
        val ex = assertThrows<IllegalStateException> { manifest.promptAsset("unknown.prompt") }
        assertTrue(ex.message!!.contains("unknown.prompt"))
    }

    // ── Tool binding ──────────────────────────────────────────────────────────

    @Test
    fun `should return ToolDefinition with correct name`() {
        val tool = manifest.tool("no_input") { ToolResult(null) }
        assertEquals("no_input", tool.name)
    }

    @Test
    fun `should populate tool description from yaml`() {
        val tool = manifest.tool("no_input") { ToolResult(null) }
        assertFalse(tool.description.isBlank())
    }

    @Test
    fun `should invoke the bound handler`() {
        val tool = manifest.tool("no_input") { ToolResult("ok") }
        assertEquals("ok", tool.handler.invoke(ToolRequest()).content)
    }

    @Test
    fun `should throw on unknown tool name`() {
        val ex = assertThrows<IllegalStateException> { manifest.tool("nonexistent") { ToolResult(null) } }
        assertTrue(ex.message!!.contains("nonexistent"))
    }

    // ── Input omitted → empty object schema ───────────────────────────────────

    @Test
    fun `should default input schema to empty object when input not declared`() {
        val tool = manifest.tool("no_input") { ToolResult(null) }
        assertEquals(ToolSchemaType.OBJECT, tool.inputSchema.type)
        assertTrue(tool.inputSchema.properties.isEmpty())
    }

    // ── Scalar types ──────────────────────────────────────────────────────────

    @Test
    fun `should load string field type and description`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "str_field" }
        assertEquals(ToolSchemaType.STRING, f.schema.type)
        assertEquals("A string field.", f.schema.description)
    }

    @Test
    fun `should load integer field`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "int_field" }
        assertEquals(ToolSchemaType.INTEGER, f.schema.type)
        assertEquals("An integer field.", f.schema.description)
    }

    @Test
    fun `should load number field`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "num_field" }
        assertEquals(ToolSchemaType.NUMBER, f.schema.type)
    }

    @Test
    fun `should load boolean field`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "bool_field" }
        assertEquals(ToolSchemaType.BOOLEAN, f.schema.type)
    }

    // ── Enum ──────────────────────────────────────────────────────────────────

    @Test
    fun `should load enum values on string field`() {
        val tool = manifest.tool("enum_field") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "direction" }
        assertEquals(ToolSchemaType.STRING, f.schema.type)
        assertEquals(listOf("NORTH", "SOUTH", "EAST", "WEST"), f.schema.enum)
    }

    @Test
    fun `should have null enum on plain string field`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "str_field" }
        assertNull(f.schema.enum)
    }

    // ── Required vs optional ──────────────────────────────────────────────────

    @Test
    fun `should mark field listed in required as required`() {
        val tool = manifest.tool("required_and_optional") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "must_provide" }
        assertTrue(f.required)
    }

    @Test
    fun `should mark field absent from required list as not required`() {
        val tool = manifest.tool("required_and_optional") { ToolResult(null) }
        val f = tool.inputSchema.properties.single { it.name == "may_provide" }
        assertFalse(f.required)
    }

    @Test
    fun `should treat all fields as required when required list is absent`() {
        val tool = manifest.tool("all_scalars") { ToolResult(null) }
        assertTrue(tool.inputSchema.properties.all { it.required })
    }

    // ── Array of strings ──────────────────────────────────────────────────────

    @Test
    fun `should load array output schema with string items`() {
        val tool = manifest.tool("array_of_strings") { ToolResult(null) }
        assertEquals(ToolSchemaType.ARRAY, tool.outputSchema.type)
        assertEquals(ToolSchemaType.STRING, tool.outputSchema.items!!.type)
    }

    @Test
    fun `should carry description on array schema`() {
        val tool = manifest.tool("array_of_strings") { ToolResult(null) }
        assertEquals("List of string values.", tool.outputSchema.description)
    }

    @Test
    fun `should carry description on array items`() {
        val tool = manifest.tool("array_of_strings") { ToolResult(null) }
        assertEquals("A single string element.", tool.outputSchema.items!!.description)
    }

    // ── Array of objects ──────────────────────────────────────────────────────

    @Test
    fun `should load array of objects with correct item type`() {
        val tool = manifest.tool("array_of_objects") { ToolResult(null) }
        assertEquals(ToolSchemaType.OBJECT, tool.outputSchema.items!!.type)
    }

    @Test
    fun `should load properties inside array item object`() {
        val tool = manifest.tool("array_of_objects") { ToolResult(null) }
        val items = tool.outputSchema.items!!
        val name = items.properties.single { it.name == "name" }
        val count = items.properties.single { it.name == "count" }
        assertEquals(ToolSchemaType.STRING, name.schema.type)
        assertEquals(ToolSchemaType.INTEGER, count.schema.type)
    }

    // ── Nested object ─────────────────────────────────────────────────────────

    @Test
    fun `should load nested object property type`() {
        val tool = manifest.tool("nested_object") { ToolResult(null) }
        val inner = tool.inputSchema.properties.single { it.name == "inner" }
        assertEquals(ToolSchemaType.OBJECT, inner.schema.type)
    }

    @Test
    fun `should load description on nested object`() {
        val tool = manifest.tool("nested_object") { ToolResult(null) }
        val inner = tool.inputSchema.properties.single { it.name == "inner" }
        assertEquals("Nested inner object.", inner.schema.description)
    }

    @Test
    fun `should load properties within nested object`() {
        val tool = manifest.tool("nested_object") { ToolResult(null) }
        val inner = tool.inputSchema.properties.single { it.name == "inner" }
        val value = inner.schema.properties.single { it.name == "value" }
        assertEquals(ToolSchemaType.INTEGER, value.schema.type)
    }

    // ── Array of arrays ───────────────────────────────────────────────────────

    @Test
    fun `should load nested array (array of arrays)`() {
        val tool = manifest.tool("array_of_arrays") { ToolResult(null) }
        assertEquals(ToolSchemaType.ARRAY, tool.outputSchema.type)
        assertEquals(ToolSchemaType.ARRAY, tool.outputSchema.items!!.type)
        assertEquals(ToolSchemaType.STRING, tool.outputSchema.items!!.items!!.type)
    }

    // ── ToolSchema init guards ────────────────────────────────────────────────

    @Test
    fun `should reject enum on non-string schema type`() {
        assertThrows<IllegalArgumentException> {
            ToolSchema(type = ToolSchemaType.INTEGER, enum = listOf("A", "B"))
        }
    }

    @Test
    fun `should reject properties on non-object schema type`() {
        assertThrows<IllegalArgumentException> {
            ToolSchema(type = ToolSchemaType.STRING, properties = listOf(
                ToolSchemaField("x", ToolSchema.string())
            ))
        }
    }

    @Test
    fun `should reject items on non-array schema type`() {
        assertThrows<IllegalArgumentException> {
            ToolSchema(type = ToolSchemaType.STRING, items = ToolSchema.string())
        }
    }
}
