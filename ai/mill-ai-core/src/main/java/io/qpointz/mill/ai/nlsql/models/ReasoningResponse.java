package io.qpointz.mill.ai.nlsql.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.ai.nlsql.IntentTypes;
import io.qpointz.mill.ai.nlsql.SchemaScope;
import io.qpointz.mill.ai.nlsql.SchemaStrategy;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReasoningResponse(
        @JsonProperty(value = "query") String query,
        @JsonProperty(value = "intent", required = true) String intent,
        @JsonProperty("plannedIntent") String plannedIntent,
        @JsonProperty("requiredTables") List<IntentTable> requiredTables,
        @JsonProperty("schemaScope") SchemaScope schemaScope,
        @JsonProperty("schemaStrategy") SchemaStrategy schemaStrategy,
        @JsonProperty(value = "language", required = false, defaultValue = "en") String language,
        @JsonProperty(value = "hints", required = false) List<String> hints,
        @JsonProperty(value = "hintMessage", required = false) String hintMessage,
        @JsonProperty(value = "sqlFeatures", required = false, defaultValue = "") SqlDialect.SqlFeatures sqlFeatures

) {
    public record IntentTable(
            @JsonProperty("schema") String schema,
            @JsonProperty("name") String name,
            @JsonProperty("includeConstraints") boolean includeConstraints
    ) {}

}
