package io.qpointz.mill.metadata.repository.file;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataRepositoryTest {
    
    private FileMetadataRepository repository;
    private ResourceResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new ClasspathResourceResolver();
        repository = new FileMetadataRepository(
            "classpath:metadata/example.yml",
            resolver
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
        Optional<MetadataEntity> entity1 = repository.findById("moneta.customers");
        assertTrue(entity1.isPresent());
        
        Optional<MetadataEntity> entity2 = repository.findById("MONETA.CUSTOMERS");
        assertTrue(entity2.isPresent());
        assertEquals(entity1.get().getId(), entity2.get().getId());
        
        Optional<MetadataEntity> entity3 = repository.findById("Moneta.Customers");
        assertTrue(entity3.isPresent());
        assertEquals(entity1.get().getId(), entity3.get().getId());
    }
    
    @Test
    void shouldSaveEntity_caseInsensitive() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("TEST.ENTITY");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("entity");
        
        repository.save(entity);
        
        Optional<MetadataEntity> found = repository.findById("test.entity");
        assertTrue(found.isPresent());
        assertEquals("test.entity", found.get().getId());
        
        Optional<MetadataEntity> found2 = repository.findById("TEST.ENTITY");
        assertTrue(found2.isPresent());
        assertEquals("test.entity", found2.get().getId());
    }
    
    @Test
    void shouldDeleteEntity_byId_caseInsensitive() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.delete");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("delete");
        repository.save(entity);
        
        assertTrue(repository.existsById("test.delete"));
        
        repository.deleteById("TEST.DELETE");
        
        assertFalse(repository.existsById("test.delete"));
        assertFalse(repository.existsById("TEST.DELETE"));
        assertFalse(repository.existsById("Test.Delete"));
    }
    
    @Test
    void shouldCheckExists_byId_caseInsensitive() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.exists");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("exists");
        repository.save(entity);
        
        assertTrue(repository.existsById("test.exists"));
        assertTrue(repository.existsById("TEST.EXISTS"));
        assertTrue(repository.existsById("Test.Exists"));
        assertTrue(repository.existsById("TeSt.ExIsTs"));
        
        assertFalse(repository.existsById("nonexistent"));
        assertFalse(repository.existsById("NONEXISTENT"));
    }
    
    @Test
    void shouldNormalizeId_onLoad() {
        var entities = repository.findAll();
        
        for (MetadataEntity entity : entities) {
            if (entity.getId() != null) {
                assertEquals(entity.getId().toLowerCase(), entity.getId(), 
                    "Entity ID should be normalized to lowercase: " + entity.getId());
            }
        }
    }
    
    @Test
    void shouldLoadFromMultipleFiles() {
        FileMetadataRepository multiFileRepo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            resolver
        );
        
        var entities = multiFileRepo.findAll();
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
    }
    
    @Test
    void shouldReplaceEntity_whenSameIdInMultipleFiles() {
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            resolver
        );
        
        Optional<MetadataEntity> entity = repo.findById("test.schema.table1");
        assertTrue(entity.isPresent());
        
        assertEquals("test", entity.get().getSchemaName());
        assertEquals("table1", entity.get().getTableName());
    }
    
    @Test
    void shouldMergeFacets_byTypeAndScope() {
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            resolver
        );
        
        Optional<MetadataEntity> entity = repo.findById("test.schema.table1");
        assertTrue(entity.isPresent());
        
        Map<String, Map<String, Object>> facets = entity.get().getFacets();
        assertNotNull(facets);
        
        Map<String, Object> descriptiveScopes = facets.get("descriptive");
        assertNotNull(descriptiveScopes);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> descriptiveGlobal = (Map<String, Object>) descriptiveScopes.get("global");
        assertNotNull(descriptiveGlobal);
        assertEquals("Overridden Table", descriptiveGlobal.get("displayName"));
        assertNull(descriptiveGlobal.get("description"));
        
        assertTrue(descriptiveScopes.containsKey("user:alice"));
        
        Map<String, Object> structuralScopes = facets.get("structural");
        assertNotNull(structuralScopes);
        assertTrue(structuralScopes.containsKey("global"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> structuralGlobal = (Map<String, Object>) structuralScopes.get("global");
        assertEquals("BASE_TABLE", structuralGlobal.get("physicalName"));
    }
    
    @Test
    void shouldHandleCommaSeparatedPaths() {
        List<String> paths = Arrays.asList(
            "classpath:metadata/example.yml",
            "classpath:metadata/example.yml"
        );
        
        FileMetadataRepository repo = new FileMetadataRepository(paths, resolver);
        var entities = repo.findAll();
        assertFalse(entities.isEmpty());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void shouldInjectTypeMarker_onLoad() {
        Optional<MetadataEntity> entity = repository.findById("moneta.customers");
        assertTrue(entity.isPresent());
        
        Map<String, Map<String, Object>> facets = entity.get().getFacets();
        Map<String, Object> structuralScopes = facets.get("structural");
        assertNotNull(structuralScopes);
        
        Map<String, Object> globalData = (Map<String, Object>) structuralScopes.get("global");
        assertNotNull(globalData);
        assertEquals("structural", globalData.get("_type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldInjectTypeMarker_onSave() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.type-inject");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("test");
        entity.setTableName("type-inject");
        entity.setFacet("descriptive", "global", new HashMap<>(Map.of("displayName", "Test")));

        repository.save(entity);

        Optional<MetadataEntity> found = repository.findById("test.type-inject");
        assertTrue(found.isPresent());

        Map<String, Object> descriptiveGlobal = (Map<String, Object>) found.get().getFacets().get("descriptive").get("global");
        assertEquals("descriptive", descriptiveGlobal.get("_type"));
    }

    @Test
    void shouldPreserveFacetsFromBase_whenNotInOverride() {
        FileMetadataRepository repo = new FileMetadataRepository(
            Arrays.asList(
                "classpath:metadata/base-test.yml",
                "classpath:metadata/override-test.yml"
            ),
            resolver
        );
        
        Optional<MetadataEntity> result = repo.findById("test.schema.table1");
        assertTrue(result.isPresent());
        
        Map<String, Map<String, Object>> resultFacets = result.get().getFacets();
        assertNotNull(resultFacets);
        
        assertNotNull(resultFacets.get("descriptive"));
        @SuppressWarnings("unchecked")
        Map<String, Object> descriptiveGlobal = (Map<String, Object>) resultFacets.get("descriptive").get("global");
        assertNotNull(descriptiveGlobal);
        assertEquals("Overridden Table", descriptiveGlobal.get("displayName"));
        
        assertNotNull(resultFacets.get("structural"));
        @SuppressWarnings("unchecked")
        Map<String, Object> structuralGlobal = (Map<String, Object>) resultFacets.get("structural").get("global");
        assertNotNull(structuralGlobal);
        assertEquals("BASE_TABLE", structuralGlobal.get("physicalName"));
    }
}
