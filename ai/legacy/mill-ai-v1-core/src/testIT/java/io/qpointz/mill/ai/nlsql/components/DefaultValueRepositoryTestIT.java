package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.nlsql.RefineIntentTestIt;
import io.qpointz.mill.ai.nlsql.ValueRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {DefaultValueRepositoryTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class DefaultValueRepositoryTestIT extends BaseIntegrationTestIT {

    private final VectorStore vectorStore;

    public DefaultValueRepositoryTestIT(@Autowired EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore
                .builder(embeddingModel)
                .build();
    }

    @Test
    void test() {
        val rep = new DefaultValueRepository(this.vectorStore);
        val docs = List.of(
                new ValueRepository.ValueDocument("1", List.of("P","T", "A"), "US", "Country name: United States of America", Optional.of("Country name"), Optional.of(0.4)),
                new ValueRepository.ValueDocument("2", List.of("P","T", "A"), "KR", "Country name: Republic of South Korea", Optional.of("Country name"), Optional.of(0.4))
        );
        rep.ingest(docs);
        val res = rep.lookupValue(List.of("P","T","A"), "Korea");
        assertTrue(res.isPresent());
        assertEquals("KR", res.get());

        val res1 = rep.lookupValue(List.of("P","T","A"),"United states of America");
        assertTrue(res1.isPresent());
        assertEquals("US", res1.get());
    }

}