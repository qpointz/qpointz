package io.qpointz.mill.metadata.database;

import io.qpointz.mill.InProcessTest;
import io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration;
import io.qpointz.mill.data.backend.MillGrpcService;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import lombok.val;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("in-proc-test")
@ContextConfiguration(classes = {
        CalciteBackendAutoConfiguration.class,
        MillGrpcService.class,
        GrpcAdviceAutoConfiguration.class,
        DefaultServiceConfiguration.class
}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ColumnsMetadataTest extends InProcessTest {

    private Stream<ColumnsMetadata.ColumnRecord> collect(String catalogPattern, String schemaPattern, String tableNamePattern, String columnNamePattern) {
        val con = this.createConnection();
        return new ColumnsMetadata(con, catalogPattern, schemaPattern, tableNamePattern, columnNamePattern)
                .getMetadata().stream();
    }

    @Test
    void trivia() {
        assertTrue(collect(null, null, null, null)
                .count() > 0);

    }

    @Test
    void byColumnName() {
        assertEquals(1, collect(null, "test", "TEST", "FIRST_NAME")
                .count());

    }



}