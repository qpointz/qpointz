package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;


public class VectorBlockBatchedIterator implements Iterator<VectorBlock> {

    private final Schema schema;
    private final ResultSet resultSet;
    private final int batchSize;
    private final VectorConsumer<?>[] consumers;
    private final int columnCount;

    private boolean didNext = false;
    private VectorBlock block;

    public VectorBlockBatchedIterator(Schema schema, ResultSet resultSet, int batchSize) {
        this.schema = schema;
        this.resultSet = resultSet;
        this.batchSize = batchSize;

        this.columnCount = schema.getFieldsCount();
        this.consumers = new VectorConsumer<?>[this.columnCount];
        for (var i=0;i<this.columnCount;i++) {
            final var field = this.schema.getFields(i);
            final var consumer = VectorConsumers.of(field);
            consumer.init(batchSize);
            this.consumers[i] = consumer;
        }
    }

    @SneakyThrows
    @Override
    public boolean hasNext() {
        if (!didNext) {
            doNext();
        }
        var result = this.block!=null;
        if (!result && !resultSet.isClosed()) {
            resultSet.close();
        }
        return result;
    }

    @SneakyThrows
    @Override
    public VectorBlock next() {
        if (!didNext) {
            doNext();
        }
        this.didNext = false;
        return block;
    }

    private void doNext() throws SQLException {
        var rowIndex = 0;
        this.block = null;

        while (this.resultSet.next()) {
            for (var column=0;column<this.columnCount;column++) {
                this.consumers[column].read(this.resultSet, column+1, rowIndex);
            }
            rowIndex++;

            if (rowIndex>=batchSize) {
                break;
            }
        }

        if (rowIndex>0) {

            final var blockBuilder = VectorBlock.newBuilder();

            for (int column = 0; column < this.columnCount; column++) {
                final var consumer = this.consumers[column];
                final var vectorBuilder = Vector.newBuilder();
                consumer.vector(vectorBuilder);
                blockBuilder.addVectors(vectorBuilder);
                consumer.reset();
            }

            blockBuilder.setVectorSize(rowIndex);

            this.block = blockBuilder.build();
        }
        this.didNext = true;
    }
}
