package io.qpointz.mill.metadata.service

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JsonSchemaFacetContentValidatorTest {

    private lateinit var validator: JsonSchemaFacetContentValidator

    @BeforeEach
    fun setUp() {
        validator = JsonSchemaFacetContentValidator()
    }

    @Test
    fun shouldPass_validData() {
        val schema = mapOf<String, Any>(
            "type" to "object",
            "properties" to mapOf("name" to mapOf("type" to "string"), "count" to mapOf("type" to "integer")),
            "required" to listOf("name")
        )
        val result = validator.validate(schema, mapOf("name" to "test", "count" to 42))
        assertTrue(result.valid)
    }

    @Test
    fun shouldFail_missingRequiredField() {
        val schema = mapOf<String, Any>(
            "type" to "object",
            "properties" to mapOf("name" to mapOf("type" to "string")),
            "required" to listOf("name")
        )
        val result = validator.validate(schema, mapOf("count" to 42))
        assertFalse(result.valid)
        assertFalse(result.errors.isEmpty())
    }

    @Test
    fun shouldFail_wrongType() {
        val schema = mapOf<String, Any>(
            "type" to "object",
            "properties" to mapOf("count" to mapOf("type" to "integer"))
        )
        val result = validator.validate(schema, mapOf("count" to "not-a-number"))
        assertFalse(result.valid)
    }

    @Test
    fun shouldPass_noRequiredFields() {
        val schema = mapOf<String, Any>(
            "type" to "object",
            "properties" to mapOf("name" to mapOf("type" to "string"))
        )
        val result = validator.validate(schema, emptyMap<String, Any>())
        assertTrue(result.valid)
    }
}
