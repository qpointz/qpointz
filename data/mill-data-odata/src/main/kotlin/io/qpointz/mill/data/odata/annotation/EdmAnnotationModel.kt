package io.qpointz.mill.data.odata.annotation

/**
 * CSDL annotations keyed by their EDM target for one physical schema.
 *
 * @property annotationsByTarget annotations grouped by model element
 */
class EdmAnnotationModel private constructor(
    private val annotationsByTarget: Map<EdmAnnotationTarget, List<EdmCsdlAnnotation>>,
) {

    /** True when no annotations are held. */
    val isEmpty: Boolean get() = annotationsByTarget.isEmpty()

    /**
     * @param entityTypeName entity type / table name
     * @return annotations for the entity type, or empty when none
     */
    fun entityType(entityTypeName: String): List<EdmCsdlAnnotation> =
        annotationsByTarget.entries
            .asSequence()
            .filter { (target, _) -> target is EdmAnnotationTarget.EntityType && target.entityTypeName == entityTypeName }
            .flatMap { it.value }
            .toList()

    /**
     * @param entityTypeName entity type / table name
     * @param propertyName structural property / column name
     * @return annotations for the property, or empty when none
     */
    fun structuralProperty(entityTypeName: String, propertyName: String): List<EdmCsdlAnnotation> =
        annotationsByTarget.entries
            .asSequence()
            .filter { (target, _) ->
                target is EdmAnnotationTarget.StructuralProperty &&
                    target.entityTypeName == entityTypeName &&
                    target.propertyName == propertyName
            }
            .flatMap { it.value }
            .toList()

    /**
     * Builds annotation models while constructing an OData EDM for a schema.
     */
    class Builder {

        private val entries = linkedMapOf<EdmAnnotationTarget, MutableList<EdmCsdlAnnotation>>()

        /**
         * @param target model element receiving annotations
         * @param facets resolved facets for that element
         * @param mapper facet-to-annotation mapper
         */
        fun addFromFacets(
            target: EdmAnnotationTarget,
            facets: io.qpointz.mill.data.schema.SchemaFacets,
            mapper: EdmAnnotationMapper,
        ) {
            val mapped = mapper.map(target, facets)
            if (mapped.isEmpty()) {
                return
            }
            entries.computeIfAbsent(target) { mutableListOf() }.addAll(mapped)
        }

        /**
         * @return immutable annotation model
         */
        fun build(): EdmAnnotationModel =
            EdmAnnotationModel(entries.mapValues { it.value.toList() })
    }
}
