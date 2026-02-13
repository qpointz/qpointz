package io.qpointz.mill.ai.nlsql.configuration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {ValueMappingConfigurationTest.class, ValueMappingConfiguration.class})
@ActiveProfiles("test-moneta-valuemap-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class ValueMappingConfigurationTest {

    @Test
    void trivia(@Autowired ValueMappingConfiguration documents) {
        assertNotNull(documents);
        //assertTrue(documents.getValueMapping().size()>0);
    }

}
