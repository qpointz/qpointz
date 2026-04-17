package io.qpointz.mill.ai.valuemap

/**
 * Concatenates child [ValueSource] lists in order (WI-181); dedupe is [ValueMappingService] responsibility.
 */
class CompositeValueSource(
    private val sources: List<ValueSource>,
) : ValueSource {

    override fun provideEntries(): List<AttributeValueEntry> =
        sources.flatMap { it.provideEntries() }
}
