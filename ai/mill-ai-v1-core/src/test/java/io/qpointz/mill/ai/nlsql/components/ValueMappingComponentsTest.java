package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
import io.qpointz.mill.metadata.impl.file.FileRepository;
import io.qpointz.mill.vectors.VectorBlockIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ValueMappingComponents.
 * Tests the complete flow of ingesting static and dynamic value mappings into RAG.
 */
class ValueMappingComponentsTest {

    private ValueRepository mockRepository;
    private DataOperationDispatcher mockDispatcher;
    private MetadataProvider mockMetadataProvider;
    private ValueMappingComponents components;

    @BeforeEach
    void setUp() {
        mockRepository = mock(ValueRepository.class);
        mockDispatcher = mock(DataOperationDispatcher.class);
        mockMetadataProvider = mock(MetadataProvider.class);
        
        components = new ValueMappingComponents(
            mockRepository,
            mockDispatcher,
            mockMetadataProvider
        );
    }

    @Test
    void testOnApplicationReady_IngestsStaticMappings() {
        // Arrange: Create test mappings
        var mapping1 = new FileRepository.ValueMapping(
            "premium",
            "PREMIUM",
            Optional.of("Premium"),
            Optional.of("High value"),
            Optional.of("en"),
            Optional.of(List.of("gold", "vip"))
        );

        var context1 = new MetadataProvider.ValueMappingWithContext(
            "TEST_SCHEMA",
            "TEST_TABLE",
            "SEGMENT",
            Optional.of("Customer segment"),
            Optional.of(0.6),
            mapping1
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of(context1));
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of());

        // Act
        components.onApplicationReady();

        // Assert: Verify ingest was called
        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor = 
            ArgumentCaptor.forClass(List.class);
        
        verify(mockRepository, atLeastOnce()).ingest(captor.capture());
        
        var ingestedDocs = captor.getValue();
        assertNotNull(ingestedDocs);
        assertFalse(ingestedDocs.isEmpty());
        
        // Should have 3 documents: premium + gold + vip
        assertEquals(3, ingestedDocs.size());
        
        // Verify document properties
        var premiumDoc = ingestedDocs.stream()
            .filter(doc -> doc.id().contains("premium"))
            .findFirst()
            .orElseThrow();
        
        assertEquals("PREMIUM", premiumDoc.value());
        assertEquals(List.of("TEST_SCHEMA", "TEST_TABLE", "SEGMENT"), premiumDoc.target());
        assertTrue(premiumDoc.text().contains("premium"));
        assertTrue(premiumDoc.text().contains("gold"));
        assertTrue(premiumDoc.text().contains("vip"));
        assertTrue(premiumDoc.text().contains("Customer segment: premium"));
        assertEquals(Optional.of("Customer segment"), premiumDoc.context());
        assertEquals(Optional.of(0.6), premiumDoc.similarityThreshold());
    }

    @Test
    void testOnApplicationReady_IngestsDynamicMappings() {
        // Arrange: Create test SQL source
        var source = new MetadataProvider.ValueMappingSourceWithContext(
            "TEST_SCHEMA",
            "TEST_TABLE",
            "COUNTRY",
            Optional.of("Country name"),
            Optional.of(0.55),
            "country_source",
            "SELECT 'US' AS ID, 'US' AS VALUE, 'United States' AS TEXT",
            "Test source",
            true,
            3600
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of());
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of(source));

        // Mock SQL execution result
        var mockResult = createMockVectorIterator(List.<String[]>of(
            new String[]{"US", "US", "United States"},
            new String[]{"CA", "CA", "Canada"},
            new String[]{"MX", "MX", "Mexico"}
        ));
        
        when(mockDispatcher.execute(any(QueryRequest.class)))
            .thenReturn(mockResult);

        // Act
        components.onApplicationReady();

        // Assert
        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor = 
            ArgumentCaptor.forClass(List.class);
        
        verify(mockRepository, atLeastOnce()).ingest(captor.capture());
        
        var ingestedDocs = captor.getValue();
        assertNotNull(ingestedDocs);
        assertEquals(3, ingestedDocs.size(), "Should ingest 3 country documents");
        
        // Verify document properties
        var usDoc = ingestedDocs.stream()
            .filter(doc -> doc.value().equals("US"))
            .findFirst()
            .orElseThrow();
        
        assertEquals("Country name: United States", usDoc.text());
        assertEquals(List.of("TEST_SCHEMA", "TEST_TABLE", "COUNTRY"), usDoc.target());
        assertEquals(Optional.of("Country name"), usDoc.context());
        assertEquals(Optional.of(0.55), usDoc.similarityThreshold());
    }

    @Test
    void testOnApplicationReady_HandlesEmptyMappings() {
        // Arrange
        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of());
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of());

        // Act
        components.onApplicationReady();

        // Assert: Should not call ingest
        verify(mockRepository, never()).ingest(anyList());
    }

    @Test
    void testOnApplicationReady_SkipsDisabledSources() {
        // Arrange
        var enabledSource = new MetadataProvider.ValueMappingSourceWithContext(
            "SCHEMA1",
            "TABLE1",
            "ATTR1",
            Optional.of("Attr1 context"),
            Optional.of(0.4),
            "enabled",
            "SELECT 1",
            "",
            true,
            3600
        );

        var disabledSource = new MetadataProvider.ValueMappingSourceWithContext(
            "SCHEMA2",
            "TABLE2",
            "ATTR2",
            Optional.of("Attr2 context"),
            Optional.of(0.4),
            "disabled",
            "SELECT 2",
            "",
            false,
            3600
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of());
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of(enabledSource, disabledSource));

        var mockResult = createMockVectorIterator(List.<String[]>of(
            new String[]{"1", "1", "One"}
        ));
        
        when(mockDispatcher.execute(any(QueryRequest.class)))
            .thenReturn(mockResult);

        // Act
        components.onApplicationReady();

        // Assert: Should only execute SQL once (for enabled source)
        verify(mockDispatcher, times(1)).execute(any(QueryRequest.class));
    }

    @Test
    void testOnApplicationReady_HandlesNullValues() {
        // Arrange
        var source = new MetadataProvider.ValueMappingSourceWithContext(
            "TEST_SCHEMA",
            "TEST_TABLE",
            "ATTR",
            Optional.of("Attr context"),
            Optional.of(0.45),
            "test_source",
            "SELECT ...",
            "",
            true,
            3600
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of());
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of(source));

        // Mock result with some null values
        var mockResult = createMockVectorIterator(List.<String[]>of(
            new String[]{"1", "VAL1", "Text1"},
            new String[]{null, "VAL2", "Text2"},  // null ID
            new String[]{"3", "VAL3", "Text3"}
        ));
        
        when(mockDispatcher.execute(any(QueryRequest.class)))
            .thenReturn(mockResult);

        // Act
        components.onApplicationReady();

        // Assert: Should skip null row
        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor = 
            ArgumentCaptor.forClass(List.class);
        
        verify(mockRepository).ingest(captor.capture());
        
        var docs = captor.getValue();
        assertEquals(2, docs.size(), "Should skip row with null value");
    }

    @Test
    void testOnApplicationReady_HandlesException() {
        // Arrange
        when(mockMetadataProvider.getAllValueMappings())
            .thenThrow(new RuntimeException("Test exception"));

        // Act - should not throw
        assertDoesNotThrow(() -> components.onApplicationReady());
    }

    @Test
    void testIngestStaticAndDynamic_Together() {
        // Arrange: Both static and dynamic mappings
        var staticMapping = new FileRepository.ValueMapping(
            "USA",
            "US",
            Optional.empty(),
            Optional.empty(),
            Optional.of("en"),
            Optional.of(List.of("United States"))
        );

        var staticContext = new MetadataProvider.ValueMappingWithContext(
            "SCHEMA",
            "TABLE",
            "COUNTRY",
            Optional.of("Country name"),
            Optional.of(0.5),
            staticMapping
        );

        var dynamicSource = new MetadataProvider.ValueMappingSourceWithContext(
            "SCHEMA",
            "TABLE",
            "COUNTRY",
            Optional.of("Country name"),
            Optional.of(0.5),
            "all_countries",
            "SELECT ...",
            "",
            true,
            3600
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of(staticContext));
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of(dynamicSource));

        var mockResult = createMockVectorIterator(List.<String[]>of(
            new String[]{"CA", "CA", "Canada"},
            new String[]{"MX", "MX", "Mexico"}
        ));
        
        when(mockDispatcher.execute(any(QueryRequest.class)))
            .thenReturn(mockResult);

        // Act
        components.onApplicationReady();

        // Assert: Should call ingest twice (once for static, once for dynamic)
        verify(mockRepository, times(2)).ingest(anyList());
    }

    @Test
    void testDocumentIdGeneration_IsUnique() {
        // Arrange
        var mapping1 = new FileRepository.ValueMapping(
            "premium",
            "PREMIUM",
            Optional.empty(),
            Optional.empty(),
            Optional.of("en"),
            Optional.empty()
        );

        var mapping2 = new FileRepository.ValueMapping(
            "premium",  // Same term
            "PREMIUM",
            Optional.empty(),
            Optional.empty(),
            Optional.of("es"),  // Different language
            Optional.empty()
        );

        var context1 = new MetadataProvider.ValueMappingWithContext(
            "SCHEMA",
            "TABLE",
            "ATTR",
            Optional.empty(),
            Optional.empty(),
            mapping1
        );

        var context2 = new MetadataProvider.ValueMappingWithContext(
            "SCHEMA",
            "TABLE",
            "ATTR",
            Optional.empty(),
            Optional.empty(),
            mapping2
        );

        when(mockMetadataProvider.getAllValueMappings())
            .thenReturn(List.of(context1, context2));
        
        when(mockMetadataProvider.getAllValueMappingSources())
            .thenReturn(List.of());

        // Act
        components.onApplicationReady();

        // Assert
        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor = 
            ArgumentCaptor.forClass(List.class);
        
        verify(mockRepository).ingest(captor.capture());
        
        var docs = captor.getValue();
        assertEquals(2, docs.size());
        
        // IDs should be different (include language code)
        assertNotEquals(docs.get(0).id(), docs.get(1).id());
        assertTrue(docs.get(0).id().contains("-en"));
        assertTrue(docs.get(1).id().contains("-es"));
    }

    /**
     * Helper to create a mock VectorBlockIterator with string data.
     */
    private VectorBlockIterator createMockVectorIterator(List<String[]> rows) {
        if (rows.isEmpty()) {
            return new VectorBlockIterator() {
                private final Iterator<VectorBlock> delegate = List.<VectorBlock>of().iterator();

                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public VectorBlock next() {
                    return delegate.next();
                }

                @Override
                public VectorBlockSchema schema() {
                    return VectorBlockSchema.getDefaultInstance();
                }
            };
        }

        int columnCount = rows.get(0).length;

        var schemaBuilder = VectorBlockSchema.newBuilder();
        for (int col = 0; col < columnCount; col++) {
            schemaBuilder.addFields(Field.newBuilder()
                .setName("COL_" + col)
                .setFieldIdx(col)
                .setType(DataType.newBuilder()
                    .setType(LogicalDataType.newBuilder()
                        .setTypeId(LogicalDataType.LogicalDataTypeId.STRING)
                        .build())
                    .setNullability(DataType.Nullability.NULL)
                    .build())
                .build());
        }

        var blockBuilder = VectorBlock.newBuilder()
            .setSchema(schemaBuilder.build())
            .setVectorSize(rows.size());

        for (int col = 0; col < columnCount; col++) {
            var nullsBuilder = Vector.NullsVector.newBuilder();
            var stringValuesBuilder = Vector.StringVector.newBuilder();

            for (var row : rows) {
                var value = row[col];
                nullsBuilder.addNulls(value == null);
                stringValuesBuilder.addValues(value == null ? "" : value);
            }

            blockBuilder.addVectors(Vector.newBuilder()
                .setFieldIdx(col)
                .setNulls(nullsBuilder.build())
                .setStringVector(stringValuesBuilder.build())
                .build());
        }

        var block = blockBuilder.build();

        return new VectorBlockIterator() {
            private final Iterator<VectorBlock> delegate = List.of(block).iterator();

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public VectorBlock next() {
                return delegate.next();
            }

            @Override
            public VectorBlockSchema schema() {
                return block.getSchema();
            }
        };
    }
}


