package io.qpointz.mill.metadata.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MetadataYamlSerializerLegacyEnvelopeTest {

    @Test
    fun shouldFail_whenLegacyEnvelopeProvided() {
        val yaml = """
            entities:
              - id: moneta
                type: SCHEMA
                schemaName: moneta
                facets: {}
        """.trimIndent()
        assertThrows(IllegalStateException::class.java) {
            MetadataYamlSerializer.deserialize(yaml)
        }
    }

    @Test
    fun shouldDeserializeMultidoc_whenLeadingCommentPreambleBeforeFirstSeparator() {
        val yaml = """
            # Generated fixture — preamble only (no document body)
            ---
            kind: MetadataScope
            scopeUrn: urn:mill/metadata/scope:global
            scopeType: GLOBAL
        """.trimIndent()
        val doc = MetadataYamlSerializer.deserialize(yaml)
        assertEquals(1, doc.scopes.size)
        assertEquals("urn:mill/metadata/scope:global", doc.scopes.first().res)
    }
}
