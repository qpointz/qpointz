package io.qpointz.delta.service;

import io.qpointz.delta.proto.PreparedStatement;
import io.qpointz.delta.proto.VectorBlock;

import java.util.Iterator;

public interface ExecutionProvider {

    SqlExecutionProvider getSqlExecutionProvider();

    SubstraitExecutionProvider getSubstraitExecutionProvider();

    default boolean canExecuteSql() {
        return this.getSqlExecutionProvider()!=null;
    }

    default boolean canExecuteSubstraitPlan() {
        return this.getSubstraitExecutionProvider()!=null;
    }

    //Iterator<VectorBlock> execute(PreparedStatement statement, int batchSize);
}
