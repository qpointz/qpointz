package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.facet.exceptions.FacetTypeManifestInvalidException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FacetTypeManifestNormalizerTest {

    @Test
    fun shouldAllowKnownStringFormat() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.STRING,
                title = "Birth date",
                description = "Date-only value",
                format = "date"
            )
        )
        assertDoesNotThrow { FacetTypeManifestNormalizer.normalizeStrict(manifest) }
    }

    @Test
    fun shouldRejectUnknownStringFormat() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.STRING,
                title = "String",
                description = "String value",
                format = "uuid"
            )
        )
        assertThrows(FacetTypeManifestInvalidException::class.java) {
            FacetTypeManifestNormalizer.normalizeStrict(manifest)
        }
    }

    @Test
    fun shouldRejectFormatOnNonStringNode() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.NUMBER,
                title = "Count",
                description = "Numeric value",
                format = "date"
            )
        )
        assertThrows(FacetTypeManifestInvalidException::class.java) {
            FacetTypeManifestNormalizer.normalizeStrict(manifest)
        }
    }

    @Test
    fun shouldRejectEnumValueWithoutDescription() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.ENUM,
                title = "Status",
                description = "Status selection",
                values = listOf(
                    FacetEnumValue(value = "draft", description = "")
                )
            )
        )
        assertThrows(FacetTypeManifestInvalidException::class.java) {
            FacetTypeManifestNormalizer.normalizeStrict(manifest)
        }
    }

    @Test
    fun shouldAllowEnumValuesWithDescriptions() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.ENUM,
                title = "Status",
                description = "Status selection",
                values = listOf(
                    FacetEnumValue(value = "draft", description = "Work in progress"),
                    FacetEnumValue(value = "approved", description = "Validated and accepted")
                )
            )
        )
        assertDoesNotThrow { FacetTypeManifestNormalizer.normalizeStrict(manifest) }
    }

    @Test
    fun shouldTrimFieldStereotypeAndNormalizeBlankToNull() {
        val manifest = baseManifest(
            payload = FacetPayloadSchema(
                type = FacetSchemaType.OBJECT,
                title = "Root",
                description = "Root object",
                fields = listOf(
                    FacetPayloadField(
                        name = "ref",
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.STRING,
                            title = "Ref",
                            description = "Reference"
                        ),
                        required = false,
                        stereotype = listOf("  table  ")
                    ),
                    FacetPayloadField(
                        name = "plain",
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.STRING,
                            title = "Plain",
                            description = "Plain text"
                        ),
                        required = true,
                        stereotype = listOf("   ")
                    )
                ),
                required = emptyList()
            )
        )
        val norm = FacetTypeManifestNormalizer.normalizeStrict(manifest)
        val fields = norm.payload.fields!!
        assertEquals(listOf("table"), fields[0].stereotype)
        assertNull(fields[1].stereotype)
    }

    private fun baseManifest(payload: FacetPayloadSchema): FacetTypeManifest =
        FacetTypeManifest(
            typeKey = "descriptive",
            title = "Descriptive",
            description = "Example",
            payload = payload
        )
}

