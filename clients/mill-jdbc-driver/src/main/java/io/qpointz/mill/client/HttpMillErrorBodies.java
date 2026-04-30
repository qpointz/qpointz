package io.qpointz.mill.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

/**
 * Parses HTTP API error payloads (RFC 9457 Problem Details, legacy Spring Boot/Mill envelopes,
 * legacy GRPC JSON {@code code}/{@code message} pairs from the HTTP access layer,
 * or plain text) into a JDBC-facing single-line diagnostic string.
 */
@UtilityClass
class HttpMillErrorBodies {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int BODY_SNIPPET_MAX = 2000;

    /**
     * Builds a JDBC-facing message from HTTP failure metadata and body bytes.
     *
     * @param httpStatus   HTTP status code
     * @param reasonPhrase optional reason phrase (often generic; retained as last-resort filler)
     * @param bodyBytes    raw problem body bytes; may be null or empty
     */
    static String describe(int httpStatus, String reasonPhrase, byte[] bodyBytes) {
        String bodyChars = truncateToText(bodyBytes);
        ParseResult parsed = parseBody(bodyChars);
        return buildLine(httpStatus, reasonPhrase, bodyChars, parsed);
    }

    private static ParseResult parseBody(String bodyChars) {
        if (bodyChars == null || bodyChars.isBlank()) {
            return ParseResult.empty();
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(bodyChars.trim());
        } catch (Exception e) {
            return ParseResult.plainSnippet(bodyChars);
        }
        if (!root.isObject()) {
            return ParseResult.plainSnippet(bodyChars);
        }
        JsonNode detail = root.get("detail");
        JsonNode title = root.get("title");
        JsonNode message = root.get("message");
        JsonNode error = root.get("error");
        JsonNode grpcCode = root.get("code");
        JsonNode type = root.get("type");
        JsonNode trace = firstDefinedScalar(root, "traceId", "trace_id");
        return ParseResult.ok(detail, title, message, error, grpcCode, trace, type);
    }

    private static JsonNode firstDefinedScalar(JsonNode obj, String... fieldNames) {
        for (String name : fieldNames) {
            if (obj.hasNonNull(name)) {
                return obj.get(name);
            }
        }
        return null;
    }

    private static String truncateToText(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return "";
        }
        if (raw.length > BODY_SNIPPET_MAX) {
            return new String(raw, 0, BODY_SNIPPET_MAX, StandardCharsets.UTF_8) + "...(truncated)";
        }
        return new String(raw, StandardCharsets.UTF_8);
    }

    private static String buildLine(int httpStatus, String reasonPhrase, String bodyChars, ParseResult parsed) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("[HTTP ").append(httpStatus).append("]");
        String semantic = parsed.primaryLine();
        if (semantic != null && !semantic.isBlank()) {
            sb.append(' ').append(semantic.trim());
        } else if (parsed.snippetFallback() != null) {
            sb.append(' ').append(parsed.snippetFallback().trim());
        } else if (bodyChars != null && !bodyChars.isBlank()) {
            sb.append(' ').append(bodyChars.trim());
        } else if (reasonPhrase != null && !reasonPhrase.isBlank()) {
            sb.append(' ').append(reasonPhrase.trim());
        }

        String trace = parsed.traceIdText();
        if (trace != null && !trace.isBlank()) {
            sb.append(" [traceId=").append(trace.trim()).append(']');
        }
        String typeText = parsed.problemTypeText();
        if (typeText != null && !typeText.isBlank()) {
            sb.append(" (type=").append(typeText.trim()).append(')');
        }
        return sb.toString();
    }

    /** Prefer RFC detail, then legacy message/error/title/code. */
    private record ParseResult(
            JsonNode detail,
            JsonNode title,
            JsonNode message,
            JsonNode error,
            JsonNode grpcCode,
            JsonNode traceId,
            JsonNode type,
            String snippetFallback
    ) {

        static ParseResult empty() {
            return new ParseResult(null, null, null, null, null, null, null, null);
        }

        static ParseResult plainSnippet(String body) {
            String s = body == null ? "" : body.trim();
            return new ParseResult(null, null, null, null, null, null, null, s.isEmpty() ? null : s);
        }

        static ParseResult ok(
                JsonNode detail,
                JsonNode title,
                JsonNode message,
                JsonNode error,
                JsonNode grpcCode,
                JsonNode traceId,
                JsonNode type) {
            return new ParseResult(detail, title, message, error, grpcCode, traceId, type, null);
        }

        String traceIdText() {
            return asScalarText(traceId);
        }

        String problemTypeText() {
            return asScalarText(type);
        }

        String primaryLine() {
            String dt = nonEmptyText(detail);
            String grpcLabel = nonEmptyScalar(grpcCode);
            if (dt != null && grpcLabel != null) {
                return dt + " (code=" + grpcLabel + ")";
            }
            if (dt != null) {
                return dt;
            }
            String ms = nonEmptyText(message);
            if (ms != null) {
                return ms;
            }
            String er = nonEmptyText(error);
            if (er != null) {
                return er;
            }
            String ti = nonEmptyText(title);
            if (ti != null) {
                return ti;
            }
            return grpcLabel;
        }

        private static String nonEmptyScalar(JsonNode n) {
            if (n == null || n.isNull() || n.isMissingNode()) {
                return null;
            }
            if (n.isTextual()) {
                String t = n.asText();
                return t.isBlank() ? null : t;
            }
            if (n.isNumber()) {
                return n.numberValue().toString();
            }
            if (n.isBoolean()) {
                return Boolean.toString(n.booleanValue());
            }
            return null;
        }

        private static String nonEmptyText(JsonNode n) {
            return nonEmptyScalar(n);
        }

        private static String asScalarText(JsonNode n) {
            return nonEmptyScalar(n);
        }
    }
}
