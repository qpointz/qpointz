package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.sql.RecordReaders;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@ConditionalOnService("ai-nl2data")
@EnableScheduling
@Slf4j
public class ValueMappingComponents {

    private final ValueRepository repository;
    private final DataOperationDispatcher dataDispatcher;
    private final MetadataService metadataService;

    public ValueMappingComponents(@Autowired(required = false) ValueRepository repository,
                                  @Autowired(required = false) DataOperationDispatcher dataOperation,
                                  @Autowired(required = false) MetadataService metadataService) {
        this.repository = repository;
        this.dataDispatcher = dataOperation;
        this.metadataService = metadataService;
    }

    public record ValueMappingWithContext(
        String schemaName,
        String tableName,
        String attributeName,
        Optional<String> attributeContext,
        Optional<Double> similarityThreshold,
        String userTerm,
        String databaseValue,
        Optional<String> displayValue,
        Optional<String> description,
        String languageCode,
        List<String> aliases
    ) {
        public String getFullyQualifiedName() {
            return String.format("%s.%s.%s", schemaName, tableName, attributeName);
        }

        public String toEmbeddingText() {
            var terms = new StringBuilder();
            attributeContext.filter(ctx -> !ctx.isBlank()).ifPresent(ctx ->
                terms.append(ctx).append(": ").append(userTerm).append(" "));
            terms.append(userTerm).append(" ");
            if (aliases != null) {
                aliases.forEach(alias -> terms.append(alias).append(" "));
            }
            terms.append(attributeName).append(" ");
            terms.append(tableName).append(" ");
            attributeContext.filter(ctx -> !ctx.isBlank()).ifPresent(ctx -> terms.append(ctx).append(" "));
            description.ifPresent(desc -> terms.append(desc).append(" "));
            return terms.toString().trim();
        }

        public Stream<ValueMappingWithContext> expand() {
            Stream<ValueMappingWithContext> original = Stream.of(this);
            Stream<ValueMappingWithContext> fromAliases = (aliases != null ? aliases : List.<String>of())
                .stream()
                .map(alias -> new ValueMappingWithContext(
                    schemaName, tableName, attributeName,
                    attributeContext, similarityThreshold,
                    alias, databaseValue, displayValue,
                    Optional.of("Alias of " + userTerm),
                    languageCode, List.of()));
            return Stream.concat(original, fromAliases);
        }
    }

    public record ValueMappingSourceWithContext(
        String schemaName,
        String tableName,
        String attributeName,
        Optional<String> attributeContext,
        Optional<Double> similarityThreshold,
        String sourceName,
        String sql,
        String description,
        boolean enabled,
        int cacheTtlSeconds
    ) {
        public String getFullyQualifiedName() {
            return String.format("%s.%s.%s", schemaName, tableName, attributeName);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Value Mapping RAG component initialized.");
        if (repository == null || this.dataDispatcher == null || this.metadataService == null) {
            log.info("Value mapping not set up");
            return;
        }

        try {
            ingestStaticValueMappings();
            ingestDynamicValueMappings();
        } catch (Exception ex) {
            log.error("Error during value mapping ingestion: {}", ex.getMessage(), ex);
        }
    }

    List<ValueMappingWithContext> getAllValueMappings() {
        List<ValueMappingWithContext> result = new ArrayList<>();

        List<MetadataEntity> attributes = metadataService.findByType(MetadataType.ATTRIBUTE);
        for (MetadataEntity attribute : attributes) {
            Optional<ValueMappingFacet> facetOpt = attribute.getFacet("value-mapping", "global", ValueMappingFacet.class);
            if (facetOpt.isEmpty()) continue;
            ValueMappingFacet facet = facetOpt.get();

            if (facet.getMappings() == null) continue;

            for (ValueMappingFacet.ValueMapping m : facet.getMappings()) {
                result.add(new ValueMappingWithContext(
                    attribute.getSchemaName(),
                    attribute.getTableName(),
                    attribute.getAttributeName(),
                    Optional.ofNullable(facet.getContext()),
                    Optional.ofNullable(facet.getSimilarityThreshold()),
                    m.getUserTerm(),
                    m.getDatabaseValue(),
                    Optional.ofNullable(m.getDisplayValue()),
                    Optional.ofNullable(m.getDescription()),
                    m.getLanguage() != null ? m.getLanguage() : "en",
                    m.getAliases() != null ? m.getAliases() : List.of()
                ));
            }
        }

        return result.stream()
                .flatMap(ValueMappingWithContext::expand)
                .toList();
    }

    List<ValueMappingSourceWithContext> getAllValueMappingSources() {
        List<ValueMappingSourceWithContext> result = new ArrayList<>();

        List<MetadataEntity> attributes = metadataService.findByType(MetadataType.ATTRIBUTE);
        for (MetadataEntity attribute : attributes) {
            Optional<ValueMappingFacet> facetOpt = attribute.getFacet("value-mapping", "global", ValueMappingFacet.class);
            if (facetOpt.isEmpty()) continue;
            ValueMappingFacet facet = facetOpt.get();

            if (facet.getSources() == null) continue;

            for (ValueMappingFacet.ValueMappingSource src : facet.getSources()) {
                if (!src.getEnabled()) continue;
                result.add(new ValueMappingSourceWithContext(
                    attribute.getSchemaName(),
                    attribute.getTableName(),
                    attribute.getAttributeName(),
                    Optional.ofNullable(facet.getContext()),
                    Optional.ofNullable(facet.getSimilarityThreshold()),
                    src.getName() != null ? src.getName() : "unknown",
                    src.getSql() != null ? src.getSql() : "",
                    src.getDescription() != null ? src.getDescription() : "",
                    true,
                    src.getCacheTtl()
                ));
            }
        }
        return result;
    }

    private void ingestStaticValueMappings() {
        log.info("Ingesting static value mappings from metadata...");

        val allMappings = getAllValueMappings();

        if (allMappings.isEmpty()) {
            log.info("No static value mappings found in metadata.");
            return;
        }

        log.info("Found {} static value mappings to ingest (including aliases)", allMappings.size());

        val docs = new ArrayList<ValueRepository.ValueDocument>();
        val seenDocIds = new java.util.HashSet<String>();

        for (var mapping : allMappings) {
            try {
                val targetId = List.of(
                    mapping.schemaName().toUpperCase(),
                    mapping.tableName().toUpperCase(),
                    mapping.attributeName().toUpperCase()
                );

                val docId = String.format("%s-%s-%s",
                    mapping.getFullyQualifiedName().replace(".", "-"),
                    mapping.userTerm().toLowerCase().replace(" ", "-"),
                    mapping.languageCode()
                );

                val doc = new ValueRepository.ValueDocument(
                    docId,
                    targetId,
                    mapping.databaseValue(),
                    mapping.toEmbeddingText(),
                    mapping.attributeContext(),
                    mapping.similarityThreshold()
                );

                if (!seenDocIds.add(docId)) {
                    log.debug("Skipping duplicate static document id: {}", docId);
                    continue;
                }

                docs.add(doc);

                log.debug("Created static document: {} -> {} ({})",
                    mapping.userTerm(),
                    mapping.databaseValue(),
                    mapping.getFullyQualifiedName()
                );

            } catch (Exception ex) {
                log.error("Error creating document for mapping: {}",
                    mapping.userTerm(), ex);
            }
        }

        if (!docs.isEmpty()) {
            log.info("Ingesting {} static documents into RAG vector store", docs.size());
            repository.ingest(docs);
            log.info("Successfully ingested static value mappings");
        }
    }

    private void ingestDynamicValueMappings() {
        log.info("Ingesting dynamic value mappings from SQL sources...");

        val allSources = getAllValueMappingSources();

        if (allSources.isEmpty()) {
            log.info("No SQL sources found in metadata.");
            return;
        }

        int totalDocuments = 0;
        int successfulSources = 0;

        for (var source : allSources) {
            if (!source.enabled()) {
                log.debug("Skipping disabled source: {} for {}",
                    source.sourceName(), source.getFullyQualifiedName());
                continue;
            }

            try {
                log.info("Processing SQL source: {} for {}",
                    source.sourceName(), source.getFullyQualifiedName());

                val count = ingestSqlSource(source);
                totalDocuments += count;
                successfulSources++;

                log.info("Ingested {} documents from source: {}", count, source.sourceName());

            } catch (Exception ex) {
                log.error("Error processing SQL source {}: {}",
                    source.sourceName(), ex.getMessage(), ex);
            }
        }

        log.info("Successfully ingested {} dynamic documents from {} SQL sources",
            totalDocuments, successfulSources);
    }

    private int ingestSqlSource(ValueMappingSourceWithContext source) {
        val request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder()
                .setSql(source.sql())
                .build()
            )
            .setConfig(QueryExecutionConfig.newBuilder()
                .setFetchSize(1000)
                .build())
            .build();

        val result = dataDispatcher.execute(request);
        val recordReader = RecordReaders.recordReader(result);

        val targetId = List.of(
            source.schemaName().toUpperCase(),
            source.tableName().toUpperCase(),
            source.attributeName().toUpperCase()
        );

        val docs = new ArrayList<ValueRepository.ValueDocument>();

        while (recordReader.next()) {
            try {
                if (recordReader.isNull(0) || recordReader.isNull(1) || recordReader.isNull(2)) {
                    log.warn("Skipping null values from source {}", source.sourceName());
                    continue;
                }

                val id = recordReader.getString(0);
                val value = recordReader.getString(1);
                val text = recordReader.getString(2);

                val docId = String.format("%s-%s-%s",
                    source.getFullyQualifiedName().replace(".", "-"),
                    source.sourceName().replace(" ", "-"),
                    id.toLowerCase().replace(" ", "-")
                );

                val doc = new ValueRepository.ValueDocument(
                    docId,
                    targetId,
                    value,
                    applyContextPrefix(source.attributeContext(), text),
                    source.attributeContext(),
                    source.similarityThreshold()
                );

                docs.add(doc);

            } catch (Exception ex) {
                log.error("Error reading row from source {}: {}",
                    source.sourceName(), ex.getMessage());
            }
        }

        if (!docs.isEmpty()) {
            repository.ingest(docs);
        }

        return docs.size();
    }

    private String applyContextPrefix(Optional<String> context, String text) {
        if (context.isPresent() && !context.get().isBlank()) {
            return context.get() + ": " + text;
        }
        return text;
    }
}
