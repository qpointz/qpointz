package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.*;
import com.google.protobuf.ByteString;
import io.qpointz.rapids.testing.H2Db;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CalciteDataServiceSerializationTest {

    private static CalciteDataService service;
    private static H2Db db;

    @BeforeAll
    public static void createService() throws ClassNotFoundException {
        db = H2Db.create("servicetest", H2Db.scriptFromResource("h2-test/sample.sql"));
        service = CalciteDataServiceConfig.builder()
                .defaultConfig()
                .add(db.schemaFactory(), db.getSchemaName(), db.schemaOperand())
                .buildService();
    }

    @AfterAll
    public static void destroyService() throws IOException {
        log.info("Shutting down test");
        db.close();
        log.info("Db closed");
    }


    @Test
    void serviceReturnData() {
        final var res = service.execSqlStreamed("SELECT * FROM `servicetest`.`DTYPES`", 12);
        log.info("Response status:{}={}", res.getStatus().getCode(), res.getStatus().getMessage());
        assertEquals(ResponseCode.OK, res.getStatus().getCode());
    }

    private Field columnOf(ExecQueryResponse result, String columnName) {
        final var mayBeColumn = result.getSchema()
              .getFieldsList()
              .stream()
              .filter(k-> k.getName().equals(columnName))
              .findFirst();
        if (mayBeColumn.isEmpty()) {
            throw new RuntimeException(String.format("Column %s not present", columnName));
        }
        return mayBeColumn.get();
    }

    private <TType> void testSerialization(String caseName,
                                           String columnName,
                                           TType nullValue,
                                           List<Optional<TType>> expected,
                                           VectorReader<TType> reader ) {
        final var req = ExecQueryRequest.newBuilder()
                .setSql("SELECT * FROM `servicetest`.`DTYPES` WHERE `TEST`='" + caseName + "' ORDER BY `ID`")
                .build();
        final var res = service.onExecQuery(req);
        log.info("exec sql for testcase:{}, status:{}={}", caseName, res.getStatus().getCode(), res.getStatus().getMessage());
        assertEquals(ResponseCode.OK, res.getStatus().getCode(), "performed successfully");
        assertTrue(res.getVector().getVectorSize()>0, "has non empty results");

        final var vBlock  = res.getVector();

        final var field = columnOf(res, columnName);
        final var fieldIdx = field.getIndex();
        for (var i =0;i< expected.size();i++) {
            var exp = expected.get(i);
            var expV = exp.orElse(nullValue);
            var act = reader.read(vBlock, fieldIdx, i);
            assertTrue(exp.isEmpty() == act.isEmpty(), String.format("Case %s column %s: value index %d: Expected null: %s. Got %s", caseName, columnName, i, exp.isEmpty(), exp.isEmpty()));
            assertTrue(exp.equals(act), String.format("Case %s column %s: index %d: Expected %s got %s", caseName, columnName, i, exp, act));
        }

    }

    @Test
    void stringSerializationFixedWidth() {
        testSerialization("STRING", "STR", ""
                , List.of(
                        Optional.of(org.apache.commons.lang3.StringUtils.rightPad("FIXED", 20)),
                        Optional.empty()
                ),
                RapidsTypes.STRING.VECTOR_READER
        );
    }

    @Test
    void stringSerializationVariableLength() {
        testSerialization("STRING", "STR_VAR", ""
                , List.of(
                        Optional.of("VARIABLE_STRING"),
                        Optional.empty()
                ),
                RapidsTypes.STRING.VECTOR_READER
        );
    }

    @Test
    void booleanSerialization() {
        testSerialization("BOOL","BOOL", false
                , List.of(Optional.empty(), Optional.of(false), Optional.of(true)),
                RapidsTypes.BOOLEAN.VECTOR_READER
        );
    }

    @Test
    void int32Serialization() {
        testSerialization("INT32","INT32F", 0
                , List.of(Optional.empty(), Optional.of(234)),
                RapidsTypes.INT32.VECTOR_READER
        );
    }

    @Test
    void int64Serialization() {
        testSerialization("INT64","INT64F", 0L
                , List.of(Optional.empty(), Optional.of(1234L)),
                RapidsTypes.INT64.VECTOR_READER
        );
    }

    @Test
    void doubleSerialization() {
        testSerialization("DOUBLE","DOUBLEF", 0D
                , List.of(Optional.empty(), Optional.of(-1.234D)),
                RapidsTypes.DOUBLE.VECTOR_READER
        );
    }

    @Test
    void floatSerialization() {
        testSerialization("FLOAT","FLOATF", 0F
                , List.of(Optional.empty(), Optional.of(567890.3824F)),
                RapidsTypes.FLOAT.VECTOR_READER
        );
    }

    @Test
    void binaryVarSerialization() {
        testSerialization("BIN_VAR","BIN_VAR", ByteString.EMPTY
                , List.of(Optional.empty(), Optional.of(ByteString.copyFrom(new byte[]{(byte)0x07, (byte)0xA2, (byte)0xFE, (byte)0x92,
                                                                                     (byte)0x65, (byte)0xFF, (byte)0xE4}))),
                RapidsTypes.BYTES.VECTOR_READER
        );
    }

    @Test
    void binaryFixSerialization() {
        testSerialization("BIN_FIX","BIN_FIX", ByteString.EMPTY
                , List.of(Optional.empty(), Optional.of(ByteString.copyFrom(new byte[]{(byte)0x01, (byte)0xFF, (byte)0xFA, (byte)0xCD, (byte)0x03,
                                                                                     (byte)0x2D, (byte)0xD3, (byte)0x00, (byte)0x00, (byte)0x00}))),
                RapidsTypes.BYTES.VECTOR_READER
        );
    }

    @Test
    void dateSerialization() {
        testSerialization("DATE","DATEF", RapidsTypes.DATE.javaNullValue()
                , List.of(Optional.empty(), Optional.of(LocalDate.of(2013,12,31))),
                RapidsTypes.DATE.VECTOR_READER
        );
    }

    @Test
    void timeSerialization() {
        testSerialization("TIME", "TIME_NO_TZ",
                        RapidsTypes.TIME.javaNullValue(),
                        List.of(
                                Optional.empty(),
                                Optional.of(LocalTime.of(10, 12, 24))
                        ),
                        RapidsTypes.TIME.VECTOR_READER
                );
    }

    @Test
    void timeStampSerialization() {
        testSerialization("TIME", "TS_NO_TZ", RapidsTypes.DATETIME.javaNullValue(),
            List.of(
                    Optional.empty(),
                    Optional.of(LocalDateTime.of(2014,7, 25,11,15,16, 123*1000000))
            ),
            RapidsTypes.DATETIME.VECTOR_READER
        );
    }

}
