package io.qpointz.mill.metadata.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MetadataEntityTest {
    
    @Test
    void shouldCreateEntity_withBasicFields() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.entity");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("moneta");
        entity.setTableName("customers");
        
        assertEquals("test.entity", entity.getId());
        assertEquals(MetadataType.TABLE, entity.getType());
    }
    
    @Test
    void shouldSetAndGetFacet_withScope() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.entity");
        
        Map<String, Object> descriptiveData = Map.of(
            "displayName", "Test Table",
            "description", "Test description"
        );
        
        entity.setFacet("descriptive", "global", descriptiveData);
        
        Optional<Map> facet = entity.getFacet("descriptive", "global", Map.class);
        assertTrue(facet.isPresent());
        assertEquals("Test Table", facet.get().get("displayName"));
    }
    
    @Test
    void shouldGetMergedFacet_withUserScope() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.entity");
        
        // Global facet
        Map<String, Object> globalFacet = Map.of(
            "description", "Global description"
        );
        entity.setFacet("descriptive", "global", globalFacet);
        
        // User-specific facet
        Map<String, Object> userFacet = Map.of(
            "description", "User-specific description"
        );
        entity.setFacet("descriptive", "user:alice@company.com", userFacet);
        
        // Get merged facet for user (should return user scope)
        Optional<Map> merged = entity.getMergedFacet(
            "descriptive",
            "alice@company.com",
            List.of(),
            List.of(),
            Map.class
        );
        
        assertTrue(merged.isPresent());
        assertEquals("User-specific description", merged.get().get("description"));
    }
    
    @Test
    void shouldGetFacetScopes() {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.entity");
        
        entity.setFacet("descriptive", "global", Map.of());
        entity.setFacet("descriptive", "user:alice@company.com", Map.of());
        entity.setFacet("descriptive", "team:engineering", Map.of());
        
        var scopes = entity.getFacetScopes("descriptive");
        assertEquals(3, scopes.size());
        assertTrue(scopes.contains("global"));
        assertTrue(scopes.contains("user:alice@company.com"));
        assertTrue(scopes.contains("team:engineering"));
    }
}

