package io.qpointz.mill.ai.nlsql.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qpointz.mill.utils.JsonUtils;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
/**
 * JPA converter for storing arbitrary maps as JSON strings.
 */
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    /**
     * Serializes map content to JSON for database storage.
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> customerInfo) {

        String customerInfoJson = null;
        try {
            customerInfoJson = JsonUtils.defaultJsonMapper().writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return customerInfoJson;
    }

    /**
     * Deserializes JSON column into a map.
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String customerInfoJSON) {

        Map<String, Object> customerInfo = null;
        try {
            customerInfo = JsonUtils.defaultJsonMapper().readValue(customerInfoJSON, Map.class);
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }
        return customerInfo;
    }

}
