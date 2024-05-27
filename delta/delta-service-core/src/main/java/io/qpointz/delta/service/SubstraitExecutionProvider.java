package io.qpointz.delta.service;

import io.qpointz.delta.proto.PlanStatement;
import io.qpointz.delta.proto.VectorBlock;

import java.sql.SQLException;
import java.util.Iterator;

public interface SubstraitExecutionProvider {
    public Iterator<VectorBlock> execute(PlanStatement plan, int batchSize) throws SQLException;
}
