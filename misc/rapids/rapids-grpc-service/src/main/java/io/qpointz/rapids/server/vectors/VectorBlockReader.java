package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.Schema;
import io.qpointz.rapids.grpc.Vector;
import io.qpointz.rapids.grpc.VectorBlock;
import io.qpointz.rapids.grpc.VectorConsumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VectorBlockReader {

    private final Schema schema;
    private final PreparedStatement statement;

    public VectorBlockReader(Schema schema, PreparedStatement statement) {
        this.schema = schema;
        this.statement = statement;
    }

    public static VectorBlock fromStatement(Schema schema, PreparedStatement statement) throws SQLException {
        final var reader = new VectorBlockReader(schema, statement);
        return reader.create();
    }

    public VectorBlock create() throws SQLException {
        final var columnCount = this.schema.getFieldsCount();
        final var consumers = new VectorConsumer<?>[columnCount];
        for (var i=0;i<columnCount;i++) {
            final var field = this.schema.getFields(i);
            final var consumer = VectorConsumers.of(field);
            consumer.init(0);
            consumers[i] = consumer;
        }

        final var resultSet = this.statement.executeQuery();
        var rowIndex = 0;
        while (resultSet.next()) {
            for (var column=0;column<columnCount;column++) {
                consumers[column].read(resultSet, column+1, rowIndex);
            }
            rowIndex++;
        }

        final var blockBuilder = VectorBlock.newBuilder();

        if (rowIndex>0) {
            for (int column = 0; column < columnCount; column++) {
                final var consumer = consumers[column];
                final var vectorBuilder = Vector.newBuilder();
                consumer.vector(vectorBuilder);
                blockBuilder.addVectors(vectorBuilder);
                consumer.reset();
            }
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }

        return blockBuilder
                .setVectorSize(rowIndex)
                .build();

    }

}
