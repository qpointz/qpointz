package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class DefaultFacetCatalogTest {

    private lateinit var definitions: InMemoryFacetTypeDefinitionRepository
    private lateinit var runtime: InMemoryFacetTypeRepository
    private lateinit var catalog: DefaultFacetCatalog

    private fun now() = Instant.now()

    private fun def(
        typeKey: String,
        displayName: String = typeKey,
        mandatory: Boolean = false,
        enabled: Boolean = true,
        card: FacetTargetCardinality = FacetTargetCardinality.SINGLE
    ): FacetTypeDefinition {
        val t = now()
        return FacetTypeDefinition(
            typeKey = typeKey,
            displayName = displayName,
            description = null,
            mandatory = mandatory,
            enabled = enabled,
            targetCardinality = card,
            applicableTo = null,
            contentSchema = null,
            schemaVersion = "1.0",
            createdAt = t,
            createdBy = "test",
            lastModifiedAt = t,
            lastModifiedBy = "test"
        )
    }

    @BeforeEach
    fun setUp() {
        definitions = InMemoryFacetTypeDefinitionRepository()
        runtime = InMemoryFacetTypeRepository()
        catalog = DefaultFacetCatalog(definitions, runtime)
    }

    @Test
    fun shouldRegisterDefinition_andMirrorRuntimeRow() {
        val key = MetadataUrns.FACET_TYPE_DESCRIPTIVE
        val saved = catalog.registerDefinition(def(key, displayName = "Descriptive"))
        assertEquals(MetadataEntityUrn.canonicalize(key), saved.typeKey)
        val ft = catalog.findType(key)
        assertNotNull(ft)
        assertEquals(MetadataEntityUrn.canonicalize(key), ft!!.typeKey)
        assertEquals("Descriptive", ft.definition!!.displayName)
    }

    @Test
    fun shouldFindDefinition_byCanonicalKey() {
        catalog.registerDefinition(def("urn:mill/metadata/facet-type:custom"))
        val d = catalog.findDefinition("urn:mill/metadata/facet-type:custom")
        assertNotNull(d)
    }

    @Test
    fun shouldListDefinitions() {
        catalog.registerDefinition(def(MetadataUrns.FACET_TYPE_STRUCTURAL))
        catalog.registerDefinition(def(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
        assertEquals(2, catalog.listDefinitions().size)
    }

    @Test
    fun shouldResolveCardinality_fromDefinition() {
        catalog.registerDefinition(
            def("urn:mill/metadata/facet-type:multi", card = FacetTargetCardinality.MULTIPLE)
        )
        assertEquals(FacetTargetCardinality.MULTIPLE, catalog.resolveCardinality("urn:mill/metadata/facet-type:multi"))
    }

    @Test
    fun shouldReturnNull_whenTypeUnknown() {
        assertNull(catalog.findType("urn:mill/metadata/facet-type:absent"))
    }

    @Test
    fun shouldInspect_withoutSchema_asOk() {
        catalog.registerDefinition(def(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
        val r = catalog.inspect(MetadataUrns.FACET_TYPE_DESCRIPTIVE, mapOf("x" to 1))
        assertEquals(true, r.valid)
    }
}
