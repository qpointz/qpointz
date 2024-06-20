package io.qpointz.delta.sql;

import java.sql.ResultSet;

public class VectorBlockIterators {

    private VectorBlockIterators() {}

    public static VectorBlockIterator fromResultSet(ResultSet resultSet, int batchSize) {
        return new ResultSetVectorBlockIterator(resultSet, batchSize);
    }

}
