package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for date/time literal syntaxes in SQL dialects.
 * 
 * @param date DATE literal configuration
 * @param time TIME literal configuration
 * @param timestamp TIMESTAMP literal configuration
 * @param interval INTERVAL literal configuration
 */
public record DatesTimes(
    @JsonProperty("date") DateTimeLiteral date,
    @JsonProperty("time") DateTimeLiteral time,
    @JsonProperty("timestamp") DateTimeLiteral timestamp,
    @JsonProperty("interval") IntervalLiteral interval
) {}
