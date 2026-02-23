package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.RelationCardinality;
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.RelationFacet;
import io.qpointz.mill.metadata.domain.core.StructuralFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.util.*;
import java.util.stream.Collectors;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.pebbleTemplate;

@Builder
@AllArgsConstructor
public class SchemaMessageSpec extends MessageSpec {

    @Getter
    private final MetadataService metadataService;

    @Getter
    private final MessageType messageType;

    @Getter
    private final MessageTemplate template;

    @Getter
    @Builder.Default
    private final List<ReasoningResponse.IntentTable> requiredTables = List.of();

    @Getter
    @Builder.Default
    private final boolean includeAttributes = true;

    @Getter
    @Builder.Default
    private final boolean includeRelations = true;

    @Getter
    @Builder.Default
    private final boolean includeRelationExpressions = true;

    public static SchemaMessageSpecBuilder builder(MessageType messageType, MetadataService metadataService) {
        val builder = new SchemaMessageSpecBuilder();
        return builder
                .messageType(messageType)
                .metadataService(metadataService)
                .template(createTemplate());
    }

    private static MessageTemplate createTemplate() {
        return pebbleTemplate("templates/nlsql/schema.prompt", SchemaMessageSpec.class);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of(
                "includeAttributes", this.includeAttributes,
                "includeRelations", this.includeRelations,
                "includeRelationExpressions", this.includeRelationExpressions,
                "schemas", this.getSchemas(),
                "relations", this.getRelations());
    }

    private boolean includeFullSchema() {
        return this.requiredTables == null || this.requiredTables.isEmpty();
    }

    private List<SchemaMessageModel.Schema> getSchemas() {
        final Set<String> requiredSchemaNames = this.requiredTables.stream()
                .map(z -> z.schema().toUpperCase())
                .collect(Collectors.toSet());

        List<MetadataEntity> schemaEntities = this.metadataService.findByType(MetadataType.SCHEMA);

        return schemaEntities.stream()
                .filter(s -> requiredSchemaNames.isEmpty() || requiredSchemaNames.contains(
                        s.getSchemaName() != null ? s.getSchemaName().toUpperCase() : ""))
                .map(schemaEntity -> {
                    String schemaName = schemaEntity.getSchemaName();
                    String description = schemaEntity.getFacet("descriptive", "global", DescriptiveFacet.class)
                            .map(DescriptiveFacet::getDescription)
                            .orElse(null);

                    List<MetadataEntity> tableEntities = this.metadataService.findByType(MetadataType.TABLE).stream()
                            .filter(t -> schemaName != null && schemaName.equalsIgnoreCase(t.getSchemaName()))
                            .toList();

                    List<SchemaMessageModel.Table> tables;
                    if (includeFullSchema()) {
                        tables = tableEntities.stream().map(this::toTable).toList();
                    } else {
                        val schemaTableNames = this.requiredTables.stream()
                                .filter(z -> z.schema().equalsIgnoreCase(schemaName))
                                .map(z -> z.name().toUpperCase())
                                .collect(Collectors.toSet());
                        tables = tableEntities.stream()
                                .filter(t -> schemaTableNames.contains(
                                        t.getTableName() != null ? t.getTableName().toUpperCase() : ""))
                                .map(this::toTable)
                                .toList();
                    }

                    return new SchemaMessageModel.Schema(schemaName, description, tables);
                })
                .toList();
    }

    private SchemaMessageModel.Table toTable(MetadataEntity tableEntity) {
        String description = tableEntity.getFacet("descriptive", "global", DescriptiveFacet.class)
                .map(DescriptiveFacet::getDescription)
                .orElse(null);

        List<SchemaMessageModel.Attribute> attributes = List.of();
        if (includeAttributes) {
            attributes = this.metadataService.findByType(MetadataType.ATTRIBUTE).stream()
                    .filter(a -> Objects.equals(a.getSchemaName(), tableEntity.getSchemaName())
                            && Objects.equals(a.getTableName(), tableEntity.getTableName()))
                    .map(this::toAttribute)
                    .toList();
        }

        return new SchemaMessageModel.Table(tableEntity.getSchemaName(), tableEntity.getTableName(),
                description, attributes);
    }

    private SchemaMessageModel.Attribute toAttribute(MetadataEntity attrEntity) {
        String description = attrEntity.getFacet("descriptive", "global", DescriptiveFacet.class)
                .map(DescriptiveFacet::getDescription)
                .orElse(null);
        String typeName = attrEntity.getFacet("structural", "global", StructuralFacet.class)
                .map(StructuralFacet::getPhysicalType)
                .orElse(null);
        Boolean nullable = attrEntity.getFacet("structural", "global", StructuralFacet.class)
                .map(StructuralFacet::getNullable)
                .orElse(null);

        return new SchemaMessageModel.Attribute(
                attrEntity.getSchemaName(), attrEntity.getTableName(),
                attrEntity.getAttributeName(), description, typeName, nullable);
    }

    private List<SchemaMessageModel.Relation> getRelations() {
        List<SchemaMessageModel.Relation> result = new ArrayList<>();

        for (MetadataEntity entity : this.metadataService.findAll()) {
            Optional<RelationFacet> facetOpt = entity.getFacet("relation", "global", RelationFacet.class);
            if (facetOpt.isEmpty()) continue;

            for (RelationFacet.Relation rel : facetOpt.get().getRelations()) {
                if (rel.getSourceTable() == null || rel.getTargetTable() == null) continue;

                if (!includeFullSchema()) {
                    boolean hasSrc = this.requiredTables.stream()
                            .anyMatch(z -> z.schema().equalsIgnoreCase(rel.getSourceTable().getSchema()) &&
                                    z.name().equalsIgnoreCase(rel.getSourceTable().getTable()));
                    boolean hasTrg = this.requiredTables.stream()
                            .anyMatch(z -> z.schema().equalsIgnoreCase(rel.getTargetTable().getSchema()) &&
                                    z.name().equalsIgnoreCase(rel.getTargetTable().getTable()));
                    if (!(hasSrc && hasTrg)) continue;
                }

                String srcAttr = extractAttribute(rel.getSourceAttributes());
                String trgAttr = extractAttribute(rel.getTargetAttributes());

                result.add(new SchemaMessageModel.Relation(
                        rel.getSourceTable().getSchema(), rel.getSourceTable().getTable(), srcAttr,
                        rel.getTargetTable().getSchema(), rel.getTargetTable().getTable(), trgAttr,
                        cardinalityToString(rel.getCardinality()),
                        rel.getDescription()));
            }
        }
        return result;
    }

    private String extractAttribute(List<String> attributes) {
        if (attributes == null || attributes.isEmpty()) return null;
        return attributes.get(0);
    }

    private String cardinalityToString(RelationCardinality cardinality) {
        if (cardinality == null) return null;
        return switch (cardinality) {
            case ONE_TO_ONE -> "1:1";
            case ONE_TO_MANY -> "1:N";
            case MANY_TO_ONE -> "N:1";
            case MANY_TO_MANY -> "N:N";
        };
    }
}
