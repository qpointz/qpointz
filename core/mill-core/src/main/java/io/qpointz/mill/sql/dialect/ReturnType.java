package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Return type specification for SQL functions.
 * 
 * @param type The return type (e.g., "STRING", "INTEGER", "DECIMAL", "BOOLEAN", "DATE", "TIMESTAMP")
 * @param nullable Whether the return type is nullable
 */
public record ReturnType(
    @JsonProperty("type") String type,
    @JsonProperty("nullable") Boolean nullable
) {}
