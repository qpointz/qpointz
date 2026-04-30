package io.qpointz.mill.data.backend.grpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration;
import io.qpointz.mill.data.backend.flow.SourceDefinitionReader;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.backend.grpc.config.MillGrpcConfiguration;
import io.qpointz.mill.proto.DataConnectServiceGrpc;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.QueryResultResponse;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MillGrpcSkymillQueryIT.Config.class)
@ActiveProfiles("skymill")
class MillGrpcSkymillQueryIT {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            SqlAutoConfiguration.class,
            BackendAutoConfiguration.class,
            FlowBackendAutoConfiguration.class,
            DefaultServiceConfiguration.class,
            MillGrpcConfiguration.class,
            MillGrpcService.class,
            GrpcExceptionInterceptor.class,
            GrpcServiceDescriptor.class,
    })
    static class Config {
    }

    private static final String SCHEMA = "skymill";
    private static final String QUERY_CASES_RELATIVE = "test/it-querycases/skymill-sql.json";

    @Autowired
    Server grpcServer;

    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void shouldExecuteSharedSkymillQuerySet() throws Exception {
        val queryCases = resolveQueryCasesPath();
        assertTrue(Files.exists(queryCases), "Missing query-case file at " + queryCases.toAbsolutePath());

        // Fail fast with a detailed exception if the flow source descriptor cannot be parsed.
        SourceDefinitionReader.read(Path.of("src/testIT/resources/flow-skymill-it.yaml"));

        val mapper = new ObjectMapper();
        val cases = List.of(mapper.readValue(Files.readAllBytes(queryCases), QueryCase[].class));
        assertFalse(cases.isEmpty(), "Expected at least one query case");

        val port = grpcServer.getPort();
        channel = ManagedChannelBuilder.forAddress("127.0.0.1", port).usePlaintext().build();
        val stub = DataConnectServiceGrpc.newBlockingStub(channel);

        final List<String> schemas;
        try {
            schemas = stub.listSchemas(ListSchemasRequest.getDefaultInstance()).getSchemasList();
        } catch (StatusRuntimeException e) {
            fail("listSchemas failed. status=" + e.getStatus(), e);
            return;
        }
        assertTrue(
                schemas.stream().anyMatch(s -> s.equalsIgnoreCase(SCHEMA)),
                "Expected schema " + SCHEMA + " in listSchemas=" + schemas
        );

        for (QueryCase c : cases) {
            val sql = c.sql.replace("{schema}", SCHEMA);
            val request = QueryRequest.newBuilder()
                    .setStatement(SQLStatement.newBuilder().setSql(sql).build())
                    .setConfig(QueryExecutionConfig.newBuilder().setFetchSize(50).build())
                    .build();

            final List<VectorBlock> vectors;
            try {
                val responses = stub.execQuery(request);
                vectors = collectVectors(responses);
            } catch (StatusRuntimeException e) {
                fail("[" + c.id + "] gRPC execQuery failed. sql=" + sql + " status=" + e.getStatus(), e);
                return;
            }

            assertNotNull(vectors, "[" + c.id + "] vectors must not be null");
            assertFalse(vectors.isEmpty(), "[" + c.id + "] expected at least one vector block (schema-bearing)");

            val firstSchemaFields = vectors.get(0).getSchema().getFieldsList().stream()
                    .map(f -> f.getName().toLowerCase())
                    .collect(Collectors.toSet());
            val required = c.requiredColumns.stream().map(String::toLowerCase).collect(Collectors.toSet());
            assertTrue(firstSchemaFields.containsAll(required), "[" + c.id + "] missing required columns: " + required);

            val rowCount = vectors.stream().mapToLong(VectorBlock::getVectorSize).sum();
            assertTrue(rowCount >= c.minRows, "[" + c.id + "] expected minRows=" + c.minRows + " got=" + rowCount);
            if (c.maxRows != null) {
                assertTrue(rowCount <= c.maxRows, "[" + c.id + "] expected maxRows=" + c.maxRows + " got=" + rowCount);
            }

            if (c.allRowsColumnEquals != null && rowCount > 0) {
                assertAllRowsEqual(vectors.iterator(), c.allRowsColumnEquals.column, c.allRowsColumnEquals.value, c.id);
            }
        }
    }

    private static Path resolveQueryCasesPath() {
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 8; i++) {
            Path candidate = dir.resolve(QUERY_CASES_RELATIVE);
            if (Files.exists(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
            if (dir == null) break;
        }
        return Paths.get(QUERY_CASES_RELATIVE);
    }

    private static List<VectorBlock> collectVectors(Iterator<QueryResultResponse> responses) {
        val out = new java.util.ArrayList<VectorBlock>();
        while (responses.hasNext()) {
            out.add(responses.next().getVector());
        }
        return out;
    }

    private static void assertAllRowsEqual(
            Iterator<VectorBlock> vectors,
            String column,
            String expected,
            String caseId
    ) {
        val records = VectorBlockRecordIterator.of(vectors);
        if (!records.hasNext()) {
            return;
        }
        val idx = resolveColumnIndex(records, column, caseId);
        while (records.next()) {
            val actual = records.getString(idx);
            assertEquals(expected, actual, "[" + caseId + "] unexpected value in column " + column);
        }
    }

    private static int resolveColumnIndex(VectorBlockRecordIterator records, String column, String caseId) {
        val direct = records.getColumnNames().get(column);
        if (direct != null) return direct;
        for (var e : records.getColumnNames().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(column)) {
                return e.getValue();
            }
        }
        fail("[" + caseId + "] column not found in vector schema: " + column + " available=" + records.getColumnNames().keySet());
        return -1;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QueryCase {
        public String id;
        public String description;
        public String sql;
        public long minRows;
        public Long maxRows;
        public List<String> requiredColumns = List.of();
        public ColumnEquals allRowsColumnEquals;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ColumnEquals {
        public String column;
        public String value;
    }
}

