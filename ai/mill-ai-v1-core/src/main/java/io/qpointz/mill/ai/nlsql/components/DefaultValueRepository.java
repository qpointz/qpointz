package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import lombok.Getter;
import lombok.val;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link ValueRepository} backed by a Spring AI {@link VectorStore}. Ingested value
 * documents are converted into vector embeddings and similarity search is used to resolve
 * user-provided terms to canonical database values.
 */
public class DefaultValueRepository implements ValueRepository {

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.55;

    @Getter
    private final VectorStore store;
    private final Map<List<String>, Double> targetThresholds = new ConcurrentHashMap<>();
    private final Map<List<String>, String> targetContexts = new ConcurrentHashMap<>();

    public DefaultValueRepository(VectorStore store) {
        this.store = store;
    }

    /**
     * Persists the provided documents into the configured vector store.
     *
     * @param valueDocuments value mapping documents to index
     */
    public void ingest(List<ValueDocument> valueDocuments) {
        val docs = valueDocuments.stream()
                .map(valueDocument -> {
                    updateCaches(valueDocument);
                    return valueDocument.toDocument();
                })
                .toList();
        this.store.add(docs);
    }

    @Override
    public Optional<String> lookupValue(List<String> target, String query) {
        val normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isEmpty()) {
            return Optional.empty();
        }

        val typeFilter = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("type"),
                new Filter.Value("field-value-mapping"));
        val targetFilter = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("target"),
                new Filter.Value(target));

        val effectiveThreshold = resolveThreshold(target);
        val effectiveContext = resolveContext(target);
        val vectorQuery = applyContextToQuery(query, effectiveContext);

        val request = SearchRequest.builder()
                .query(vectorQuery)
                .filterExpression(new Filter.Expression(Filter.ExpressionType.AND, targetFilter, typeFilter))
                .similarityThreshold(effectiveThreshold)
                .topK(5)
                .build();

        val docs = this.store.similaritySearch(request);
        if (docs.isEmpty()) {
            return Optional.empty();
        }

        val bestMatch = docs.stream()
                .map(doc -> toMatchCandidate(doc, normalizedQuery))
                .flatMap(Optional::stream)
                .min(Comparator.comparingInt(MatchCandidate::distance));

        if (bestMatch.isPresent()) {
            val candidate = bestMatch.get();
            if (isAcceptableMatch(normalizedQuery, candidate.value(), candidate.distance())) {
                return Optional.of(candidate.value());
            }
        }

        // Fall back to the top vector result when typo tolerance does not find a confident match.
        val topValue = docs.get(0).getMetadata().get("value");
        return Optional.ofNullable(topValue).map(Object::toString);
    }

    private Optional<MatchCandidate> toMatchCandidate(Document document, String normalizedQuery) {
        var metadataValue = document.getMetadata().get("value");
        if (metadataValue == null) {
            return Optional.empty();
        }
        var canonicalValue = metadataValue.toString();
        var distance = levenshteinDistance(normalizedQuery, canonicalValue.toLowerCase(Locale.ROOT));
        return Optional.of(new MatchCandidate(canonicalValue, distance));
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAcceptableMatch(String query, String candidate, int distance) {
        if (candidate.isEmpty()) {
            return false;
        }
        var maxLen = Math.max(query.length(), candidate.length());
        var normalizedDistance = maxLen == 0 ? 0 : (double) distance / maxLen;
        return distance <= 2 || normalizedDistance <= 0.4;
    }

    private int levenshteinDistance(String s1, String s2) {
        if (s1.equals(s2)) {
            return 0;
        }
        var len1 = s1.length();
        var len2 = s2.length();
        if (len1 == 0) {
            return len2;
        }
        if (len2 == 0) {
            return len1;
        }
        var dp = new int[len2 + 1];
        for (int j = 0; j <= len2; j++) {
            dp[j] = j;
        }
        for (int i = 1; i <= len1; i++) {
            int previous = dp[0];
            dp[0] = i;
            for (int j = 1; j <= len2; j++) {
                int temp = dp[j];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[j] = previous;
                } else {
                    dp[j] = 1 + Math.min(Math.min(dp[j - 1], dp[j]), previous);
                }
                previous = temp;
            }
        }
        return dp[len2];
    }

    private record MatchCandidate(String value, int distance) {
    }

    private void updateCaches(ValueDocument document) {
        val key = List.copyOf(document.target());

        document.similarityThreshold().ifPresentOrElse(
                threshold -> targetThresholds.put(key, threshold),
                () -> targetThresholds.remove(key)
        );

        document.context()
                .filter(ctx -> !ctx.isBlank())
                .ifPresentOrElse(
                        ctx -> targetContexts.put(key, ctx),
                        () -> targetContexts.remove(key)
                );
    }

    private double resolveThreshold(List<String> target) {
        return targetThresholds.getOrDefault(List.copyOf(target), DEFAULT_SIMILARITY_THRESHOLD);
    }

    private Optional<String> resolveContext(List<String> target) {
        return Optional.ofNullable(targetContexts.get(List.copyOf(target)));
    }

    private String applyContextToQuery(String originalQuery, Optional<String> context) {
        return context
                .filter(ctx -> !ctx.isBlank())
                .map(ctx -> ctx + ": " + originalQuery)
                .orElse(originalQuery);
    }
}
