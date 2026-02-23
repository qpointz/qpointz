package io.qpointz.mill.metadata.repository.file

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileMetadataRepositoryTest {

    private lateinit var repository: FileMetadataRepository
    private lateinit var resolver: ResourceResolver

    @BeforeEach
    fun setUp() {
        resolver = ClasspathResourceResolver()
        repository = FileMetadataRepository("classpath:metadata/example.yml", resolver)
    }

    @Test fun shouldLoadEntities_fromYaml() {
        assertFalse(repository.findAll().isEmpty())
    }

    @Test fun shouldFindEntity_byId() {
        val entity = repository.findById("moneta.customers")
        assertTrue(entity.isPresent)
        assertEquals(MetadataType.TABLE, entity.get().type)
        assertEquals("customers", entity.get().tableName)
    }

    @Test fun shouldFindEntity_byLocation() {
        val entity = repository.findByLocation("moneta", "customers", null)
        assertTrue(entity.isPresent)
        assertEquals("moneta.customers", entity.get().id)
    }

    @Test fun shouldFindEntities_byType() {
        val concepts = repository.findByType(MetadataType.CONCEPT)
        assertFalse(concepts.isEmpty())
        assertEquals(MetadataType.CONCEPT, concepts[0].type)
    }

    @Test fun shouldFindEntity_byId_caseInsensitive() {
        val e1 = repository.findById("moneta.customers")
        assertTrue(e1.isPresent)
        val e2 = repository.findById("MONETA.CUSTOMERS")
        assertTrue(e2.isPresent)
        assertEquals(e1.get().id, e2.get().id)
        val e3 = repository.findById("Moneta.Customers")
        assertTrue(e3.isPresent)
        assertEquals(e1.get().id, e3.get().id)
    }

    @Test fun shouldSaveEntity_caseInsensitive() {
        val entity = MetadataEntity(id = "TEST.ENTITY", type = MetadataType.TABLE, schemaName = "test", tableName = "entity")
        repository.save(entity)
        val found = repository.findById("test.entity")
        assertTrue(found.isPresent)
        assertEquals("test.entity", found.get().id)
        val found2 = repository.findById("TEST.ENTITY")
        assertTrue(found2.isPresent)
        assertEquals("test.entity", found2.get().id)
    }

    @Test fun shouldDeleteEntity_byId_caseInsensitive() {
        val entity = MetadataEntity(id = "test.delete", type = MetadataType.TABLE, schemaName = "test", tableName = "delete")
        repository.save(entity)
        assertTrue(repository.existsById("test.delete"))
        repository.deleteById("TEST.DELETE")
        assertFalse(repository.existsById("test.delete"))
    }

    @Test fun shouldCheckExists_byId_caseInsensitive() {
        val entity = MetadataEntity(id = "test.exists", type = MetadataType.TABLE, schemaName = "test", tableName = "exists")
        repository.save(entity)
        assertTrue(repository.existsById("test.exists"))
        assertTrue(repository.existsById("TEST.EXISTS"))
        assertTrue(repository.existsById("Test.Exists"))
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test fun shouldNormalizeId_onLoad() {
        for (entity in repository.findAll()) {
            val id = entity.id
            if (id != null) assertEquals(id.lowercase(), id, "Entity ID should be normalized to lowercase: $id")
        }
    }

    @Test fun shouldLoadFromMultipleFiles() {
        val repo = FileMetadataRepository(listOf("classpath:metadata/base-test.yml", "classpath:metadata/override-test.yml"), resolver)
        val entities = repo.findAll()
        assertFalse(entities.isEmpty())
        assertEquals(2, entities.size)
    }

    @Test fun shouldReplaceEntity_whenSameIdInMultipleFiles() {
        val repo = FileMetadataRepository(listOf("classpath:metadata/base-test.yml", "classpath:metadata/override-test.yml"), resolver)
        val entity = repo.findById("test.schema.table1")
        assertTrue(entity.isPresent)
        assertEquals("test", entity.get().schemaName)
        assertEquals("table1", entity.get().tableName)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldMergeFacets_byTypeAndScope() {
        val repo = FileMetadataRepository(listOf("classpath:metadata/base-test.yml", "classpath:metadata/override-test.yml"), resolver)
        val entity = repo.findById("test.schema.table1")
        assertTrue(entity.isPresent)
        val facets = entity.get().facets
        assertNotNull(facets)
        val descriptiveScopes = facets["descriptive"]!!
        val descriptiveGlobal = descriptiveScopes["global"] as Map<String, Any?>
        assertEquals("Overridden Table", descriptiveGlobal["displayName"])
        assertNull(descriptiveGlobal["description"])
        assertTrue(descriptiveScopes.containsKey("user:alice"))
        val structuralScopes = facets["structural"]!!
        val structuralGlobal = structuralScopes["global"] as Map<String, Any?>
        assertEquals("BASE_TABLE", structuralGlobal["physicalName"])
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldInjectTypeMarker_onLoad() {
        val entity = repository.findById("moneta.customers")
        assertTrue(entity.isPresent)
        val globalData = entity.get().facets["structural"]!!["global"] as Map<String, Any?>
        assertEquals("structural", globalData["_type"])
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldInjectTypeMarker_onSave() {
        val entity = MetadataEntity(id = "test.type-inject", type = MetadataType.TABLE, schemaName = "test", tableName = "type-inject")
        entity.setFacet("descriptive", "global", mutableMapOf<String, Any?>("displayName" to "Test"))
        repository.save(entity)
        val found = repository.findById("test.type-inject")
        assertTrue(found.isPresent)
        val descriptiveGlobal = found.get().facets["descriptive"]!!["global"] as Map<String, Any?>
        assertEquals("descriptive", descriptiveGlobal["_type"])
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun shouldPreserveFacetsFromBase_whenNotInOverride() {
        val repo = FileMetadataRepository(listOf("classpath:metadata/base-test.yml", "classpath:metadata/override-test.yml"), resolver)
        val result = repo.findById("test.schema.table1")
        assertTrue(result.isPresent)
        val facets = result.get().facets
        val descriptiveGlobal = facets["descriptive"]!!["global"] as Map<String, Any?>
        assertEquals("Overridden Table", descriptiveGlobal["displayName"])
        val structuralGlobal = facets["structural"]!!["global"] as Map<String, Any?>
        assertEquals("BASE_TABLE", structuralGlobal["physicalName"])
    }
}
