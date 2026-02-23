package io.qpointz.mill.metadata.domain

/** Canonical metadata entity kinds persisted in the metadata repository. */
enum class MetadataType {
    CATALOG, SCHEMA, TABLE, ATTRIBUTE, CONCEPT
}

/** Supported target kinds for facet applicability rules. */
enum class MetadataTargetType {
    CATALOG, SCHEMA, TABLE, ATTRIBUTE, CONCEPT, ANY
}

/** Cardinality options used by relation facets. */
enum class RelationCardinality {
    ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
}

/** Semantic relation category used by relation facets. */
enum class RelationType {
    FOREIGN_KEY, LOGICAL, HIERARCHICAL
}

/** Data sensitivity tags for descriptive metadata. */
enum class DataClassification {
    PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
}

/** Origin of concept definitions in concept facets. */
enum class ConceptSource {
    MANUAL, INFERRED, NL2SQL
}
