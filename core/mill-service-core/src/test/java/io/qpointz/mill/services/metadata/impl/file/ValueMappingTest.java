package io.qpointz.mill.services.metadata.impl.file;

import io.qpointz.mill.services.metadata.MetadataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValueMappingWithContext and ValueMappingSourceWithContext functionality.
 * Tests YAML parsing, context creation, and document generation.
 */
class ValueMappingTest {

    private FileAnnotationsRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        String yaml = """
            model:
              name: test-model
              description: Test model for value mappings
            
            schemas:
              - name: TEST_SCHEMA
                description: Test schema
                tables:
                  - name: TEST_TABLE
                    description: Test table
                    attributes:
                      # Attribute with static mappings and aliases
                      - name: SEGMENT
                        type: VARCHAR
                        description: Customer segment
                        value-mappings:
                          context: Customer segment
                          similarity-threshold: 0.6
                          mappings:
                            - user-term: premium
                              database-value: PREMIUM
                              display-value: Premium Customers
                              description: High-value customers
                              language: en
                              aliases: [gold, golden, vip]
                            
                            - user-term: standard
                              database-value: STANDARD
                              display-value: Standard Customers
                              language: en
                              aliases: [regular, normal]
                            
                            - user-term: básico
                              database-value: BASIC
                              language: es
                          
                          sources: []
                      
                      # Attribute with both static and dynamic
                      - name: COUNTRY
                        type: VARCHAR
                        value-mappings:
                          context: Country name
                          similarity-threshold: 0.55
                          mappings:
                            - user-term: USA
                              database-value: US
                              aliases: [United States, America]
                          
                          sources:
                            - type: sql
                              name: country_distinct
                              description: Get all countries
                              enabled: true
                              sql: |
                                SELECT DISTINCT 
                                  "COUNTRY" AS "ID",
                                  "COUNTRY" AS "VALUE",
                                  "COUNTRY" AS "TEXT"
                                FROM "TEST_SCHEMA"."TEST_TABLE"
                              cache-ttl-seconds: 3600
                      
                      # Attribute with SQL sources only
                      - name: STATUS
                        type: VARCHAR
                        value-mappings:
                          context: Account status
                          similarity-threshold: 0.5
                          mappings: []
                          sources:
                            - type: sql
                              name: status_values
                              enabled: true
                              sql: SELECT "STATUS" AS "ID", "STATUS" AS "VALUE", "STATUS" AS "TEXT" FROM "TEST_SCHEMA"."TEST_TABLE"
                            
                            - type: sql
                              name: disabled_source
                              enabled: false
                              sql: SELECT 1
                      
                      # Attribute without value mappings
                      - name: ID
                        type: INTEGER
            """;

        var inputStream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        var fileRepository = FileRepository.from(inputStream);
        repository = new FileAnnotationsRepository(fileRepository);
    }

    @Test
    void testGetAllValueMappings_ReturnsAllMappings() {
        var mappings = repository.getAllValueMappings();
        
        assertNotNull(mappings);
        assertFalse(mappings.isEmpty());
        
        // Should have: premium + 3 aliases + standard + 2 aliases + básico + USA + 2 aliases
        // = 1 + 3 + 1 + 2 + 1 + 1 + 2 = 11 mappings
        assertEquals(11, mappings.size(), "Should expand all aliases into separate mappings");
    }

    @Test
    void testGetAllValueMappings_PremiumMapping() {
        var mappings = repository.getAllValueMappings();
        
        // Find premium mapping
        var premium = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("premium"))
            .findFirst();
        
        assertTrue(premium.isPresent());
        assertEquals("TEST_SCHEMA", premium.get().schemaName());
        assertEquals("TEST_TABLE", premium.get().tableName());
        assertEquals("SEGMENT", premium.get().attributeName());
        assertEquals("PREMIUM", premium.get().mapping().databaseValue());
        assertEquals("Premium Customers", premium.get().mapping().getDisplayValueOrDefault());
        assertEquals("en", premium.get().mapping().getLanguageCode());
    }

    @Test
    void testGetAllValueMappings_AliasExpansion() {
        var mappings = repository.getAllValueMappings();
        
        // Check that "gold" alias was expanded into a mapping
        var gold = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("gold"))
            .findFirst();
        
        assertTrue(gold.isPresent(), "Alias 'gold' should be expanded into a mapping");
        assertEquals("PREMIUM", gold.get().mapping().databaseValue(), "Alias should map to same value as original");
        assertEquals("TEST_SCHEMA.TEST_TABLE.SEGMENT", gold.get().getFullyQualifiedName());
    }

    @Test
    void testGetAllValueMappings_MultipleAliases() {
        var mappings = repository.getAllValueMappings();
        
        // Check all premium aliases are present
        var premiumAliases = mappings.stream()
            .filter(m -> m.mapping().databaseValue().equals("PREMIUM"))
            .map(m -> m.mapping().userTerm())
            .toList();
        
        assertTrue(premiumAliases.contains("premium"), "Should have original term");
        assertTrue(premiumAliases.contains("gold"), "Should have 'gold' alias");
        assertTrue(premiumAliases.contains("golden"), "Should have 'golden' alias");
        assertTrue(premiumAliases.contains("vip"), "Should have 'vip' alias");
        assertEquals(4, premiumAliases.size(), "Should have 4 mappings for PREMIUM");
    }

    @Test
    void testGetAllValueMappings_MultiLanguage() {
        var mappings = repository.getAllValueMappings();
        
        // Find Spanish mapping
        var basico = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("básico"))
            .findFirst();
        
        assertTrue(basico.isPresent());
        assertEquals("es", basico.get().mapping().getLanguageCode());
        assertEquals("BASIC", basico.get().mapping().databaseValue());
    }

    @Test
    void testValueMappingWithContext_GetFullyQualifiedName() {
        var mappings = repository.getAllValueMappings();
        var first = mappings.iterator().next();
        
        assertEquals("TEST_SCHEMA.TEST_TABLE.SEGMENT", first.getFullyQualifiedName());
    }

    @Test
    void testValueMappingWithContext_ToEmbeddingText() {
        var mappings = repository.getAllValueMappings();
        
        var premium = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("premium"))
            .findFirst()
            .orElseThrow();
        
        String embeddingText = premium.toEmbeddingText();
        
        assertNotNull(embeddingText);
        assertTrue(embeddingText.contains("premium"), "Should contain user term");
        assertTrue(embeddingText.contains("gold"), "Should contain aliases");
        assertTrue(embeddingText.contains("vip"), "Should contain aliases");
        assertTrue(embeddingText.contains("SEGMENT"), "Should contain attribute name");
        assertTrue(embeddingText.contains("TEST_TABLE"), "Should contain table name");
        assertTrue(embeddingText.contains("High-value customers"), "Should contain description");
        assertTrue(embeddingText.contains("Customer segment: premium"), "Should prefix context for embeddings");
    }

    @Test
    void testValueMappingWithContext_ContextAndThresholdPresent() {
        var premium = repository.getAllValueMappings().stream()
                .filter(m -> m.mapping().userTerm().equals("premium"))
                .findFirst()
                .orElseThrow();

        assertEquals(Optional.of("Customer segment"), premium.attributeContext());
        assertEquals(Optional.of(0.6), premium.similarityThreshold());
    }

    @Test
    void testValueMappingWithContext_ToDocument() {
        var mappings = repository.getAllValueMappings();
        
        var premium = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("premium"))
            .findFirst()
            .orElseThrow();
        
        String doc = premium.toDocument();
        
        assertNotNull(doc);
        assertTrue(doc.contains("Attribute: TEST_SCHEMA.TEST_TABLE.SEGMENT"));
        assertTrue(doc.contains("User Term: premium"));
        assertTrue(doc.contains("Database Value: PREMIUM"));
        assertTrue(doc.contains("Display Value: Premium Customers"));
        assertTrue(doc.contains("Language: en"));
        assertTrue(doc.contains("Description: High-value customers"));
        assertTrue(doc.contains("Aliases: gold, golden, vip"));
    }

    @Test
    void testGetAllValueMappingSources_ReturnsAllSources() {
        var sources = repository.getAllValueMappingSources();
        
        assertNotNull(sources);
        assertFalse(sources.isEmpty());
        
        // Should have 2 enabled sources (country_distinct and status_values)
        // disabled_source should NOT be included
        assertEquals(2, sources.size());
    }

    @Test
    void testGetAllValueMappingSources_CountrySource() {
        var sources = repository.getAllValueMappingSources();
        
        var countrySource = sources.stream()
            .filter(s -> s.sourceName().equals("country_distinct"))
            .findFirst();
        
        assertTrue(countrySource.isPresent());
        assertEquals("TEST_SCHEMA", countrySource.get().schemaName());
        assertEquals("TEST_TABLE", countrySource.get().tableName());
        assertEquals("COUNTRY", countrySource.get().attributeName());
        assertEquals(Optional.of("Country name"), countrySource.get().attributeContext());
        assertEquals(Optional.of(0.55), countrySource.get().similarityThreshold());
        assertTrue(countrySource.get().enabled());
        assertEquals(3600, countrySource.get().cacheTtlSeconds());
        assertEquals("Get all countries", countrySource.get().description());
        assertTrue(countrySource.get().sql().contains("SELECT DISTINCT"));
    }

    @Test
    void testGetAllValueMappingSources_StatusSource() {
        var sources = repository.getAllValueMappingSources();
        
        var statusSource = sources.stream()
            .filter(s -> s.sourceName().equals("status_values"))
            .findFirst();
        
        assertTrue(statusSource.isPresent());
        assertEquals("STATUS", statusSource.get().attributeName());
        assertEquals(Optional.of("Account status"), statusSource.get().attributeContext());
        assertEquals(Optional.of(0.5), statusSource.get().similarityThreshold());
        assertTrue(statusSource.get().enabled());
    }

    @Test
    void testGetAllValueMappingSources_DisabledSourceExcluded() {
        var sources = repository.getAllValueMappingSources();
        
        var disabledSource = sources.stream()
            .filter(s -> s.sourceName().equals("disabled_source"))
            .findFirst();
        
        assertFalse(disabledSource.isPresent(), "Disabled sources should not be included");
    }

    @Test
    void testValueMappingSourceWithContext_GetFullyQualifiedName() {
        var sources = repository.getAllValueMappingSources();
        var first = sources.iterator().next();
        
        String fqn = first.getFullyQualifiedName();
        assertNotNull(fqn);
        assertTrue(fqn.matches("\\w+\\.\\w+\\.\\w+"), "Should match SCHEMA.TABLE.ATTRIBUTE pattern");
    }

    @Test
    void testValueMappingSourceWithContext_GetSourceId() {
        var sources = repository.getAllValueMappingSources();
        
        var countrySource = sources.stream()
            .filter(s -> s.sourceName().equals("country_distinct"))
            .findFirst()
            .orElseThrow();
        
        String sourceId = countrySource.getSourceId();
        
        assertNotNull(sourceId);
        assertEquals("TEST_SCHEMA-TEST_TABLE-COUNTRY-country_distinct", sourceId);
    }

    @Test
    void testEmptyValueMappings_ReturnsEmptyList() throws Exception {
        String yaml = """
            model:
              name: empty-model
            schemas:
              - name: EMPTY_SCHEMA
                tables:
                  - name: EMPTY_TABLE
                    attributes:
                      - name: ATTR
                        type: VARCHAR
            """;

        var inputStream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        var fileRepository = FileRepository.from(inputStream);
        var emptyRepository = new FileAnnotationsRepository(fileRepository);

        var mappings = emptyRepository.getAllValueMappings();
        var sources = emptyRepository.getAllValueMappingSources();

        assertTrue(mappings.isEmpty());
        assertTrue(sources.isEmpty());
    }

    @Test
    void testValueMappingExpand_CreatesCorrectAliases() {
        // Create a sample mapping with aliases
        var mapping = new FileRepository.ValueMapping(
            "premium",
            "PREMIUM",
            Optional.of("Premium"),
            Optional.of("High value"),
            Optional.of("en"),
            Optional.of(List.of("gold", "vip"))
        );

        var context = new MetadataProvider.ValueMappingWithContext(
            "SCHEMA",
            "TABLE",
            "ATTR",
            Optional.of("Test context"),
            Optional.of(0.7),
            mapping
        );

        var expanded = context.expand().toList();

        // Should have 3: original + 2 aliases
        assertEquals(3, expanded.size());

        // Check original is present
        var original = expanded.stream()
            .filter(m -> m.mapping().userTerm().equals("premium"))
            .findFirst();
        assertTrue(original.isPresent());

        // Check aliases are present
        var goldAlias = expanded.stream()
            .filter(m -> m.mapping().userTerm().equals("gold"))
            .findFirst();
        assertTrue(goldAlias.isPresent());
        assertEquals("PREMIUM", goldAlias.get().mapping().databaseValue());
        assertEquals(Optional.of("Test context"), goldAlias.get().attributeContext());
        assertEquals(Optional.of(0.7), goldAlias.get().similarityThreshold());

        var vipAlias = expanded.stream()
            .filter(m -> m.mapping().userTerm().equals("vip"))
            .findFirst();
        assertTrue(vipAlias.isPresent());
        assertEquals("PREMIUM", vipAlias.get().mapping().databaseValue());
        assertEquals(Optional.of("Test context"), vipAlias.get().attributeContext());
        assertEquals(Optional.of(0.7), vipAlias.get().similarityThreshold());
    }

    @Test
    void testValueMappingExpand_NoAliases() {
        var mapping = new FileRepository.ValueMapping(
            "standard",
            "STANDARD",
            Optional.empty(),
            Optional.empty(),
            Optional.of("en"),
            Optional.empty()  // No aliases
        );

        var context = new MetadataProvider.ValueMappingWithContext(
            "SCHEMA",
            "TABLE",
            "ATTR",
            Optional.empty(),
            Optional.empty(),
            mapping
        );

        var expanded = context.expand().toList();

        // Should have only 1: the original
        assertEquals(1, expanded.size());
        assertEquals("standard", expanded.get(0).mapping().userTerm());
    }

    @Test
    void testDefaultLanguage() {
        var mappings = repository.getAllValueMappings();
        
        // Find mapping without explicit language (should default to "en")
        var standard = mappings.stream()
            .filter(m -> m.mapping().userTerm().equals("standard"))
            .findFirst()
            .orElseThrow();
        
        assertEquals("en", standard.mapping().getLanguageCode(), "Should default to 'en'");
    }

    @Test
    void testMultipleAttributesWithMappings() {
        var mappings = repository.getAllValueMappings();
        
        // Check we have mappings from multiple attributes
        var attributes = mappings.stream()
            .map(MetadataProvider.ValueMappingWithContext::attributeName)
            .distinct()
            .toList();
        
        assertTrue(attributes.contains("SEGMENT"));
        assertTrue(attributes.contains("COUNTRY"));
        assertEquals(2, attributes.size(), "Should have mappings from 2 attributes");
    }

    @Test
    void testMultipleAttributesWithSources() {
        var sources = repository.getAllValueMappingSources();
        
        // Check we have sources from multiple attributes
        var attributes = sources.stream()
            .map(MetadataProvider.ValueMappingSourceWithContext::attributeName)
            .distinct()
            .toList();
        
        assertTrue(attributes.contains("COUNTRY"));
        assertTrue(attributes.contains("STATUS"));
        assertEquals(2, attributes.size(), "Should have sources from 2 attributes");
    }
}


