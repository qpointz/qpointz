package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Function argument specification for SQL functions.
 * 
 * @param name Argument name
 * @param type Argument type (e.g., "STRING", "INTEGER", "DECIMAL", "ENUM", "PATTERN", "COLUMN_REF")
 * @param required Whether the argument is required
 * @param default_ Optional default value
 * @param min Optional minimum value (for numeric types)
 * @param max Optional maximum value (for numeric types)
 * @param regex Optional regex pattern (for pattern types)
 * @param enum_ Optional enum values (for ENUM type)
 * @param multi Whether multiple values are allowed
 * @param variadic Whether the argument is variadic (can accept multiple values)
 * @param notes Optional notes about the argument (can be String or List<String>)
 */
public record FunctionArg(
    @JsonProperty("name") String name,
    @JsonProperty("type") String type,
    @JsonProperty("required") Boolean required,
    @JsonProperty("default") Optional<String> default_,
    @JsonProperty("min") Optional<Integer> min,
    @JsonProperty("max") Optional<Integer> max,
    @JsonProperty("regex") Optional<String> regex,
    @JsonProperty("enum") Optional<List<String>> enum_,
    @JsonProperty("multi") Optional<Boolean> multi,
    @JsonProperty("variadic") Optional<Boolean> variadic,
    @JsonProperty("notes") Optional<Object> notes
) {}
