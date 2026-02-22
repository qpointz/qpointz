package io.qpointz.mill.data.backend.calcite;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SchemaPlusSchemaProviderTest extends BaseTest {

    @Test
    void test() {
        this.getContextRunner().run(ctx -> {
            assertNotNull(ctx.getSchemaProvider());
        });
    }

}
