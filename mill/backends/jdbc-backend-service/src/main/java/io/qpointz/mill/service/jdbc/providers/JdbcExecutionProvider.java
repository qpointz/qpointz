package io.qpointz.mill.service.jdbc.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;

public class JdbcExecutionProvider implements ExecutionProvider {

    @Override
    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
