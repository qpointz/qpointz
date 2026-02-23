package io.qpointz.mill.metadata.domain.core

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.metadata.domain.AbstractFacet
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.ValidationException

/** Normalization mappings between user terms and physical values. */
open class ValueMappingFacet(
    @JsonProperty("context")
    var context: String? = null,

    @JsonProperty("similarity-threshold")
    var similarityThreshold: Double? = null,

    @JsonProperty("mappings")
    var mappings: MutableList<ValueMapping> = mutableListOf(),

    @JsonProperty("sources")
    var sources: MutableList<ValueMappingSource> = mutableListOf()
) : AbstractFacet() {

    /** Single term-to-value mapping entry. */
    data class ValueMapping(
        @JsonProperty("user-term") val userTerm: String? = null,
        @JsonProperty("database-value") val databaseValue: String? = null,
        @JsonProperty("display-value") val displayValue: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("language") val language: String = "en",
        @JsonProperty("aliases") val aliases: List<String> = emptyList()
    )

    /** External source definition that can populate mapping entries. */
    data class ValueMappingSource(
        @JsonProperty("type") val type: String? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("definition") @JsonAlias("sql") val definition: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("enabled") val enabled: Boolean = true,
        @JsonProperty("cronExpression") val cronExpression: String? = null,
        @JsonProperty("cache-ttl-seconds") val cacheTtlSeconds: Int = 3600
    ) {
        val sql: String? get() = definition
        val cacheTtl: Int get() = cacheTtlSeconds
    }

    override val facetType: String get() = "value-mapping"

    override fun validate() {
        for (m in mappings) {
            if (m.userTerm.isNullOrEmpty()) throw ValidationException("ValueMappingFacet: userTerm is required for mapping")
            if (m.databaseValue.isNullOrEmpty()) throw ValidationException("ValueMappingFacet: databaseValue is required for mapping: ${m.userTerm}")
        }
        for (s in sources) {
            if (s.name.isNullOrEmpty()) throw ValidationException("ValueMappingFacet: name is required for source")
            if (s.sql.isNullOrEmpty()) throw ValidationException("ValueMappingFacet: definition/sql is required for source: ${s.name}")
        }
    }

    override fun merge(other: MetadataFacet): MetadataFacet {
        if (other !is ValueMappingFacet) return this
        other.context?.let { context = it }
        other.similarityThreshold?.let { similarityThreshold = it }
        if (other.mappings.isNotEmpty()) {
            val merged = mappings.toMutableList()
            for (om in other.mappings) {
                if (merged.none { it.userTerm == om.userTerm && it.databaseValue == om.databaseValue }) merged.add(om)
            }
            mappings = merged
        }
        if (other.sources.isNotEmpty()) {
            val merged = sources.toMutableList()
            for (os in other.sources) {
                if (merged.none { it.name == os.name }) merged.add(os)
            }
            sources = merged
        }
        return this
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ValueMappingFacet && mappings == other.mappings)
    override fun hashCode(): Int = mappings.hashCode()
}
