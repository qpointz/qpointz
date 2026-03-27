package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.facet.exceptions.FacetTypeManifestInvalidException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
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

    private fun baseManifest(payload: FacetPayloadSchema): FacetTypeManifest =
        FacetTypeManifest(
            typeKey = "descriptive",
            title = "Descriptive",
            description = "Example",
            payload = payload
        )
}

