package io.qpointz.mill.ai.nlsql.configuration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = {Nl2SqlConfiguration.class})
@ActiveProfiles("test-moneta-valuemap-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class Nl2SqlConfigurationTestIT {

    @Test
    void trivia(@Autowired Nl2SqlConfiguration documents, @Autowired @Qualifier("LOJOKOJ") List<Map<String,Object>> sources) {
        assertNotNull(documents);
        assertFalse(sources.isEmpty());
    }



}
