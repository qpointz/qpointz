package io.qpointz.mill.data.backend.jdbc.providers;

import io.qpointz.mill.data.backend.jdbc.BaseTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JdbcSchemaProviderTest extends BaseTest {

    @Test
    void trivial() {
        this.getContextRunner().run(ctx -> {
            val schemaProvider = ctx.getSchemaProvider();
            val names = StreamSupport.stream(schemaProvider.getSchemaNames().spliterator(), false).toList();
            assertTrue(names.size() == 2);
        });
    }

}
