package io.qpointz.mill.ai.nlsql;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy interface responsible for resolving placeholder mappings returned by the NL2SQL
 * model. Implementations can perform simple pass-through logic or consult a {@link ValueRepository}
 * to translate user-friendly terms into canonical database values.
 */
public interface ValueMapper {

    /**
     * Value object representing a single placeholder emitted by the NL2SQL model.
     * Jackson annotations allow the structure to be materialised directly from the JSON payload.
     *
     * @param placeholder placeholder identifier returned by the model
     * @param target fully-qualified target in {@code SCHEMA.TABLE.COLUMN} form
     * @param display human readable description of the placeholder
     * @param resolvedValue value predicted by the model prior to post-processing
     * @param type original placeholder type supplied by the model
     * @param meaning semantic hint that describes how the placeholder should be interpreted
     * @param kind optional additional hint (for example, {@code enum})
     */
    record PlaceholderMapping(
            @JsonProperty("placeholder") String placeholder,
            @JsonProperty("target") String target,
            @JsonProperty("display") String display,
            @JsonProperty("resolved-value") String resolvedValue,
            @JsonProperty("type") String type,
            @JsonProperty("meaning") String meaning,
            @JsonProperty(value = "kind", required = false) String kind
    ) {

        /**
         * Converts the dotted {@link #target} string into a structured identifier that can be used
         * as the key when querying a {@link ValueRepository}.
         *
         * @return list containing schema, table and column identifiers
         */
        public List<String> targetAsId() {
            return Arrays.asList(target.split("\\."));
        }

    }

    /**
     * Result of a value mapping operation, pairing the original placeholder metadata with the value
     * that should ultimately be substituted into the generated SQL.
     *
     * @param placeholder original placeholder context
     * @param mappedValue final resolved value
     */
    record MappedValue(PlaceholderMapping placeholder, String mappedValue) {
    }

    /**
     * Resolve a placeholder value, returning the canonical value that should be inserted into SQL.
     *
     * @param mapping placeholder context supplied by the NL2SQL model
     * @return pairing of the placeholder and the resolved value
     */
    MappedValue mapValue(PlaceholderMapping mapping);

}
