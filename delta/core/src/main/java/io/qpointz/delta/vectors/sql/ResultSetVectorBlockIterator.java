package io.qpointz.delta.vectors.sql;

import io.qpointz.delta.proto.Field;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.proto.VectorBlockSchema;
import io.qpointz.delta.types.sql.JdbcToSubstraitTypeMapper;
import io.qpointz.delta.types.sql.JdbcTypeInfo;
import io.qpointz.delta.vectors.MappingVectorProducer;
import io.qpointz.delta.vectors.VectorBlockIterator;
import io.qpointz.delta.vectors.VectorProducer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetVectorBlockIterator implements VectorBlockIterator {

    @Getter
    private final ResultSet resultSet;

    @Getter
    private final int batchSize;
    private final ResultSetColumnReader[] columnReaders;
    private final MappingVectorProducer<ResultSetColumnReader, ?>[] vectorProducers;
    private final int columnCount;

    public ResultSetVectorBlockIterator(ResultSet resultSet, int batchSize) {
        this.resultSet = resultSet;
        this.batchSize = batchSize;
        try {
            this.columnCount = this.resultSet.getMetaData().getColumnCount();
            columnReaders = new ResultSetColumnReader[columnCount];
            vectorProducers = new MappingVectorProducer[columnCount];
            val meta = resultSet.getMetaData();
            val schemaBuilder = VectorBlockSchema.newBuilder();
            for (int i = 0; i < this.columnCount; i++) {
                val colIdx = i + 1;
                columnReaders[i]=new ResultSetColumnReader(resultSet, colIdx);
                val jdbcInfo = new JdbcTypeInfo(
                        meta.getColumnType(colIdx),
                        meta.isNullable(colIdx) != ResultSetMetaData.columnNoNulls,
                        meta.getPrecision(colIdx),
                        meta.getScale(colIdx)
                );
                vectorProducers[i] = ResultSetVectorProducerFactory.DEFAULT.fromJdbcType(jdbcInfo);
                schemaBuilder.addFields(Field.newBuilder()
                                .setFieldIdx(i)
                                .setType(JdbcToSubstraitTypeMapper.DEFAULT.jdbc(jdbcInfo))
                                .setName(meta.getColumnName(colIdx))
                                .build());
            }
            this.schema = schemaBuilder.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean didNext = false;

    private VectorBlock block = null;

    private VectorBlockSchema schema = null;

    @Override
    public VectorBlockSchema schema() {
        return this.schema;
    }

    @Override
    public boolean hasNext() {
        if (!didNext) {
            doNext();
        }
        return this.block != null;
    }



    @Override
    public VectorBlock next() {
        if (!didNext) {
            doNext();
        }
        this.didNext = false;
        return block;
    }

    private void doNext() {
        var rowIndex = 0;
        this.block = null;

        try {
            while (this.resultSet.next()) {
                for (var column = 0; column < this.columnCount; column++) {
                    vectorProducers[column].append(columnReaders[column]);
                }
                rowIndex++;
                if (rowIndex >= this.batchSize) {
                    break;
                }
            }
            if (rowIndex>0) {
                val blockBuilder = VectorBlock.newBuilder()
                        .setSchema(schema)
                        .setVectorSize(rowIndex);

                for (int column = 0; column < this.columnCount; column++) {
                    final Vector vector = vectorProducers[column]
                            .vectorBuilder()
                            .setFieldIdx(column)
                            .build();
                    blockBuilder.addVectors(vector);
                    vectorProducers[column].reset();
                }
                this.block = blockBuilder.build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.didNext = true;
    }
}
