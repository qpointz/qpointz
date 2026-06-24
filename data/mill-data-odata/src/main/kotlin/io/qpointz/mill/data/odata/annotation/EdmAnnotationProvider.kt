package io.qpointz.mill.data.odata.annotation

/**
 * Supplies facet-derived CSDL annotations for a physical schema.
 */
fun interface EdmAnnotationProvider {

    /**
     * @param schemaName physical schema name (OData service container name)
     * @return annotations for that schema; empty when unknown or not yet built
     */
    fun annotationsForSchema(schemaName: String): EdmAnnotationModel
}
