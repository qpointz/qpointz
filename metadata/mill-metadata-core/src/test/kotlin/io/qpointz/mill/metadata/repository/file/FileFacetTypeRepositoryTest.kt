package io.qpointz.mill.metadata.repository.file

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FileFacetTypeRepositoryTest {

    private fun createRepository(): FileFacetTypeRepository =
        FileFacetTypeRepository(listOf("classpath:metadata/facet-types-test.yml"), ClasspathResourceResolver())

    @Test fun shouldLoadFacetTypes_fromYaml() {
        assertEquals(2, createRepository().findAll().size)
    }

    @Test fun shouldFindByTypeKey() {
        val descriptive = createRepository().findByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        assertTrue(descriptive.isPresent)
        assertEquals("Descriptive", descriptive.get().displayName)
        assertTrue(descriptive.get().mandatory)
    }

    @Test fun shouldLoadApplicableTo() {
        val targets = createRepository().findByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
            .orElseThrow().applicableTo
        assertNotNull(targets)
        assertTrue(MetadataUrns.ENTITY_TYPE_TABLE in targets!!)
        assertTrue(MetadataUrns.ENTITY_TYPE_SCHEMA in targets)
        assertTrue(MetadataUrns.ENTITY_TYPE_ATTRIBUTE in targets)
    }

    @Test fun shouldLoadContentSchema() {
        val audit = createRepository().findByTypeKey("custom-audit").orElseThrow()
        assertTrue(audit.hasContentSchema())
        assertNotNull(audit.contentSchema)
        assertEquals("object", audit.contentSchema!!["type"])
    }

    @Test fun shouldSaveNewType() {
        val repo = createRepository()
        repo.save(FacetTypeDescriptor(typeKey = "new-type", displayName = "New Type"))
        assertTrue(repo.existsByTypeKey("new-type"))
    }

    @Test fun shouldDeleteType() {
        val repo = createRepository()
        assertTrue(repo.existsByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
        repo.deleteByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        assertFalse(repo.existsByTypeKey(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
    }

    @Test fun shouldReturnEmpty_forUnknownType() {
        assertTrue(createRepository().findByTypeKey("nonexistent").isEmpty)
    }
}
