package io.qpointz.mill.service.jdbc.providers;

import io.qpointz.mill.service.jdbc.BaseTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JdbcMetadataProviderTest extends BaseTest {

    @Test
    void trivial() {
        val names = StreamSupport.stream(this.getMetadataProvider().getSchemaNames().spliterator(), false).toList();
        assertTrue(names.size()==2);
    }

}
