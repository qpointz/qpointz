package io.qpointz.delta.service;

import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;


public interface ExecutionProvider {
    VectorBlockIterator execute(Plan plan, QueryExecutionConfig config);
}
