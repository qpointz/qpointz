package io.qpointz.mill.data.odata.annotation

/**
 * Identifies where a CSDL annotation is attached in the service schema.
 */
sealed interface EdmAnnotationTarget {

    /** Physical schema that owns the annotated model element. */
    val schemaName: String

    /**
     * An entity type (physical table) in the schema.
     *
     * @property schemaName physical schema name
     * @property entityTypeName entity type / table name
     */
    data class EntityType(
        override val schemaName: String,
        val entityTypeName: String,
    ) : EdmAnnotationTarget

    /**
     * A structural property (physical column) on an entity type.
     *
     * @property schemaName physical schema name
     * @property entityTypeName entity type / table name
     * @property propertyName column name
     */
    data class StructuralProperty(
        override val schemaName: String,
        val entityTypeName: String,
        val propertyName: String,
    ) : EdmAnnotationTarget
}
