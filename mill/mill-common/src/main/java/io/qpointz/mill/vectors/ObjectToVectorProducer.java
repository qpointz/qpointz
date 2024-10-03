package io.qpointz.mill.vectors;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.RecordReaderResultSetBase;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import io.qpointz.mill.types.logical.LogicalType;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

public class ObjectToVectorProducer<T> {

    private final List<MapperInfo<T, ?>> mappers;

    public record MapperInfo<T,F>(String name, LogicalType<F,?> type, Function<T, Optional<F>> mapper) {

        public Vector asVector(Collection<T> col) {
            val prod = type.getVectorProducer();
            for(var v: col ) {
                val m = mapper.apply(v);
                if (m.isEmpty()) {
                    prod.append(null, true);
                    continue;
                }
                prod.append(m.get(), false);
            }
            return prod.vectorBuilder().build();
        }

        public Field.Builder getField() {
            val logicalType = LogicalDataType.newBuilder()
                    .setTypeId(type.getLogicalTypeId())
                    .build();
            val dataType = DataType.newBuilder()
                    .setNullability(DataType.Nullability.NULL)
                    .setType(logicalType);
            return Field.newBuilder()
                    .setName(name)
                    .setType(dataType);
        }
    }

    public static <T,F> MapperInfo<T,F> mapper(String name, LogicalType<F,?> type, Function<T, Optional<F>> mapper) {
        return new MapperInfo<T,F>(name,type,mapper);
    }

    public static <K> ResultSet resultSet(List<MapperInfo<K,?>> mappers, Collection<K> objects) throws SQLException {
        val om = new ObjectToVectorProducer<K>(mappers);
        val vb = om.fromCollection(objects);
        return new RecordReaderResultSetBase(new VectorBlockRecordIterator(List.of(vb).iterator()) {
            @Override
            public void close() {
            }
        }) {
            @Override
            public SQLWarning getWarnings() throws SQLException {
                return null;
            }

            @Override
            public void clearWarnings() throws SQLException {

            }

            @Override
            public String getCursorName() throws SQLException {
                return "";
            }

            @Override
            public boolean isBeforeFirst() throws SQLException {
                return false;
            }

            @Override
            public boolean isAfterLast() throws SQLException {
                return false;
            }

            @Override
            public boolean isFirst() throws SQLException {
                return false;
            }

            @Override
            public boolean isLast() throws SQLException {
                return false;
            }

            @Override
            public void beforeFirst() throws SQLException {

            }

            @Override
            public void afterLast() throws SQLException {

            }

            @Override
            public boolean first() throws SQLException {
                return false;
            }

            @Override
            public boolean last() throws SQLException {
                return false;
            }

            @Override
            public int getRow() throws SQLException {
                return 0;
            }

            @Override
            public void setFetchSize(int rows) throws SQLException {

            }

            @Override
            public int getFetchSize() throws SQLException {
                return 0;
            }

            @Override
            public Statement getStatement() throws SQLException {
                return null;
            }

            @Override
            public boolean isClosed() throws SQLException {
                return false;
            }
        };
    }

    public ObjectToVectorProducer(List<MapperInfo<T,?>> mappers) {
        this.mappers = mappers;
    }

    public VectorBlock fromCollection(Collection<T> col) {
        val vbBuilder = VectorBlock.newBuilder();
        val schemaBuilder = VectorBlockSchema.newBuilder();
        var colIdx = 0;
        for(val mp : this.mappers) {
            val v = mp.asVector(col);
            schemaBuilder
                    .addFields(mp.getField().setFieldIdx(colIdx++));
            vbBuilder.addVectors(v);
        }
        return vbBuilder
                .setSchema(schemaBuilder)
                .setVectorSize(col.size())
                .build();
    }

}
