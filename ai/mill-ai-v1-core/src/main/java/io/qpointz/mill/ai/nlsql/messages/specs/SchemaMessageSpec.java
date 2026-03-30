package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import io.qpointz.mill.ai.nlsql.metadata.NlsqlMetadataFacets;
import io.qpointz.mill.ai.nlsql.metadata.SchemaMessageMetadataPorts;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.data.schema.CatalogPath;
import io.qpointz.mill.data.schema.SchemaEntityKinds;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.RelationCardinality;
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.data.schema.facet.RelationFacet;
import io.qpointz.mill.data.schema.facet.StructuralFacet;
import io.qpointz.mill.metadata.repository.FacetRepository;
import io.qpointz.mill.metadata.service.MetadataEntityService;
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
    private final MetadataEntityService metadataEntityService;

    @Getter
    private final FacetRepository facetRepository;

    @Getter
    private final io.qpointz.mill.data.schema.MetadataEntityUrnCodec urnCodec;

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

    /**
     * Builds a spec with facet and URN codec wiring required for schema prompts.
     */
    public static SchemaMessageSpecBuilder forMetadata(MessageType messageType, SchemaMessageMetadataPorts ports) {
        return SchemaMessageSpec.builder()
                .messageType(messageType)
                .metadataEntityService(ports.metadataEntityService())
                .facetRepository(ports.facetRepository())
                .urnCodec(ports.urnCodec())
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

    private CatalogPath path(MetadataEntity e) {
        return urnCodec.decode(e.getId());
    }

    private String schemaName(MetadataEntity e) {
        return path(e).getSchema();
    }

    private String tableName(MetadataEntity e) {
        return path(e).getTable();
    }

    private String attributeName(MetadataEntity e) {
        return path(e).getColumn();
    }

    private List<SchemaMessageModel.Schema> getSchemas() {
        final Set<String> requiredSchemaNames = this.requiredTables.stream()
                .map(z -> z.schema().toUpperCase())
                .collect(Collectors.toSet());

        List<MetadataEntity> schemaEntities = this.metadataEntityService.findByKind(SchemaEntityKinds.SCHEMA);

        return schemaEntities.stream()
                .filter(s -> requiredSchemaNames.isEmpty() || requiredSchemaNames.contains(
                        schemaName(s) != null ? schemaName(s).toUpperCase() : ""))
                .map(schemaEntity -> {
                    String schemaNm = schemaName(schemaEntity);
                    String description = NlsqlMetadataFacets.readGlobalFacet(
                                    facetRepository, schemaEntity.getId(), "descriptive", DescriptiveFacet.class)
                            .map(DescriptiveFacet::getDescription)
                            .orElse(null);

                    List<MetadataEntity> tableEntities = this.metadataEntityService.findByKind(SchemaEntityKinds.TABLE).stream()
                            .filter(t -> schemaNm != null && schemaNm.equalsIgnoreCase(schemaName(t)))
                            .toList();

                    List<SchemaMessageModel.Table> tables;
                    if (includeFullSchema()) {
                        tables = tableEntities.stream().map(this::toTable).toList();
                    } else {
                        val schemaTableNames = this.requiredTables.stream()
                                .filter(z -> z.schema().equalsIgnoreCase(schemaNm))
                                .map(z -> z.name().toUpperCase())
                                .collect(Collectors.toSet());
                        tables = tableEntities.stream()
                                .filter(t -> schemaTableNames.contains(
                                        tableName(t) != null ? tableName(t).toUpperCase() : ""))
                                .map(this::toTable)
                                .toList();
                    }

                    return new SchemaMessageModel.Schema(schemaNm, description, tables);
                })
                .toList();
    }

    private SchemaMessageModel.Table toTable(MetadataEntity tableEntity) {
        String description = NlsqlMetadataFacets.readGlobalFacet(
                        facetRepository, tableEntity.getId(), "descriptive", DescriptiveFacet.class)
                .map(DescriptiveFacet::getDescription)
                .orElse(null);

        List<SchemaMessageModel.Attribute> attributes = List.of();
        if (includeAttributes) {
            attributes = this.metadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE).stream()
                    .filter(a -> Objects.equals(schemaName(a), schemaName(tableEntity))
                            && Objects.equals(tableName(a), tableName(tableEntity)))
                    .map(this::toAttribute)
                    .toList();
        }

        return new SchemaMessageModel.Table(schemaName(tableEntity), tableName(tableEntity),
                description, attributes);
    }

    private SchemaMessageModel.Attribute toAttribute(MetadataEntity attrEntity) {
        String description = NlsqlMetadataFacets.readGlobalFacet(
                        facetRepository, attrEntity.getId(), "descriptive", DescriptiveFacet.class)
                .map(DescriptiveFacet::getDescription)
                .orElse(null);
        String typeName = NlsqlMetadataFacets.readGlobalFacet(
                        facetRepository, attrEntity.getId(), "structural", StructuralFacet.class)
                .map(StructuralFacet::getPhysicalType)
                .orElse(null);
        Boolean nullable = NlsqlMetadataFacets.readGlobalFacet(
                        facetRepository, attrEntity.getId(), "structural", StructuralFacet.class)
                .map(StructuralFacet::getNullable)
                .orElse(null);

        return new SchemaMessageModel.Attribute(
                schemaName(attrEntity), tableName(attrEntity),
                attributeName(attrEntity), description, typeName, nullable);
    }

    private List<SchemaMessageModel.Relation> getRelations() {
        List<SchemaMessageModel.Relation> result = new ArrayList<>();

        for (MetadataEntity entity : this.metadataEntityService.findAll()) {
            Optional<RelationFacet> facetOpt = NlsqlMetadataFacets.readGlobalFacet(
                    facetRepository, entity.getId(), "relation", RelationFacet.class);
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
