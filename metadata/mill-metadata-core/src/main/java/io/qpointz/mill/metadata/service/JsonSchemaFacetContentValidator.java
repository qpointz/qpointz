package io.qpointz.mill.metadata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.qpointz.mill.metadata.domain.ValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public class JsonSchemaFacetContentValidator implements FacetContentValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;

    public JsonSchemaFacetContentValidator() {
        this.objectMapper = new ObjectMapper();
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    @Override
    public ValidationResult validate(Map<String, Object> contentSchema, Object facetData) {
        try {
            JsonNode schemaNode = objectMapper.valueToTree(contentSchema);
            JsonSchema schema = schemaFactory.getSchema(schemaNode);

            JsonNode dataNode = objectMapper.valueToTree(facetData);
            Set<ValidationMessage> errors = schema.validate(dataNode);

            if (errors.isEmpty()) {
                return ValidationResult.ok();
            }

            return ValidationResult.fail(
                    errors.stream()
                            .map(ValidationMessage::getMessage)
                            .toList());
        } catch (Exception e) {
            log.warn("JSON Schema validation error", e);
            return ValidationResult.fail("Schema validation error: " + e.getMessage());
        }
    }
}
