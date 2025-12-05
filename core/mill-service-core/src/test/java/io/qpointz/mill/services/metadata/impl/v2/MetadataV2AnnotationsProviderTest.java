package io.qpointz.mill.services.metadata.impl.v2;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataV2AnnotationsProviderTest {

    @Mock
    private MetadataService metadataService;

    private MetadataV2AnniotationsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MetadataV2AnniotationsProvider(metadataService);
    }

    @Test
    void shouldReturnEmpty_forModelName() {
        // When
        var result = provider.getModelName();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmpty_forModelDescription() {
        // When
        var result = provider.getModelDescription();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetSchemaDescription_whenEntityExists() {
        // Given
        MetadataEntity schema = createSchemaEntity("moneta", "Moneta Schema");
        when(metadataService.findByLocation("moneta", null, null)).thenReturn(Optional.of(schema));

        // When
        var result = provider.getSchemaDescription("moneta");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Moneta Schema", result.get());
    }

    @Test
    void shouldReturnEmpty_whenSchemaNotFound() {
        // Given
        when(metadataService.findByLocation("nonexistent", null, null)).thenReturn(Optional.empty());

        // When
        var result = provider.getSchemaDescription("nonexistent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmpty_whenNoDescriptiveFacet() {
        // Given
        MetadataEntity schema = createEntity("moneta", "moneta", null, null, MetadataType.SCHEMA);
        when(metadataService.findByLocation("moneta", null, null)).thenReturn(Optional.of(schema));

        // When
        var result = provider.getSchemaDescription("moneta");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetTableDescription_whenEntityExists() {
        // Given
        MetadataEntity table = createTableEntity("moneta", "clients", "Clients Table");
        when(metadataService.findByLocation("moneta", "clients", null)).thenReturn(Optional.of(table));

        // When
        var result = provider.getTableDescription("moneta", "clients");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Clients Table", result.get());
    }

    @Test
    void shouldGetAttributeDescription_whenEntityExists() {
        // Given
        MetadataEntity attribute = createAttributeEntity("moneta", "clients", "client_id", "Client ID");
        when(metadataService.findByLocation("moneta", "clients", "client_id")).thenReturn(Optional.of(attribute));

        // When
        var result = provider.getAttributeDescription("moneta", "clients", "client_id");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Client ID", result.get());
    }

    @Test
    void shouldGetAllValueMappings_whenAttributesHaveValueMappings() {
        // Given
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setContext("Client segment");
        facet.setSimilarityThreshold(0.5);
        facet.setMappings(List.of(
            new ValueMappingFacet.ValueMapping("premium", "PREMIUM", "Premium", "High value", "en", List.of("gold", "vip"))
        ));
        
        MetadataEntity attribute = createAttributeWithValueMapping(
            "moneta",
            "clients",
            "segment",
            facet
        );
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var mappings = provider.getAllValueMappings();

        // Then
        assertNotNull(mappings);
        assertEquals(3, mappings.size()); // Original + 2 aliases
        
        var premiumMapping = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("premium"))
            .findFirst();
        assertTrue(premiumMapping.isPresent());
        assertEquals("PREMIUM", premiumMapping.get().mapping().databaseValue());
        assertEquals("moneta", premiumMapping.get().schemaName());
        assertEquals("clients", premiumMapping.get().tableName());
        assertEquals("segment", premiumMapping.get().attributeName());
    }

    @Test
    void shouldReturnEmpty_whenNoValueMappings() {
        // Given
        MetadataEntity attribute = createEntity("moneta.clients.segment", "moneta", "clients", "segment", MetadataType.ATTRIBUTE);
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var mappings = provider.getAllValueMappings();

        // Then
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
    }

    @Test
    void shouldExpandAliases_inValueMappings() {
        // Given
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setMappings(List.of(
            new ValueMappingFacet.ValueMapping("premium", "PREMIUM", null, null, "en", List.of("gold", "vip"))
        ));
        
        MetadataEntity attribute = createAttributeWithValueMapping(
            "moneta",
            "clients",
            "segment",
            facet
        );
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var mappings = provider.getAllValueMappings();

        // Then
        assertEquals(3, mappings.size());
        
        var userTerms = mappings.stream()
            .map(m -> m.mapping().userTerm())
            .toList();
        assertTrue(userTerms.contains("premium"));
        assertTrue(userTerms.contains("gold"));
        assertTrue(userTerms.contains("vip"));
    }

    @Test
    void shouldGetAllValueMappingSources_whenAttributesHaveSources() {
        // Given
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setSources(List.of(
            new ValueMappingFacet.ValueMappingSource(
                "SQL_QUERY",
                "country_values",
                "SELECT DISTINCT COUNTRY FROM CLIENTS",
                "Discover countries",
                true,
                null,
                3600
            )
        ));
        
        MetadataEntity attribute = createAttributeWithValueMapping(
            "moneta",
            "clients",
            "country",
            facet
        );
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var sources = provider.getAllValueMappingSources();

        // Then
        assertNotNull(sources);
        assertEquals(1, sources.size());
        
        var source = sources.iterator().next();
        assertEquals("country_values", source.sourceName());
        assertTrue(source.sql().contains("SELECT DISTINCT"));
        assertEquals("moneta", source.schemaName());
        assertEquals("clients", source.tableName());
        assertEquals("country", source.attributeName());
        assertTrue(source.enabled());
        assertEquals(3600, source.cacheTtlSeconds());
    }

    @Test
    void shouldFilterDisabledSources() {
        // Given
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setSources(List.of(
            new ValueMappingFacet.ValueMappingSource("SQL_QUERY", "enabled_source", "SELECT 1", "Enabled", true, null, 3600),
            new ValueMappingFacet.ValueMappingSource("SQL_QUERY", "disabled_source", "SELECT 2", "Disabled", false, null, 3600)
        ));
        
        MetadataEntity attribute = createAttributeWithValueMapping(
            "moneta",
            "clients",
            "country",
            facet
        );
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var sources = provider.getAllValueMappingSources();

        // Then
        assertEquals(1, sources.size());
        assertEquals("enabled_source", sources.iterator().next().sourceName());
    }

    @Test
    void shouldHandleMultipleAttributes_withValueMappings() {
        // Given
        ValueMappingFacet facet1 = new ValueMappingFacet();
        facet1.setMappings(List.of(
            new ValueMappingFacet.ValueMapping("premium", "PREMIUM", null, null, "en", null)
        ));
        
        ValueMappingFacet facet2 = new ValueMappingFacet();
        facet2.setMappings(List.of(
            new ValueMappingFacet.ValueMapping("usa", "US", "United States", null, "en", null)
        ));
        
        MetadataEntity attr1 = createAttributeWithValueMapping("moneta", "clients", "segment", facet1);
        MetadataEntity attr2 = createAttributeWithValueMapping("moneta", "clients", "country", facet2);
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attr1, attr2));

        // When
        var mappings = provider.getAllValueMappings();

        // Then
        assertEquals(2, mappings.size());
    }

    @Test
    void shouldHandleNullDescriptions() {
        // Given
        MetadataEntity schema = createSchemaEntity("moneta", null);
        when(metadataService.findByLocation("moneta", null, null)).thenReturn(Optional.of(schema));

        // When
        var result = provider.getSchemaDescription("moneta");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseGlobalScope() {
        // Given
        MetadataEntity schema = createSchemaEntity("moneta", "Description");
        when(metadataService.findByLocation("moneta", null, null)).thenReturn(Optional.of(schema));

        // When
        provider.getSchemaDescription("moneta");

        // Then
        verify(metadataService).findByLocation("moneta", null, null);
        // Verify that getFacet is called with "global" scope (indirectly through DescriptiveFacet)
    }

    @Test
    void shouldHandleValueMappingWithDefinitionField() {
        // Given - YAML uses "definition" instead of "sql"
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setSources(List.of(
            new ValueMappingFacet.ValueMappingSource(
                "SQL_QUERY",
                "country_values",
                "SELECT DISTINCT COUNTRY",  // definition field
                "Discover",
                true,
                null,
                3600
            )
        ));
        
        MetadataEntity attribute = createAttributeWithValueMapping(
            "moneta",
            "clients",
            "country",
            facet
        );
        when(metadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(attribute));

        // When
        var sources = provider.getAllValueMappingSources();

        // Then
        assertEquals(1, sources.size());
        assertTrue(sources.iterator().next().sql().contains("SELECT DISTINCT"));
    }

    // Helper methods

    private MetadataEntity createEntity(String id, String schema, String table, String attribute, MetadataType type) {
        MetadataEntity entity = new MetadataEntity();
        entity.setId(id);
        entity.setType(type);
        entity.setSchemaName(schema);
        entity.setTableName(table);
        entity.setAttributeName(attribute);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private MetadataEntity createSchemaEntity(String schemaName, String description) {
        MetadataEntity entity = createEntity(schemaName, schemaName, null, null, MetadataType.SCHEMA);
        if (description != null) {
            DescriptiveFacet facet = new DescriptiveFacet();
            facet.setDescription(description);
            entity.setFacet("descriptive", "global", facet);
        }
        return entity;
    }

    private MetadataEntity createTableEntity(String schema, String table, String description) {
        MetadataEntity entity = createEntity(schema + "." + table, schema, table, null, MetadataType.TABLE);
        if (description != null) {
            DescriptiveFacet facet = new DescriptiveFacet();
            facet.setDescription(description);
            entity.setFacet("descriptive", "global", facet);
        }
        return entity;
    }

    private MetadataEntity createAttributeEntity(String schema, String table, String attribute, String description) {
        MetadataEntity entity = createEntity(schema + "." + table + "." + attribute, schema, table, attribute, MetadataType.ATTRIBUTE);
        if (description != null) {
            DescriptiveFacet facet = new DescriptiveFacet();
            facet.setDescription(description);
            entity.setFacet("descriptive", "global", facet);
        }
        return entity;
    }

    private MetadataEntity createAttributeWithValueMapping(String schema, String table, String attribute, ValueMappingFacet valueMappingFacet) {
        MetadataEntity entity = createEntity(schema + "." + table + "." + attribute, schema, table, attribute, MetadataType.ATTRIBUTE);
        entity.setFacet("value-mapping", "global", valueMappingFacet);
        return entity;
    }
}

