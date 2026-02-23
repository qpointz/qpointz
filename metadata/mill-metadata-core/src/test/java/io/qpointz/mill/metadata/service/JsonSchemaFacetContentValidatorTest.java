package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSchemaFacetContentValidatorTest {

    private JsonSchemaFacetContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JsonSchemaFacetContentValidator();
    }

    @Test
    void shouldPass_validData() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string"),
                        "count", Map.of("type", "integer")
                ),
                "required", List.of("name")
        );

        Map<String, Object> data = Map.of("name", "test", "count", 42);

        ValidationResult result = validator.validate(schema, data);
        assertTrue(result.valid());
    }

    @Test
    void shouldFail_missingRequiredField() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string")
                ),
                "required", List.of("name")
        );

        Map<String, Object> data = Map.of("count", 42);

        ValidationResult result = validator.validate(schema, data);
        assertFalse(result.valid());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void shouldFail_wrongType() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "count", Map.of("type", "integer")
                )
        );

        Map<String, Object> data = Map.of("count", "not-a-number");

        ValidationResult result = validator.validate(schema, data);
        assertFalse(result.valid());
    }

    @Test
    void shouldPass_noRequiredFields() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string")
                )
        );

        Map<String, Object> data = Map.of();

        ValidationResult result = validator.validate(schema, data);
        assertTrue(result.valid());
    }
}
