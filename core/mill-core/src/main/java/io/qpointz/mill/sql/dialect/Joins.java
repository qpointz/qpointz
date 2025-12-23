package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for JOIN syntax in SQL dialects.
 * 
 * @param style Join style (e.g., "explicit" or "implicit")
 * @param crossJoin Cross join configuration
 * @param innerJoin Inner join configuration
 * @param leftJoin Left join configuration
 * @param rightJoin Right join configuration
 * @param fullJoin Full/outer join configuration
 * @param onClause ON clause configuration
 */
public record Joins(
    @JsonProperty("style") String style,
    @JsonProperty("cross-join") JoinSpec crossJoin,
    @JsonProperty("inner-join") JoinSpec innerJoin,
    @JsonProperty("left-join") JoinSpec leftJoin,
    @JsonProperty("right-join") JoinSpec rightJoin,
    @JsonProperty("full-join") JoinSpec fullJoin,
    @JsonProperty("on-clause") OnClause onClause
) {}
