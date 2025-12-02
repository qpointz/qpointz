package io.qpointz.mill.metadata.repository.file;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Optional;

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
}

