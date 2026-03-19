package io.qpointz.mill.ai.capabilities.valuemapping

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

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




