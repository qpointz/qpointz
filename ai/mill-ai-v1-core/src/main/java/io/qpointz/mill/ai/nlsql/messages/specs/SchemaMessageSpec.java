package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.services.metadata.model.Attribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qpointz.mill.ai.chat.messages.MessageTemplates.pebbleTemplate;

@Builder
@AllArgsConstructor
public class SchemaMessageSpec extends MessageSpec  {

    @Getter
    private final MetadataProvider metadataProvider;

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

    public static SchemaMessageSpecBuilder builder(MessageType messageType, MetadataProvider metadataProvider) {
        val builder = new SchemaMessageSpecBuilder();
        return builder
                .messageType(messageType)
                .metadataProvider(metadataProvider)
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
        final Set<String> requieredSchemas = this.requiredTables.stream()
                .map(z-> z.schema().toUpperCase())
                .collect(Collectors.toSet());

        return this.metadataProvider.getSchemas().stream()
                .filter(k-> requieredSchemas.isEmpty() || requieredSchemas.contains(k.name().toUpperCase()))
                .map(k -> {
                    val tables = this.metadataProvider.getTables(k.name()).stream();
                    if (requiredTables==null || requiredTables.isEmpty()) {
                        return SchemaMessageModel.Schema.of(k, tables.toList());
                    }

                    val schemaTables = this.requiredTables.stream()
                            .filter(z-> z.schema().compareToIgnoreCase(k.name())==0)
                            .map(z-> z.name().toUpperCase())
                            .collect(Collectors.toSet());

                    return SchemaMessageModel.Schema.of(k, tables.filter(t -> schemaTables.contains(t.name().toUpperCase()))
                            .toList());
                })
                .toList();
    }

    private List<SchemaMessageModel.Relation> getRelations() {
        return this.metadataProvider.getRelations().stream().map(k-> {
                            val mayBeSrc = this.metadataProvider.getTable(k.source().schema(), k.source().name());
                            val mayBeTarget = this.metadataProvider.getTable(k.target().schema(), k.target().name());

                            if (mayBeSrc.isEmpty() || mayBeTarget.isEmpty()) {
                                return Optional.<SchemaMessageModel.Relation>empty();
                            }

                            val src = mayBeSrc.get();
                            val trg = mayBeTarget.get();

                            if (!this.includeFullSchema()) {
                                val hasSrc = this.requiredTables.stream()
                                        .anyMatch(z-> z.schema().compareToIgnoreCase(src.schema())==0 &&
                                                z.name().compareToIgnoreCase(src.name())==0);
                                val hasTrg = this.requiredTables.stream()
                                        .anyMatch(z-> z.schema().compareToIgnoreCase(trg.schema())==0 &&
                                                z.name().compareToIgnoreCase(trg.name())==0);
                                if (!(hasSrc && hasTrg)) {
                                    return Optional.<SchemaMessageModel.Relation>empty();
                                }
                            }

                            val srcAttribute = src.attributes().stream()
                                    .map(Attribute::name)
                                    .filter(z-> z.compareToIgnoreCase(k.attributeRelation().source().name())==0)
                                    .findFirst()
                                    .orElse(k.attributeRelation().source().name());

                            val targetAttribute = trg.attributes().stream()
                                    .map(Attribute::name)
                                    .filter(z-> z.compareToIgnoreCase(k.attributeRelation().source().name())==0)
                                    .findFirst()
                                    .orElse(k.attributeRelation().source().name());

                            return Optional.of(new SchemaMessageModel.Relation(src.schema(), src.name(), srcAttribute,
                                    trg.schema(), trg.name(), targetAttribute,
                                    cardinlatyToString(k.cardinality()), k.description().orElse(null)));
                        }
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private String cardinlatyToString(io.qpointz.mill.services.metadata.model.Relation.Cardinality cardinality) {
        return switch (cardinality) {
            case ONE_TO_ONE -> "1:1";
            case ONE_TO_MANY -> "1:N";
            case MANY_TO_MANY -> "N:N";
            default -> null;
        };
    }
}
