package io.qpointz.mill.metadata.domain.core

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.metadata.domain.MetadataFacet

/** Normalization mappings between user terms and physical values. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ValueMappingFacet(
    @JsonProperty("context")
    val context: String? = null,

    @JsonProperty("similarity-threshold")
    val similarityThreshold: Double? = null,

    @JsonProperty("mappings")
    val mappings: List<ValueMapping> = emptyList(),

    @JsonProperty("sources")
    val sources: List<ValueMappingSource> = emptyList(),

    override val facetType: String = "value-mapping",
) : MetadataFacet {

    /** Single term-to-value mapping entry. */
    data class ValueMapping(
        @JsonProperty("user-term") val userTerm: String? = null,
        @JsonProperty("database-value") val databaseValue: String? = null,
        @JsonProperty("display-value") val displayValue: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("language") val language: String = "en",
        @JsonProperty("aliases") val aliases: List<String> = emptyList(),
    )

    /** External source definition that can populate mapping entries. */
    data class ValueMappingSource(
        @JsonProperty("type") val type: String? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("definition") @JsonAlias("sql") val definition: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("enabled") val enabled: Boolean = true,
        @JsonProperty("cronExpression") val cronExpression: String? = null,
        @JsonProperty("cache-ttl-seconds") val cacheTtlSeconds: Int = 3600,
    ) {
        val sql: String? get() = definition
        val cacheTtl: Int get() = cacheTtlSeconds
    }
}
