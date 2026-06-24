package io.qpointz.mill.data.odata.annotation

/**
 * One CSDL annotation to emit on an EDM model element in {@code $metadata}.
 *
 * @property term qualified term name, typically using a vocabulary alias (e.g. {@code Core.Description})
 * @property stringValue annotation value for OData string-typed terms
 */
data class EdmCsdlAnnotation(
    val term: String,
    val stringValue: String,
)
