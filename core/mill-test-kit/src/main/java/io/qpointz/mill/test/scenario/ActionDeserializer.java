package io.qpointz.mill.test.scenario;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom deserializer for Action that handles key-based YAML structure.
 * <p>
 * Handles three cases:
 * <ol>
 *   <li>Object value: {@code { "do-get": { "message": -1 } }} → key="do-get", params={message: -1}</li>
 *   <li>String value: {@code { "do-ask": "question" }} → key="do-ask", params={"value": "question"}</li>
 *   <li>List value: {@code { "check-data": [{ "expect": {...} }] }} → key="check-data", params={"items": [...]}</li>
 * </ol>
 */
public class ActionDeserializer extends ValueDeserializer<Action> {

    @Override
    public Action deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);

        if (!node.isObject()) {
            return ctxt.reportInputMismatch(Action.class, "Action must be an object");
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
        if (!fields.hasNext()) {
            return ctxt.reportInputMismatch(Action.class, "Action object must have exactly one field");
        }

        Map.Entry<String, JsonNode> entry = fields.next();
        String key = entry.getKey();
        JsonNode value = entry.getValue();

        if (fields.hasNext()) {
            return ctxt.reportInputMismatch(Action.class, "Action object must have exactly one field, found multiple");
        }

        Map<String, Object> params = convertValueToParams(value);

        java.util.Optional<String> name = java.util.Optional.empty();
        if (params.containsKey("name") && params.get("name") instanceof String) {
            name = java.util.Optional.of((String) params.remove("name"));
        }

        return new Action(key, name, params);
    }

    private Map<String, Object> convertValueToParams(JsonNode value) {
        Map<String, Object> params = new HashMap<>();

        if (value.isObject()) {
            for (Map.Entry<String, JsonNode> e : value.properties()) {
                params.put(e.getKey(), convertJsonNode(e.getValue()));
            }
        } else if (value.isTextual()) {
            params.put("value", value.asText());
        } else if (value.isArray()) {
            ArrayList<Object> items = new ArrayList<>();
            value.forEach(item -> items.add(convertJsonNode(item)));
            params.put("items", items);
        } else if (value.isNumber()) {
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
            params.put("value", value.asBoolean());
        } else if (value.isNull()) {
            // params remains empty
        } else {
            params.put("value", value.asText());
        }

        return params;
    }

    private Object convertJsonNode(JsonNode node) {
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonNode> e : node.properties()) {
                map.put(e.getKey(), convertJsonNode(e.getValue()));
            }
            return map;
        } else if (node.isArray()) {
            ArrayList<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(convertJsonNode(item)));
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
