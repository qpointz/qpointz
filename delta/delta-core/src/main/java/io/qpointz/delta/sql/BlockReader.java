package io.qpointz.delta.sql;

import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.proto.VectorBlock;
import io.qpointz.delta.sql.types.TypeHandler;
import io.qpointz.delta.sql.types.VectorProducer;
import lombok.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

@RequiredArgsConstructor
public class BlockReader implements Iterator<VectorBlock> {

    @Getter
    private final ResultSet resultSet;

    @Getter
    private final int batchSize;

    private boolean didNext = false;

    private VectorBlock block = null;

    public VectorBlock next(int size) throws SQLException {
        throw new SQLException("Not implemented yet");
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
        val md = this.resultSet.getMetaData();
        val sb = new ResultSetMetadataToSubstrait(md);
        val producers = sb.asTypeHandlers().stream()
                .map(TypeHandler::createVectorProducer)
                .toList();
        val columnCount = md.getColumnCount();
        val schema = sb.asTable();

        while (this.resultSet.next()) {
            for (var column=0;column<columnCount;column++) {
                producers.get(column).read(this.resultSet, column+1);
            }
            rowIndex++;

            if (rowIndex>=this.batchSize) {
                break;
            }
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
