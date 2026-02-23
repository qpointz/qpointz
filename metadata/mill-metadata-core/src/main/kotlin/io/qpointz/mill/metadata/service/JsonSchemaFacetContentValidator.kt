package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import io.qpointz.mill.metadata.domain.ValidationResult
import org.slf4j.LoggerFactory

/** JSON Schema v7 validator used for optional facet content enforcement. */
class JsonSchemaFacetContentValidator : FacetContentValidator {

    private val objectMapper = ObjectMapper()
    private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

    override fun validate(contentSchema: Map<String, Any?>, facetData: Any?): ValidationResult {
        return try {
            val schemaNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(contentSchema)
            val schema = schemaFactory.getSchema(schemaNode)
            val dataNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(facetData)
            val errors = schema.validate(dataNode)
            if (errors.isEmpty()) ValidationResult.ok()
            else ValidationResult.fail(errors.map { it.message })
        } catch (e: Exception) {
            log.warn("JSON Schema validation error", e)
            ValidationResult.fail("Schema validation error: ${e.message}")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JsonSchemaFacetContentValidator::class.java)
    }
}
