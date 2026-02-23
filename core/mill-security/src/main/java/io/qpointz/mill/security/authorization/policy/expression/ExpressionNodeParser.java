package io.qpointz.mill.security.authorization.policy.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parses structured policy expression payloads into {@link ExpressionNode} AST.
 *
 * <p>Disambiguation rules for scalar operands:
 * <ul>
 *   <li>All scalar values are treated as constants by default.</li>
 *   <li>String values prefixed with {@code #ref.} are treated as field references.</li>
 *   <li>Object forms {@code {ref: "field"}} and {@code {const: value}} are also supported.</li>
 * </ul>
 */
public final class ExpressionNodeParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String REF_PREFIX = "#ref.";

    private ExpressionNodeParser() {
    }

    public static ExpressionNode parse(JsonNode node) {
        if (node == null || node.isNull()) {
            return LiteralNode.builder().value(null).build();
        }
        if (node.isTextual()) {
            var text = node.asText();
            if (text.startsWith(REF_PREFIX) && text.length() > REF_PREFIX.length()) {
                return FieldRefNode.builder().fieldName(text.substring(REF_PREFIX.length())).build();
            }
            return LiteralNode.builder().value(text).build();
        }
        if (node.isBoolean()) {
            return LiteralNode.builder().value(node.booleanValue()).build();
        }
        if (node.isNumber()) {
            return LiteralNode.builder().value(node.numberValue()).build();
        }
        if (node.isArray()) {
            throw new IllegalArgumentException("Unexpected top-level array expression: " + node);
        }
        if (!node.isObject()) {
            return LiteralNode.builder().value(toJavaValue(node)).build();
        }

        // Explicit discriminated format (nodeType) is still supported.
        if (node.has("nodeType")) {
            return parseTypedNode(node);
        }

        if (node.has("ref")) {
            return FieldRefNode.builder().fieldName(node.get("ref").asText()).build();
        }
        if (node.has("fieldName") || node.has("fieldIndex")) {
            return FieldRefNode.builder()
                    .fieldName(textOrNull(node.get("fieldName")))
                    .fieldIndex(node.has("fieldIndex") && !node.get("fieldIndex").isNull()
                            ? node.get("fieldIndex").asInt()
                            : null)
                    .build();
        }
        if (node.has("field")) {
            return FieldRefNode.builder().fieldName(node.get("field").asText()).build();
        }
        if (node.has("fieldIndex")) {
            return FieldRefNode.builder().fieldIndex(node.get("fieldIndex").asInt()).build();
        }
        if (node.has("value")) {
            return LiteralNode.builder()
                    .value(toJavaValue(node.get("value")))
                    .dataType(textOrNull(node.get("dataType")))
                    .build();
        }
        if (node.has("const")) {
            return LiteralNode.builder().value(toJavaValue(node.get("const"))).build();
        }
        if (node.has("operator")) {
            return new CallNode(
                    node.path("operator").asText(),
                    parseOperands(node.get("operands"))
            );
        }
        if (node.has("call")) {
            return parseCallObject(node.get("call"));
        }
        if (node.has("between")) {
            return parseBetweenObject(node.get("between"));
        }

        if (node.size() == 1) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            var entry = it.next();
            return parseOperatorCall(entry.getKey(), entry.getValue());
        }

        throw new IllegalArgumentException("Cannot parse expression node: " + node);
    }

    private static ExpressionNode parseTypedNode(JsonNode node) {
        var nodeType = node.path("nodeType").asText();
        return switch (nodeType) {
            case "literal" -> LiteralNode.builder()
                    .value(toJavaValue(node.get("value")))
                    .dataType(textOrNull(node.get("dataType")))
                    .build();
            case "fieldRef" -> FieldRefNode.builder()
                    .fieldName(textOrNull(node.get("fieldName")))
                    .fieldIndex(node.has("fieldIndex") && !node.get("fieldIndex").isNull()
                            ? node.get("fieldIndex").intValue()
                            : null)
                    .build();
            case "call" -> new CallNode(
                    node.path("operator").asText(),
                    parseOperands(node.get("operands"))
            );
            case "cast" -> CastNode.builder()
                    .operand(parse(node.get("operand")))
                    .targetType(textOrNull(node.get("targetType")))
                    .build();
            case "nullCheck" -> NullCheckNode.builder()
                    .operand(parse(node.get("operand")))
                    .negated(node.path("negated").asBoolean(false))
                    .build();
            case "raw" -> RawExpressionNode.builder()
                    .expression(textOrNull(node.get("expression")))
                    .build();
            default -> throw new IllegalArgumentException("Unknown expression nodeType: " + nodeType);
        };
    }

    private static ExpressionNode parseCallObject(JsonNode callNode) {
        if (callNode == null || callNode.isNull()) {
            throw new IllegalArgumentException("call expression must not be null");
        }
        var operator = textOrNull(callNode.get("function"));
        if (operator == null) {
            operator = textOrNull(callNode.get("operator"));
        }
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("call expression requires function/operator");
        }
        return new CallNode(operator, parseOperands(callNode.get("args")));
    }

    private static ExpressionNode parseBetweenObject(JsonNode betweenNode) {
        if (betweenNode == null || betweenNode.isNull()) {
            throw new IllegalArgumentException("between expression must not be null");
        }
        JsonNode fieldNode = betweenNode.get("field");
        if (fieldNode == null) {
            throw new IllegalArgumentException("between expression requires field");
        }
        var value = parse(fieldNode);
        var low = parse(betweenNode.get("low"));
        var high = parse(betweenNode.get("high"));
        return CallNode.between(value, low, high);
    }

    private static ExpressionNode parseOperatorCall(String operator, JsonNode operandNode) {
        if ("not".equalsIgnoreCase(operator)) {
            if (operandNode != null && operandNode.isArray()) {
                if (operandNode.size() != 1) {
                    throw new IllegalArgumentException("not expects exactly one operand");
                }
                return CallNode.not(parse(operandNode.get(0)));
            }
            return CallNode.not(parse(operandNode));
        }
        return new CallNode(operator, parseOperands(operandNode));
    }

    private static List<ExpressionNode> parseOperands(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            var ops = new ArrayList<ExpressionNode>(node.size());
            for (JsonNode item : node) {
                ops.add(parse(item));
            }
            return ops;
        }
        return List.of(parse(node));
    }

    private static Object toJavaValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return MAPPER.convertValue(node, Object.class);
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
