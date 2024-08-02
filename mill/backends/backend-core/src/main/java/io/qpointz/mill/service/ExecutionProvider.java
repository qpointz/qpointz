package io.qpointz.mill.service;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;


public interface ExecutionProvider {
    VectorBlockIterator execute(Plan plan, QueryExecutionConfig config);
}