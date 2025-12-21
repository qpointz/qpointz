package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom deserializer for Action that handles key-based YAML structure.
 * 
 * Handles three cases:
 * 1. Object value: { "do-get": { "message": -1 } } → key="do-get", params={message: -1}
 * 2. String value: { "do-ask": "question" } → key="do-ask", params={"value": "question"}
 * 3. List value: { "check-data": [{ "expect": {...} }] } → key="check-data", params={"items": [...]}
 */
public class ActionDeserializer extends JsonDeserializer<Action> {

    @Override
    public Action deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (!node.isObject()) {
            throw new IOException("Action must be an object");
        }

        // Extract the single key-value pair
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        if (!fields.hasNext()) {
            throw new IOException("Action object must have exactly one field");
        }

        Map.Entry<String, JsonNode> entry = fields.next();
        String key = entry.getKey();
        JsonNode value = entry.getValue();

        // Check if there are multiple fields (shouldn't happen)
        if (fields.hasNext()) {
            throw new IOException("Action object must have exactly one field, found multiple");
        }

        // Convert value to params map based on type
        Map<String, Object> params = convertValueToParams(value, mapper);

        // Extract "name" from params if present (it's a special field)
        java.util.Optional<String> name = java.util.Optional.empty();
        if (params.containsKey("name") && params.get("name") instanceof String) {
            name = java.util.Optional.of((String) params.remove("name"));
        }

        return new Action(key, name, params);
    }

    private Map<String, Object> convertValueToParams(JsonNode value, ObjectMapper mapper) {
        Map<String, Object> params = new HashMap<>();

        if (value.isObject()) {
            // Object value: convert all fields to params
            value.fields().forEachRemaining(entry -> {
                params.put(entry.getKey(), convertJsonNode(entry.getValue(), mapper));
            });
        } else if (value.isTextual()) {
            // String value: store in "value" key
            params.put("value", value.asText());
        } else if (value.isArray()) {
            // List value: store in "items" key
            ArrayList<Object> items = new ArrayList<>();
            value.forEach(item -> items.add(convertJsonNode(item, mapper)));
            params.put("items", items);
        } else if (value.isNumber()) {
            // Number value: store in "value" key
            if (value.isInt()) {
                params.put("value", value.asInt());
            } else if (value.isLong()) {
                params.put("value", value.asLong());
            } else if (value.isDouble() || value.isFloat()) {
                params.put("value", value.asDouble());
            } else {
                params.put("value", value.numberValue());
            }
        } else if (value.isBoolean()) {
            // Boolean value: store in "value" key
            params.put("value", value.asBoolean());
        } else if (value.isNull()) {
            // Null value: empty params
            // params remains empty
        } else {
            // Fallback: convert to string
            params.put("value", value.asText());
        }

        return params;
    }

    private Object convertJsonNode(JsonNode node, ObjectMapper mapper) {
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                map.put(entry.getKey(), convertJsonNode(entry.getValue(), mapper));
            });
            return map;
        } else if (node.isArray()) {
            ArrayList<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(convertJsonNode(item, mapper)));
            return list;
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            if (node.isInt()) {
                return node.asInt();
            } else if (node.isLong()) {
                return node.asLong();
            } else if (node.isDouble() || node.isFloat()) {
                return node.asDouble();
            } else {
                return node.numberValue();
            }
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isNull()) {
            return null;
        } else {
            return node.asText();
        }
    }
}


