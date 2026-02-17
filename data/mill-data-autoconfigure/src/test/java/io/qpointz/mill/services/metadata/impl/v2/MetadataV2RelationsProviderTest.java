package io.qpointz.mill.services.metadata.impl.v2;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.RelationCardinality;
import io.qpointz.mill.metadata.domain.core.EntityReference;
import io.qpointz.mill.metadata.domain.core.RelationFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.services.metadata.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataV2RelationsProviderTest {

    @Mock
    private MetadataService metadataService;

    private MetadataV2RelationsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MetadataV2RelationsProvider(metadataService);
    }

    @Test
    void shouldGetRelations_whenEntityHasRelationFacet() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    "Description 1",
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("foreign_id"),
                    RelationCardinality.ONE_TO_MANY,
                    "Test description"
                )
            )
        );

        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertNotNull(relations);
        assertEquals(1, relations.size());
        
        Relation relation = relations.iterator().next();
        assertEquals("schema1", relation.source().schema());
        assertEquals("table1", relation.source().name());
        assertEquals("schema2", relation.target().schema());
        assertEquals("table2", relation.target().name());
        assertEquals("id", relation.attributeRelation().source().name());
        assertEquals("foreign_id", relation.attributeRelation().target().name());
        assertEquals(Relation.Cardinality.ONE_TO_MANY, relation.cardinality());
        assertTrue(relation.description().isPresent());
        assertEquals("Test description", relation.description().get());
    }

    @Test
    void shouldReturnEmpty_whenNoEntities() {
        // Given
        when(metadataService.findAll()).thenReturn(List.of());

        // When
        var relations = provider.getRelations();

        // Then
        assertNotNull(relations);
        assertTrue(relations.isEmpty());
    }

    @Test
    void shouldReturnEmpty_whenEntityHasNoRelationFacet() {
        // Given
        MetadataEntity entity = createEntity("schema1.table1", "schema1", "table1");
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertNotNull(relations);
        assertTrue(relations.isEmpty());
    }

    @Test
    void shouldMapCardinality_ONE_TO_ONE() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        assertEquals(Relation.Cardinality.ONE_TO_ONE, relations.iterator().next().cardinality());
    }

    @Test
    void shouldMapCardinality_MANY_TO_MANY() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id"),
                    RelationCardinality.MANY_TO_MANY,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        assertEquals(Relation.Cardinality.MANY_TO_MANY, relations.iterator().next().cardinality());
    }

    @Test
    void shouldMapCardinality_MANY_TO_ONE_to_ONE_TO_MANY() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id"),
                    RelationCardinality.MANY_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        assertEquals(Relation.Cardinality.ONE_TO_MANY, relations.iterator().next().cardinality());
    }

    @Test
    void shouldMapCardinality_UNSPECIFIED_whenNull() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id"),
                    null,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        assertEquals(Relation.Cardinality.UNSPECIFIED, relations.iterator().next().cardinality());
    }

    @Test
    void shouldUseAttributeFromList_whenSingleAttributeProvided() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", "fallback_attr"),
                    List.of("actual_attr"),
                    new EntityReference("schema2", "table2", null),
                    List.of("target_attr"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertEquals("actual_attr", relation.attributeRelation().source().name());
        assertEquals("target_attr", relation.attributeRelation().target().name());
    }

    @Test
    void shouldUseAttributeFromEntityReference_whenAttributeListEmpty() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", "ref_attr"),
                    List.of(),
                    new EntityReference("schema2", "table2", "target_ref_attr"),
                    List.of(),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertEquals("ref_attr", relation.attributeRelation().source().name());
        assertEquals("target_ref_attr", relation.attributeRelation().target().name());
    }

    @Test
    void shouldHandleNullAttribute_whenNoAttributeProvided() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of(),
                    new EntityReference("schema2", "table2", null),
                    List.of(),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        Relation relation = relations.iterator().next();
        assertNull(relation.attributeRelation().source().name());
        assertNull(relation.attributeRelation().target().name());
    }

    @Test
    void shouldSkipRelation_whenMultipleSourceAttributes() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("attr1", "attr2"), // Multiple attributes
                    new EntityReference("schema2", "table2", null),
                    List.of("target_attr"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertTrue(relations.isEmpty(), "Should skip relation with multiple source attributes");
    }

    @Test
    void shouldSkipRelation_whenMultipleTargetAttributes() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("source_attr"),
                    new EntityReference("schema2", "table2", null),
                    List.of("attr1", "attr2"), // Multiple attributes
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertTrue(relations.isEmpty(), "Should skip relation with multiple target attributes");
    }

    @Test
    void shouldHandleMultipleRelationsInFacet() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id1"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id2"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                ),
                createRelation(
                    "rel2",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id3"),
                    new EntityReference("schema3", "table3", null),
                    List.of("id4"),
                    RelationCardinality.ONE_TO_MANY,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(2, relations.size());
    }

    @Test
    void shouldHandleMultipleEntities() {
        // Given
        MetadataEntity entity1 = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id1"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id2"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        
        MetadataEntity entity2 = createEntityWithRelationFacet(
            "schema3.table3",
            "schema3",
            "table3",
            createRelationFacet(
                createRelation(
                    "rel2",
                    null,
                    new EntityReference("schema3", "table3", null),
                    List.of("id3"),
                    new EntityReference("schema4", "table4", null),
                    List.of("id4"),
                    RelationCardinality.MANY_TO_MANY,
                    null
                )
            )
        );
        
        when(metadataService.findAll()).thenReturn(List.of(entity1, entity2));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(2, relations.size());
    }

    @Test
    void shouldHandleNullDescription() {
        // Given
        MetadataEntity entity = createEntityWithRelationFacet(
            "schema1.table1",
            "schema1",
            "table1",
            createRelationFacet(
                createRelation(
                    "rel1",
                    null,
                    new EntityReference("schema1", "table1", null),
                    List.of("id"),
                    new EntityReference("schema2", "table2", null),
                    List.of("id"),
                    RelationCardinality.ONE_TO_ONE,
                    null
                )
            )
        );
        when(metadataService.findAll()).thenReturn(List.of(entity));

        // When
        var relations = provider.getRelations();

        // Then
        assertEquals(1, relations.size());
        assertTrue(relations.iterator().next().description().isEmpty());
    }

    // Helper methods

    private MetadataEntity createEntity(String id, String schema, String table) {
        MetadataEntity entity = new MetadataEntity();
        entity.setId(id);
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName(schema);
        entity.setTableName(table);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private MetadataEntity createEntityWithRelationFacet(
            String id,
            String schema,
            String table,
            RelationFacet relationFacet) {
        MetadataEntity entity = createEntity(id, schema, table);
        entity.setFacet("relation", "global", relationFacet);
        return entity;
    }

    private RelationFacet createRelationFacet(RelationFacet.Relation... relations) {
        RelationFacet facet = new RelationFacet();
        for (RelationFacet.Relation rel : relations) {
            facet.getRelations().add(rel);
        }
        return facet;
    }

    private RelationFacet.Relation createRelation(
            String name,
            String description,
            EntityReference sourceTable,
            List<String> sourceAttributes,
            EntityReference targetTable,
            List<String> targetAttributes,
            RelationCardinality cardinality,
            String relationDescription) {
        return new RelationFacet.Relation(
                name,
                relationDescription != null ? relationDescription : description,
                sourceTable,
                sourceAttributes != null ? sourceAttributes : new ArrayList<>(),
                targetTable,
                targetAttributes != null ? targetAttributes : new ArrayList<>(),
                cardinality,
                null, // type
                null, // joinSql
                null  // businessMeaning
        );
    }
}

