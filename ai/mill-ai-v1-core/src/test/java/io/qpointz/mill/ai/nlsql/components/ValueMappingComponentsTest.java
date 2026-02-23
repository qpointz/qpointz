package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.proto.VectorBlockSchema;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.vectors.VectorBlockIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ValueMappingComponentsTest {

    private ValueRepository mockRepository;
    private DataOperationDispatcher mockDispatcher;
    private MetadataService mockMetadataService;
    private ValueMappingComponents components;

    @BeforeEach
    void setUp() {
        mockRepository = mock(ValueRepository.class);
        mockDispatcher = mock(DataOperationDispatcher.class);
        mockMetadataService = mock(MetadataService.class);

        components = new ValueMappingComponents(
            mockRepository,
            mockDispatcher,
            mockMetadataService
        );
    }

    private MetadataEntity createAttributeEntity(String schema, String table, String attribute,
                                                  ValueMappingFacet valueMappingFacet) {
        MetadataEntity entity = new MetadataEntity();
        entity.setId(schema + "." + table + "." + attribute);
        entity.setType(MetadataType.ATTRIBUTE);
        entity.setSchemaName(schema);
        entity.setTableName(table);
        entity.setAttributeName(attribute);
        entity.setFacet("value-mapping", "global", valueMappingFacet);
        return entity;
    }

    @Test
    void testOnApplicationReady_IngestsStaticMappings() {
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setContext("Customer segment");
        facet.setSimilarityThreshold(0.6);
        facet.setMappings(List.of(
            new ValueMappingFacet.ValueMapping("premium", "PREMIUM", "Premium",
                "High value", "en", List.of("gold", "vip"))
        ));

        MetadataEntity entity = createAttributeEntity("TEST_SCHEMA", "TEST_TABLE", "SEGMENT", facet);
        when(mockMetadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(entity));

        components.onApplicationReady();

        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor =
            ArgumentCaptor.forClass(List.class);

        verify(mockRepository, atLeastOnce()).ingest(captor.capture());

        var ingestedDocs = captor.getValue();
        assertNotNull(ingestedDocs);
        assertFalse(ingestedDocs.isEmpty());
        assertEquals(3, ingestedDocs.size());

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
        ValueMappingFacet facet = new ValueMappingFacet();
        facet.setContext("Country name");
        facet.setSimilarityThreshold(0.55);
        facet.setSources(List.of(
            new ValueMappingFacet.ValueMappingSource("sql", "country_source",
                "SELECT 'US' AS ID, 'US' AS VALUE, 'United States' AS TEXT",
                "Test source", true, null, null)
        ));

        MetadataEntity entity = createAttributeEntity("TEST_SCHEMA", "TEST_TABLE", "COUNTRY", facet);
        when(mockMetadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of(entity));

        var mockResult = createMockVectorIterator(List.<String[]>of(
            new String[]{"US", "US", "United States"},
            new String[]{"CA", "CA", "Canada"},
            new String[]{"MX", "MX", "Mexico"}
        ));

        when(mockDispatcher.execute(any(QueryRequest.class)))
            .thenReturn(mockResult);

        components.onApplicationReady();

        ArgumentCaptor<List<ValueRepository.ValueDocument>> captor =
            ArgumentCaptor.forClass(List.class);

        verify(mockRepository, atLeastOnce()).ingest(captor.capture());

        var ingestedDocs = captor.getValue();
        assertNotNull(ingestedDocs);
        assertEquals(3, ingestedDocs.size());

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
        when(mockMetadataService.findByType(MetadataType.ATTRIBUTE)).thenReturn(List.of());
        components.onApplicationReady();
        verify(mockRepository, never()).ingest(anyList());
    }

    @Test
    void testOnApplicationReady_HandlesException() {
        when(mockMetadataService.findByType(MetadataType.ATTRIBUTE))
            .thenThrow(new RuntimeException("Test exception"));
        assertDoesNotThrow(() -> components.onApplicationReady());
    }

    private VectorBlockIterator createMockVectorIterator(List<String[]> rows) {
        if (rows.isEmpty()) {
            return new VectorBlockIterator() {
                private final Iterator<VectorBlock> delegate = List.<VectorBlock>of().iterator();
                @Override public boolean hasNext() { return delegate.hasNext(); }
                @Override public VectorBlock next() { return delegate.next(); }
                @Override public VectorBlockSchema schema() { return VectorBlockSchema.getDefaultInstance(); }
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
            @Override public boolean hasNext() { return delegate.hasNext(); }
            @Override public VectorBlock next() { return delegate.next(); }
            @Override public VectorBlockSchema schema() { return block.getSchema(); }
        };
    }
}
