package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.*;
import com.google.protobuf.ByteString;
import io.qpointz.rapids.testing.H2Db;
import lombok.extern.slf4j.Slf4j;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CalciteDataServiceSerializationTest {

    private static CalciteDataService service;
    private static H2Db db;

    @BeforeAll
    public static void createService() throws ClassNotFoundException {
        db = H2Db.create("servicetest", H2Db.scriptFromResource("h2/sample-model.sql"));
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


    private AbstractRapidsDataService.ExecQueryStreamResult execTestCase(String testCase) {
        final var res = service.execSql("SELECT * FROM `servicetest`.`DTYPES` WHERE `TEST`='" + testCase + "' ORDER BY `ID`", 15);
        log.info("exec sql for testcase:{}, status:{}={}", testCase, res.getStatus().getCode(), res.getStatus().getMessage());
        assertEquals(ResponseCode.OK, res.getStatus().getCode(), "performed successfully");
        assertTrue(res.getVectorBlocks().hasNext(), "has non empty results");
        return res;
    }

    @Test
    void serviceReturnData() {
        final var res = service.execSql("SELECT * FROM `servicetest`.`DTYPES`", 12);
        log.info("Response status:{}={}", res.getStatus().getCode(), res.getStatus().getMessage());
        assertEquals(ResponseCode.OK, res.getStatus().getCode());
    }

    private Field columnOf(AbstractRapidsDataService.ExecQueryStreamResult result, String columnName) {
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

    private <TType, TVectorType> void testSerialization(String caseName,
                                                        String columnName,
                                                        TType nullValue,
                                                        Function<Vector, Boolean> hasVectorOf,
                                                        Function<Vector, TVectorType> getTypeVector,
                                                        BiFunction<TVectorType, Integer, TType > getValue,
                                                        BiFunction<TVectorType, Integer, Boolean > getNull,
                                                        List<Optional<TType>> expected
                                                        ) {
        final var res = execTestCase(caseName);
        //TODO:makesure consuming all result and closing result set ugly hack
        final var vBlock = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        res.getVectorBlocks(), Spliterator.NONNULL),false)
                        .toList()
                        .get(0);
        final var field = columnOf(res, columnName);
        final var varVector = vBlock.getVectors(field.getIndex());
        assertTrue(hasVectorOf.apply(varVector), "Expected vector of type");
        final var typeVector = getTypeVector.apply(varVector);

        for (var i=0 ; i<expected.size();i++) {
            var expV = expected.get(i);
            if (expV.isEmpty()) {
                assertEquals(nullValue, getValue.apply(typeVector, i), String.format("Case %s column %s: value index %d: Expected null value %s", caseName, columnName, i, nullValue.toString()));
                assertTrue(getNull.apply(typeVector, i), String.format("Case %s column %s: null index %d: Expected to be null", caseName, columnName, i));
            } else {
                assertEquals(expV.get(), getValue.apply(typeVector, i), String.format("Case %s column %s: value index %d: Expected not null value", caseName, columnName, i));
                assertFalse(getNull.apply(typeVector, i), String.format("Case %s column %s: null index %d: Expected NOT to be null", caseName, columnName, i));
            }
        }
    }

    @Test
    void stringSerializationFixedWidth() {
        testSerialization("STRING", "STR", ""
                , Vector::hasStringVector, Vector::getStringVector
                , StringVector::getValues, StringVector::getNulls
                , List.of(
                        Optional.of(org.apache.commons.lang3.StringUtils.rightPad("FIXED", 20)),
                        Optional.empty()
                )
        );
    }

    @Test
    void stringSerializationVariableLength() {
        testSerialization("STRING", "STR_VAR", ""
                , Vector::hasStringVector, Vector::getStringVector
                , StringVector::getValues, StringVector::getNulls
                , List.of(
                        Optional.of("VARIABLE_STRING"),
                        Optional.empty()
                )
        );
    }

    @Test
    void booleanSerialization() {
        testSerialization("BOOL","BOOL", false
                , Vector::hasBoolVector, Vector::getBoolVector
                , BoolVector::getValues, BoolVector::getNulls
                , List.of(Optional.empty(), Optional.of(false), Optional.of(true))
        );
    }

    @Test
    void int32Serialization() {
        testSerialization("INT32","INT32F", 0
                , Vector::hasInt32Vector, Vector::getInt32Vector
                , Int32Vector::getValues, Int32Vector::getNulls
                , List.of(Optional.empty(), Optional.of(234))
        );
    }

    @Test
    void int64Serialization() {
        testSerialization("INT64","INT64F", 0L
                , Vector::hasInt64Vector, Vector::getInt64Vector
                , Int64Vector::getValues, Int64Vector::getNulls
                , List.of(Optional.empty(), Optional.of(1234L))
        );
    }

    @Test
    void doubleSerialization() {
        testSerialization("DOUBLE","DOUBLEF", 0D
                , Vector::hasDoubleVector, Vector::getDoubleVector
                , DoubleVector::getValues, DoubleVector::getNulls
                , List.of(Optional.empty(), Optional.of(-1.234D))
        );
    }

    @Test
    void floatSerialization() {
        testSerialization("FLOAT","FLOATF", 0F
                , Vector::hasFloatVector, Vector::getFloatVector
                , FloatVector::getValues, FloatVector::getNulls
                , List.of(Optional.empty(), Optional.of(567890.3824F))
        );
    }

    @Test
    void binaryVarSerialization() {
        testSerialization("BIN_VAR","BIN_VAR", ByteString.EMPTY
                , Vector::hasByteVector, Vector::getByteVector
                , ByteVector::getValues, ByteVector::getNulls
                , List.of(Optional.empty(), Optional.of(ByteString.copyFrom(new byte[]{(byte)0x07, (byte)0xA2, (byte)0xFE, (byte)0x92,
                                                                                     (byte)0x65, (byte)0xFF, (byte)0xE4})))
        );
    }

    @Test
    void binaryFixSerialization() {
        testSerialization("BIN_FIX","BIN_FIX", ByteString.EMPTY
                , Vector::hasByteVector, Vector::getByteVector
                , ByteVector::getValues, ByteVector::getNulls
                , List.of(Optional.empty(), Optional.of(ByteString.copyFrom(new byte[]{(byte)0x01, (byte)0xFF, (byte)0xFA, (byte)0xCD, (byte)0x03,
                                                                                     (byte)0x2D, (byte)0xD3, (byte)0x00, (byte)0x00, (byte)0x00})))
        );
    }

}
