package io.qpointz.mill.ai.valuemap

/**
 * One value from a value source for sync (WI-179): canonical text plus optional string metadata.
 */
data class AttributeValueEntry(
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
)
