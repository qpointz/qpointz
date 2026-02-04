package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {
        BackendConfiguration.class,
        JdbcCalciteConfiguration.class,
        DefaultServiceConfiguration.class
}
)
@ActiveProfiles("test-jdbc")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JdbcCalciteContextFactoryTest {

    @Autowired
    SqlProvider provider;

    @Autowired
    SubstraitDispatcher substrait;

    @Test
    @Disabled
    void validatesJdbcFunctions() {
        val sql = "SELECT * FROM `ts`.`TEST` WHERE REGEXP_LIKE(`FIRST_NAME`, '.+') = TRUE";
        val result = provider.parseSql(sql);
        if (!result.isSuccess()) {
            throw result.getException();
        }
        assertTrue(result.isSuccess(), result.getMessage());

    }

}