package io.qpointz.mill.metadata.repository.file;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataRepositoryTest {
    
    private FileMetadataRepository repository;
    private ResourceLoader resourceLoader;
    
    @BeforeEach
    void setUp() {
        resourceLoader = new DefaultResourceLoader();
        // Use test resource
        repository = new FileMetadataRepository(
            "classpath:metadata/example.yml",
            resourceLoader
        );
    }
    
    @Test
    void shouldLoadEntities_fromYaml() {
        var entities = repository.findAll();
        assertFalse(entities.isEmpty());
    }
    
    @Test
    void shouldFindEntity_byId() {
        Optional<MetadataEntity> entity = repository.findById("moneta.customers");
        assertTrue(entity.isPresent());
        assertEquals(MetadataType.TABLE, entity.get().getType());
        assertEquals("customers", entity.get().getTableName());
    }
    
    @Test
    void shouldFindEntity_byLocation() {
        Optional<MetadataEntity> entity = repository.findByLocation(
            "moneta",
            "customers",
            null
        );
        assertTrue(entity.isPresent());
        assertEquals("moneta.customers", entity.get().getId());
    }
    
    @Test
    void shouldFindEntities_byType() {
        var concepts = repository.findByType(MetadataType.CONCEPT);
        assertFalse(concepts.isEmpty());
        assertEquals(MetadataType.CONCEPT, concepts.get(0).getType());
    }
    
    @Test
    void shouldFindEntity_byId_caseInsensitive() {
        // Find with lowercase
        Optional<MetadataEntity> entity1 = repository.findById("moneta.customers");
        assertTrue(entity1.isPresent());
        
        // Find with uppercase - should find same entity
        Optional<MetadataEntity> entity2 = repository.findById("MONETA.CUSTOMERS");
        assertTrue(entity2.isPresent());
        assertEquals(entity1.get().getId(), entity2.get().getId());
        
        // Find with mixed case - should find same entity
        Optional<MetadataEntity> entity3 = repository.findById("Moneta.Customers");
        assertTrue(entity3.isPresent());
        assertEquals(entity1.get().getId(), entity3.get().getId());
    }
    
    @Test
    void shouldSaveEntity_caseInsensitive() {
        // Create entity with uppercase ID
        MetadataEntity entity = new MetadataEntity();
        entity.setId("TEST.ENTITY");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("entity");
        
        // Save it
        repository.save(entity);
        
        // Find with different case - should find the same entity (ID normalized to lowercase)
        Optional<MetadataEntity> found = repository.findById("test.entity");
        assertTrue(found.isPresent());
        assertEquals("test.entity", found.get().getId()); // ID should be normalized to lowercase
        
        // Find with original case - should also work
        Optional<MetadataEntity> found2 = repository.findById("TEST.ENTITY");
        assertTrue(found2.isPresent());
        assertEquals("test.entity", found2.get().getId());
    }
    
    @Test
    void shouldDeleteEntity_byId_caseInsensitive() {
        // First, save an entity
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.delete");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("delete");
        repository.save(entity);
        
        // Verify it exists
        assertTrue(repository.existsById("test.delete"));
        
        // Delete with different case
        repository.deleteById("TEST.DELETE");
        
        // Verify it's gone (with any case)
        assertFalse(repository.existsById("test.delete"));
        assertFalse(repository.existsById("TEST.DELETE"));
        assertFalse(repository.existsById("Test.Delete"));
    }
    
    @Test
    void shouldCheckExists_byId_caseInsensitive() {
        // First, save an entity
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.exists");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("exists");
        repository.save(entity);
        
        // Check exists with different cases
        assertTrue(repository.existsById("test.exists"));
        assertTrue(repository.existsById("TEST.EXISTS"));
        assertTrue(repository.existsById("Test.Exists"));
        assertTrue(repository.existsById("TeSt.ExIsTs"));
        
        // Non-existent entity
        assertFalse(repository.existsById("nonexistent"));
        assertFalse(repository.existsById("NONEXISTENT"));
    }
    
    @Test
    void shouldNormalizeId_onLoad() {
        // This test verifies that IDs are normalized when loading from file
        // The example.yml file should have entities with various case IDs
        var entities = repository.findAll();
        
        // All entity IDs should be lowercase (normalized)
        for (MetadataEntity entity : entities) {
            if (entity.getId() != null) {
                assertEquals(entity.getId().toLowerCase(), entity.getId(), 
                    "Entity ID should be normalized to lowercase: " + entity.getId());
            }
        }
    }
    
    @Test
    void shouldLoadFromMultipleFiles() {
        // Create repository with multiple files
        ResourceLoader loader = new DefaultResourceLoader();
        FileMetadataRepository multiFileRepo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            loader
        );
        
        var entities = multiFileRepo.findAll();
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size()); // table1 and table2
    }
    
    @Test
    void shouldReplaceEntity_whenSameIdInMultipleFiles() {
        ResourceLoader loader = new DefaultResourceLoader();
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            loader
        );
        
        // Entity from later file should have overridden properties
        Optional<MetadataEntity> entity = repo.findById("test.schema.table1");
        assertTrue(entity.isPresent());
        
        // Verify entity properties are from the later file
        assertEquals("test", entity.get().getSchemaName());
        assertEquals("table1", entity.get().getTableName());
    }
    
    @Test
    void shouldMergeFacets_byTypeAndScope() {
        ResourceLoader loader = new DefaultResourceLoader();
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            loader
        );
        
        Optional<MetadataEntity> entity = repo.findById("test.schema.table1");
        assertTrue(entity.isPresent());
        
        Map<String, Map<String, Object>> facets = entity.get().getFacets();
        assertNotNull(facets);
        
        // descriptive.global should be replaced (not merged) - description should be gone
        Map<String, Object> descriptiveScopes = facets.get("descriptive");
        assertNotNull(descriptiveScopes);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> descriptiveGlobal = (Map<String, Object>) descriptiveScopes.get("global");
        assertNotNull(descriptiveGlobal);
        assertEquals("Overridden Table", descriptiveGlobal.get("displayName"));
        assertNull(descriptiveGlobal.get("description")); // Should be replaced, not merged
        
        // descriptive.user:alice should exist
        assertTrue(descriptiveScopes.containsKey("user:alice"));
        
        // structural.global should be preserved from base file
        Map<String, Object> structuralScopes = facets.get("structural");
        assertNotNull(structuralScopes);
        assertTrue(structuralScopes.containsKey("global"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> structuralGlobal = (Map<String, Object>) structuralScopes.get("global");
        assertEquals("BASE_TABLE", structuralGlobal.get("physicalName"));
    }
    
    @Test
    void shouldHandleCommaSeparatedPaths() {
        // Test that comma-separated paths are parsed correctly
        // This is tested through the configuration, but we can verify the constructor accepts list
        ResourceLoader loader = new DefaultResourceLoader();
        List<String> paths = Arrays.asList(
            "classpath:metadata/example.yml",
            "classpath:metadata/example.yml"
        );
        
        FileMetadataRepository repo = new FileMetadataRepository(paths, loader);
        var entities = repo.findAll();
        assertFalse(entities.isEmpty());
    }
    
    @Test
    void shouldPreserveFacetsFromBase_whenNotInOverride() {
        // This test uses the actual file loading mechanism which merges facets
        // The base-test.yml has table1 with descriptive and structural facets
        // The override-test.yml has table1 with only descriptive facet (overridden)
        // Structural facet should be preserved
        
        ResourceLoader loader = new DefaultResourceLoader();
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            loader
        );
        
        Optional<MetadataEntity> result = repo.findById("test.schema.table1");
        assertTrue(result.isPresent());
        
        Map<String, Map<String, Object>> resultFacets = result.get().getFacets();
        assertNotNull(resultFacets);
        
        // descriptive.global should be replaced
        assertNotNull(resultFacets.get("descriptive"));
        @SuppressWarnings("unchecked")
        Map<String, Object> descriptiveGlobal = (Map<String, Object>) resultFacets.get("descriptive").get("global");
        assertNotNull(descriptiveGlobal);
        assertEquals("Overridden Table", descriptiveGlobal.get("displayName"));
        
        // structural.global should be preserved from base file
        assertNotNull(resultFacets.get("structural"));
        @SuppressWarnings("unchecked")
        Map<String, Object> structuralGlobal = (Map<String, Object>) resultFacets.get("structural").get("global");
        assertNotNull(structuralGlobal);
        assertEquals("BASE_TABLE", structuralGlobal.get("physicalName"));
    }
}

