package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultValueRepositoryTest {

    @Test
    void lookupValueIncludesContextAndThreshold() {
        var vectorStore = mock(VectorStore.class);
        var repository = new DefaultValueRepository(vectorStore);

        var doc = new ValueRepository.ValueDocument(
                "doc-1",
                List.of("S", "T", "A"),
                "US",
                "Country name: United States",
                Optional.of("Country name"),
                Optional.of(0.7)
        );

        repository.ingest(List.of(doc));

        var storedDocument = Document.builder()
                .id("doc-1")
                .text("Country name: United States")
                .metadata(Map.of("value", "US"))
                .build();

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(storedDocument));

        var result = repository.lookupValue(List.of("S", "T", "A"), "United States");
        assertTrue(result.isPresent());
        assertEquals("US", result.get());

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(requestCaptor.capture());

        var capturedRequest = requestCaptor.getValue();
        assertEquals("Country name: United States", capturedRequest.getQuery());
        assertEquals(0.7, capturedRequest.getSimilarityThreshold());
    }
}

