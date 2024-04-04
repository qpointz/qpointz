package io.qpointz.delta.service;

import io.qpointz.delta.proto.SQLStatement;
import io.qpointz.delta.proto.VectorBlock;

import java.sql.SQLException;
import java.util.Iterator;

public interface SqlExecutionProvider {
    Iterator<VectorBlock> execute(SQLStatement sql, int batchSize) throws SQLException;
}
