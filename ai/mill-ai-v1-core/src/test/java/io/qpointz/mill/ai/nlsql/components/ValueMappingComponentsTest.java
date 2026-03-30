package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.data.schema.SchemaEntityKinds;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataEntityUrn;
import io.qpointz.mill.metadata.domain.MetadataUrns;
import io.qpointz.mill.metadata.domain.facet.FacetInstance;
import io.qpointz.mill.metadata.domain.facet.MergeAction;
import io.qpointz.mill.metadata.repository.FacetRepository;
import io.qpointz.mill.metadata.service.MetadataEntityService;
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

import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyList;

class ValueMappingComponentsTest {

    private ValueRepository mockRepository;
    private DataOperationDispatcher mockDispatcher;
    private MetadataEntityService mockMetadataEntityService;
    private FacetRepository mockFacetRepository;
    private MetadataEntityUrnCodec urnCodec;
    private ValueMappingComponents components;

    @BeforeEach
    void setUp() {
        mockRepository = mock(ValueRepository.class);
        mockDispatcher = mock(DataOperationDispatcher.class);
        mockMetadataEntityService = mock(MetadataEntityService.class);
        mockFacetRepository = mock(FacetRepository.class);
        urnCodec = new DefaultMetadataEntityUrnCodec();

        components = new ValueMappingComponents(
            mockRepository,
            mockDispatcher,
            mockMetadataEntityService,
            mockFacetRepository,
            urnCodec
        );
    }

    private MetadataEntity attributeEntity(String schema, String table, String attribute) {
        String id = urnCodec.forAttribute(schema, table, attribute);
        Instant t = Instant.EPOCH;
        return new MetadataEntity(id, SchemaEntityKinds.ATTRIBUTE, null, t, null, t, null);
    }

    private static Map<String, Object> staticValueMappingPayload() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("user-term", "premium");
        entry.put("database-value", "PREMIUM");
        entry.put("display-value", "Premium");
        entry.put("description", "High value");
        entry.put("language", "en");
        entry.put("aliases", List.of("gold", "vip"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("context", "Customer segment");
        payload.put("similarity-threshold", 0.6);
        payload.put("mappings", List.of(entry));
        payload.put("sources", List.of());
        return payload;
    }

    private static Map<String, Object> dynamicValueMappingPayload() {
        Map<String, Object> src = new LinkedHashMap<>();
        src.put("type", "sql");
        src.put("name", "country_source");
        src.put("definition", "SELECT 'US' AS ID, 'US' AS VALUE, 'United States' AS TEXT");
        src.put("description", "Test source");
        src.put("enabled", true);
        src.put("cronExpression", null);
        src.put("cache-ttl-seconds", 3600);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("context", "Country name");
        payload.put("similarity-threshold", 0.55);
        payload.put("mappings", List.of());
        payload.put("sources", List.of(src));
        return payload;
    }

    private void stubValueMappingPayload(MetadataEntity entity, Map<String, Object> payload) {
        String eid = MetadataEntityUrn.canonicalize(entity.getId());
        String tid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath("value-mapping"));
        String global = MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL);
        FacetInstance row = new FacetInstance(
            "uid-vm-test",
            eid,
            tid,
            global,
            MergeAction.SET,
            payload,
            Instant.EPOCH,
            null,
            Instant.EPOCH,
            null
        );
        when(mockFacetRepository.findByEntityAndType(anyString(), anyString())).thenReturn(List.of(row));
    }

    @Test
    void testOnApplicationReady_IngestsStaticMappings() {
        MetadataEntity entity = attributeEntity("TEST_SCHEMA", "TEST_TABLE", "SEGMENT");
        stubValueMappingPayload(entity, staticValueMappingPayload());
        when(mockMetadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE)).thenReturn(List.of(entity));

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
        assertEquals(java.util.Optional.of("Customer segment"), premiumDoc.context());
        assertEquals(java.util.Optional.of(0.6), premiumDoc.similarityThreshold());
    }

    @Test
    void testOnApplicationReady_IngestsDynamicMappings() {
        MetadataEntity entity = attributeEntity("TEST_SCHEMA", "TEST_TABLE", "COUNTRY");
        stubValueMappingPayload(entity, dynamicValueMappingPayload());
        when(mockMetadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE)).thenReturn(List.of(entity));

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
        assertEquals(java.util.Optional.of("Country name"), usDoc.context());
        assertEquals(java.util.Optional.of(0.55), usDoc.similarityThreshold());
    }

    @Test
    void testOnApplicationReady_HandlesEmptyMappings() {
        when(mockMetadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE)).thenReturn(List.of());
        components.onApplicationReady();
        verify(mockRepository, never()).ingest(anyList());
    }

    @Test
    void testOnApplicationReady_HandlesException() {
        when(mockMetadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE))
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
