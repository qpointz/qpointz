package io.qpointz.mill.ai.core.capability

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import dev.langchain4j.model.chat.request.json.JsonObjectSchema
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
    fun `should return ToolBinding with correct name`() {
        val binding = manifest.tool("no_input") { ToolResult(null) }
        assertEquals("no_input", binding.spec.name())
    }

    @Test
    fun `should populate tool description from yaml`() {
        val binding = manifest.tool("no_input") { ToolResult(null) }
        assertFalse(binding.spec.description().isNullOrBlank())
    }

    @Test
    fun `should invoke the bound handler`() {
        val binding = manifest.tool("no_input") { ToolResult("ok") }
        assertEquals("ok", binding.handler.invoke(ToolRequest()).content)
    }

    @Test
    fun `should throw on unknown tool name`() {
        val ex = assertThrows<IllegalStateException> { manifest.tool("nonexistent") { ToolResult(null) } }
        assertTrue(ex.message!!.contains("nonexistent"))
    }

    // ── Input omitted → empty parameters ──────────────────────────────────────

    @Test
    fun `should default input schema to empty object when input not declared`() {
        val binding = manifest.tool("no_input") { ToolResult(null) }
        val params = binding.spec.parameters()
        assertNotNull(params)
        // Empty object schema has no required properties
        assertTrue(params!!.properties().isNullOrEmpty())
    }

    // ── Tool kind ─────────────────────────────────────────────────────────────

    @Test
    fun `should default tool kind to QUERY when kind not declared`() {
        val binding = manifest.tool("no_input") { ToolResult(null) }
        assertEquals(ToolKind.QUERY, binding.kind)
    }

    @Test
    fun `should resolve explicit kind query to QUERY`() {
        val binding = manifest.tool("explicit_query_tool") { ToolResult(null) }
        assertEquals(ToolKind.QUERY, binding.kind)
    }

    @Test
    fun `should resolve kind capture to CAPTURE`() {
        val binding = manifest.tool("capture_tool") { ToolResult(null) }
        assertEquals(ToolKind.CAPTURE, binding.kind)
    }

    // ── Input properties are converted to LangChain4j spec ───────────────────

    @Test
    fun `should create tool spec with parameters when input declared`() {
        val binding = manifest.tool("all_scalars") { ToolResult(null) }
        val params = binding.spec.parameters()
        assertNotNull(params)
        assertFalse(params!!.properties().isNullOrEmpty())
    }

    @Test
    fun `should include enum property in tool spec`() {
        val binding = manifest.tool("enum_field") { ToolResult(null) }
        val params = binding.spec.parameters()
        assertNotNull(params)
        val directionProp = params!!.properties()["direction"]
        assertNotNull(directionProp)
    }

    @Test
    fun `should respect required fields in tool spec`() {
        val binding = manifest.tool("required_and_optional") { ToolResult(null) }
        val params = binding.spec.parameters()
        assertNotNull(params)
        val required = params!!.required()
        assertNotNull(required)
        assertTrue(required!!.contains("must_provide"))
        assertFalse(required.contains("may_provide"))
    }
}





