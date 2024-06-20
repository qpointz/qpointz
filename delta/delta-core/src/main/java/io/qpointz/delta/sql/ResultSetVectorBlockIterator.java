package io.qpointz.delta.sql;

import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.proto.VectorBlockSchema;
import io.qpointz.delta.sql.types.TypeHandler;
import io.qpointz.delta.sql.types.VectorProducer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class ResultSetVectorBlockIterator implements VectorBlockIterator {

    @Getter
    private final ResultSet resultSet;

    @Getter
    private final int batchSize;

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
        var result = this.block != null;
        return result;
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
        var columnCount = -1;
        List<VectorProducer> producers = null;

        try {
            val md = this.resultSet.getMetaData();
            val sb = new ResultSetMetadataToSubstrait(md);
            producers = sb.asTypeHandlers().stream()
                    .map(TypeHandler::createVectorProducer)
                    .toList();
            columnCount = md.getColumnCount();
            this.schema = sb.asVectorBlockSchema();

            while (this.resultSet.next()) {
                for (var column = 0; column < columnCount; column++) {
                    producers.get(column).read(this.resultSet, column + 1);
                }
                rowIndex++;

                if (rowIndex >= this.batchSize) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (rowIndex>0) {
            final var blockBuilder = VectorBlock.newBuilder()
                    .setSchema(schema)
                    .setVectorSize(rowIndex);

            for (int column = 0; column < columnCount; column++) {
                val vector = producers.get(column).toVector();
                blockBuilder.addVectors(vector);
            }
            this.block = blockBuilder.build();
        }
        this.didNext = true;
    }
}
