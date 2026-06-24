package io.qpointz.mill.data.odata.annotation

/**
 * Well-known OData vocabulary term names used in CSDL annotations.
 *
 * <p>Terms are from [Org.OData.Core.V1](https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Core.V1.md).
 * Annotations use the {@code Core} alias declared in {@link #CORE_VOCABULARY_REFERENCE_URI}.
 */
object ODataVocabularyTerms {

    /** CSDL reference URI for the Core vocabulary include. */
    const val CORE_VOCABULARY_REFERENCE_URI =
        "https://oasis-tcs.github.io/odata-vocabularies/vocabularies/Org.OData.Core.V1.xml"

    /** Namespace included from the Core vocabulary reference. */
    const val CORE_VOCABULARY_NAMESPACE = "Org.OData.Core.V1"

    /** Alias for {@link #CORE_VOCABULARY_NAMESPACE} in generated CSDL. */
    const val CORE_VOCABULARY_ALIAS = "Core"

    /** Brief description of a model element ({@code Org.OData.Core.V1.Description}). */
    const val CORE_DESCRIPTION = "$CORE_VOCABULARY_ALIAS.Description"

    /** Long description of a model element ({@code Org.OData.Core.V1.LongDescription}). */
    const val CORE_LONG_DESCRIPTION = "$CORE_VOCABULARY_ALIAS.LongDescription"
}
