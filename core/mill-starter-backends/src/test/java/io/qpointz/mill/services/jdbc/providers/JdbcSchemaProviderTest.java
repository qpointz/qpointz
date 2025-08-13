package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.jdbc.BaseTest;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {
        BackendConfiguration.class,
        JdbcCalciteConfiguration.class
}
)
@ActiveProfiles("test-jdbc")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JdbcSchemaProviderTest extends BaseTest {

    @Test
    void trivial() {
        val names = StreamSupport.stream(this.getSchemaProvider().getSchemaNames().spliterator(), false).toList();
        assertTrue(names.size()==2);
    }

}
