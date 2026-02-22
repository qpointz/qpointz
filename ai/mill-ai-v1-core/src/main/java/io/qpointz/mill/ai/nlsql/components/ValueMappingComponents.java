package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
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

/**
 * Value Mapping Components for RAG ingestion.
 * 
 * Automatically ingests value mappings into RAG at application startup:
 * 1. Static mappings from metadata YAML (including aliases)
 * 2. Dynamic mappings from SQL sources
 */
@Component
@ConditionalOnService("ai-nl2data")
@EnableScheduling
@Slf4j
public class ValueMappingComponents {

    private final ValueRepository repository;
    private final DataOperationDispatcher dataDispatcher;
    private final MetadataProvider metadataProvider;

    public ValueMappingComponents(@Autowired(required = false) ValueRepository repository,
                                  @Autowired(required = false) DataOperationDispatcher dataOperation,
                                  @Autowired(required = false) MetadataProvider metadataProvider) {
        this.repository = repository;
        this.dataDispatcher = dataOperation;
        this.metadataProvider = metadataProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Value Mapping RAG component initialized.");
        //temp workaround
        if (repository==null || this.dataDispatcher==null || this.metadataProvider==null) {
            log.info("Value mapping not set up");
            return;
        }

        try {
            // 1. Ingest static mappings from metadata
            ingestStaticValueMappings();
            
            // 2. Ingest dynamic mappings from SQL sources
            ingestDynamicValueMappings();
        } catch (Exception ex) {
            log.error("Error during value mapping ingestion: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Ingest static value mappings from metadata YAML.
     * Includes main mappings and all expanded aliases.
     */
    private void ingestStaticValueMappings() {
        log.info("Ingesting static value mappings from metadata...");
        
        val allMappings = metadataProvider.getAllValueMappings();
        
        if (allMappings.isEmpty()) {
            log.info("No static value mappings found in metadata.");
            return;
        }
        
        log.info("Found {} static value mappings to ingest (including aliases)", allMappings.size());
        
        val docs = new ArrayList<ValueRepository.ValueDocument>();
        val seenDocIds = new java.util.HashSet<String>();
        
        for (var mappingWithContext : allMappings) {
            mappingWithContext.expand().forEach(expanded -> {
                try {
                    val targetId = List.of(
                        expanded.schemaName().toUpperCase(),
                        expanded.tableName().toUpperCase(),
                        expanded.attributeName().toUpperCase()
                    );
                    
                    val mapping = expanded.mapping();
                    
                    // Create document ID based on fully qualified name and user term
                    val docId = String.format("%s-%s-%s",
                        expanded.getFullyQualifiedName().replace(".", "-"),
                        mapping.userTerm().toLowerCase().replace(" ", "-"),
                        mapping.getLanguageCode()
                    );
                    
                    // Create document with embedding text optimized for RAG
                    val doc = new ValueRepository.ValueDocument(
                        docId,
                        targetId,
                        mapping.databaseValue(),
                        expanded.toEmbeddingText(),
                        expanded.attributeContext(),
                        expanded.similarityThreshold()
                    );

                    if (!seenDocIds.add(docId)) {
                        log.debug("Skipping duplicate static document id: {}", docId);
                        return;
                    }

                    docs.add(doc);
                    
                    log.debug("Created static document: {} -> {} ({})", 
                        mapping.userTerm(), 
                        mapping.databaseValue(),
                        expanded.getFullyQualifiedName()
                    );
                    
                } catch (Exception ex) {
                    log.error("Error creating document for mapping: {}", 
                        expanded.mapping().userTerm(), ex);
                }
            });
        }
        
        if (!docs.isEmpty()) {
            log.info("Ingesting {} static documents into RAG vector store", docs.size());
            repository.ingest(docs);
            log.info("Successfully ingested static value mappings");
        }
    }

    /**
     * Ingest dynamic value mappings from SQL sources.
     * Executes SQL queries and creates RAG documents from results.
     */
    private void ingestDynamicValueMappings() {
        log.info("Ingesting dynamic value mappings from SQL sources...");
        
        val allSources = metadataProvider.getAllValueMappingSources();
        
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

    /**
     * Execute a SQL source and ingest the results.
     * SQL must return exactly 3 columns: ID, VALUE, TEXT
     * 
     * @param source the SQL source to execute
     * @return number of documents ingested
     */
    private int ingestSqlSource(MetadataProvider.ValueMappingSourceWithContext source) {
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
                // SQL must return: ID, VALUE, TEXT
                if (recordReader.isNull(0) || recordReader.isNull(1) || recordReader.isNull(2)) {
                    log.warn("Skipping null values from source {}", source.sourceName());
                    continue;
                }
                
                val id = recordReader.getString(0);
                val value = recordReader.getString(1);
                val text = recordReader.getString(2);
                
                // Create unique document ID
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

    private String applyContextPrefix(java.util.Optional<String> context, String text) {
        if (context.isPresent() && !context.get().isBlank()) {
            return context.get() + ": " + text;
        }
        return text;
    }

}
