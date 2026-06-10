package io.qpointz.mill.ai.valuemap

/**
 * Supplies [AttributeValueEntry] rows for value-mapping indexing (WI-181).
 * Implementations do **not** dedupe; [ValueMappingService] applies last-wins on [AttributeValueEntry.content].
 */
fun interface ValueSource {

    /**
     * Returns entries to index for this source (may include duplicates across sources —
     * [CompositeValueSource] concatenates; service dedupes).
     */
    fun provideEntries(): List<AttributeValueEntry>
}
