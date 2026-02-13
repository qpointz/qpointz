package io.qpointz.mill.ai.nlsql;

import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstraction over the vector-backed store that holds field value mappings used by the
 * NL2SQL pipeline. Implementations are responsible for ingesting RAG documents that
 * describe field-level synonyms and serving similarity lookups for individual user
 * terms.
 */
public interface ValueRepository {

    /**
     * Ingests a batch of {@link ValueDocument} records into the underlying store.
     *
     * @param document documents that should be added or updated
     */
    void ingest(List<ValueDocument> document);

    /**
     * Resolves a user-supplied term to a canonical database value.
     *
     * @param target fully qualified target identifier in the form {@code [schema, table, column]}
     * @param query  value that should be matched against the stored embeddings
     * @return optional containing the canonical value when a match is found, otherwise empty
     */
    Optional<String> lookupValue(List<String> target, String query);

    /**
     * Convenience overload that ingests a single document.
     *
     * @param document document to ingest
     */
    default void ingest(ValueDocument document) {
        ingest(List.of(document));
    }

    /**
     * Immutable description of a value mapping document stored in the vector index.
     *
     * @param id     unique identifier for the document
     * @param target target identifier in the form {@code [schema, table, column]}
     * @param value  canonical value that should be returned when matched
     * @param text   embedding text that represents the mapping and its aliases
     */
    record ValueDocument(
            String id,
            List<String> target,
            String value,
            String text,
            Optional<String> context,
            Optional<Double> similarityThreshold
    ) {

        public ValueDocument(String id, List<String> target, String value, String text) {
            this(id, target, value, text, Optional.empty(), Optional.empty());
        }

        /**
         * Translates the document into the Spring AI {@link Document} representation used by the vector store.
         *
         * @return a {@link Document} ready to be indexed
         */
        public Document toDocument() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "field-value-mapping");
            metadata.put("target", target);
            metadata.put("value", value);
            context.filter(ctx -> !ctx.isBlank()).ifPresent(ctx -> metadata.put("context", ctx));
            similarityThreshold.ifPresent(threshold -> metadata.put("similarity-threshold", threshold));

            return Document.builder()
                    .id(id)
                    .text(text)
                    .metadata(metadata)
                    .build();
        }

    }

}
