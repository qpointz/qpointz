package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.services.jdbc.BaseTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JdbcMetadataProviderTest extends BaseTest {

    @Test
    void trivial() {
        val names = StreamSupport.stream(this.getMetadataProvider().getSchemaNames().spliterator(), false).toList();
        assertTrue(names.size()==2);
    }

}
