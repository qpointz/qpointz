package io.qpointz.mill.metadata.domain.facet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FacetPayloadJsonSchemaTest {

    @Test
    fun shouldGenerateJsonSchemaForManifestPayload() {
        val manifest = FacetTypeManifest(
            typeKey = "urn:mill/metadata/facet-type:governance",
            title = "Governance",
            description = "Governance metadata",
            category = "general",
            targetCardinality = FacetTargetCardinality.MULTIPLE,
            applicableTo = listOf("urn:mill/metadata/entity-type:table"),
            schemaVersion = "1.0",
            payload = FacetPayloadSchema(
                type = FacetSchemaType.OBJECT,
                title = "Governance payload",
                description = "Governance fields",
                fields = listOf(
                    FacetPayloadField(
                        name = "owner",
                        required = true,
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.STRING,
                            title = "Owner",
                            description = "Accountable owner",
                            format = "email"
                        ),
                        stereotype = listOf("email")
                    ),
                    FacetPayloadField(
                        name = "status",
                        required = false,
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.ENUM,
                            title = "Status",
                            description = "Review status",
                            default = "draft",
                            values = listOf(
                                FacetEnumValue("draft", "Work in progress"),
                                FacetEnumValue("approved", "Approved for use")
                            )
                        )
                    ),
                    FacetPayloadField(
                        name = "links",
                        required = false,
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.ARRAY,
                            title = "Links",
                            description = "Reference links",
                            items = FacetPayloadSchema(
                                type = FacetSchemaType.STRING,
                                title = "URL",
                                description = "One URL",
                                format = "uri"
                            )
                        ),
                        stereotype = listOf("hyperlink")
                    )
                )
            )
        )

        val schema = FacetPayloadJsonSchema.forManifest(manifest)

        assertEquals(FacetPayloadJsonSchema.DRAFT_07_SCHEMA, schema["\$schema"])
        assertEquals("urn:mill/metadata/facet-type:governance", schema["x-mill-facetTypeUrn"])
        assertEquals("MULTIPLE", schema["x-mill-targetCardinality"])
        assertEquals("object", schema["type"])
        assertEquals(listOf("owner"), schema["required"])

        @Suppress("UNCHECKED_CAST")
        val properties = schema["properties"] as Map<String, Map<String, Any?>>
        assertEquals("email", properties["owner"]?.get("format"))
        assertEquals(listOf("email"), properties["owner"]?.get("x-mill-stereotype"))
        assertEquals(listOf("draft", "approved"), properties["status"]?.get("enum"))
        assertEquals("draft", properties["status"]?.get("default"))
        assertEquals("array", properties["links"]?.get("type"))
    }

    @Test
    fun shouldOmitRequiredArrayWhenNoFieldsAreRequired() {
        val schema = FacetPayloadJsonSchema.forPayload(
            FacetPayloadSchema(
                type = FacetSchemaType.OBJECT,
                title = "Root",
                description = "Root",
                fields = listOf(
                    FacetPayloadField(
                        name = "optional",
                        required = false,
                        schema = FacetPayloadSchema(
                            type = FacetSchemaType.BOOLEAN,
                            title = "Optional",
                            description = "Optional flag"
                        )
                    )
                )
            )
        )

        assertFalse(schema.containsKey("required"))
        assertTrue(schema.containsKey("properties"))
    }
}
