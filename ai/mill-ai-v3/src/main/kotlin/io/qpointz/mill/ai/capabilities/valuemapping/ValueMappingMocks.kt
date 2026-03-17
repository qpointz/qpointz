package io.qpointz.mill.ai.capabilities.valuemapping

/**
 * No-op resolver for local wiring and manual testing.
 *
 * Reports no mapped attributes for any table and returns null for all value lookups.
 * Replace with a real implementation backed by ValueMappingFacet / vector store.
 */
class MockValueMappingResolver : ValueMappingResolver {
    override fun getMappedAttributes(tableId: String): List<MappedAttribute> = emptyList()

    override fun resolveValues(
        tableId: String,
        attributeName: String,
        requestedValues: List<String>,
    ): List<ValueResolution> = requestedValues.map { ValueResolution(requestedValue = it, mappedValue = null) }
}
