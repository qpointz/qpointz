package io.qpointz.mill.data.backend;

import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.RequiredArgsConstructor;

/**
 * Executes Substrait {@link Plan} instances through the existing {@link DataOperationDispatcher} path.
 */
@RequiredArgsConstructor
public class SubstraitPlanExecutor {

    private static final int DEFAULT_FETCH_SIZE = 512;

    private final DataOperationDispatcher dispatcher;
    private final SubstraitDispatcher substraitDispatcher;

    /**
     * @param plan Substrait logical plan
     * @param config execution options
     * @return streaming vector blocks
     */
    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        var proto = substraitDispatcher.planToProto(plan);
        var request = QueryRequest.newBuilder()
                .setConfig(config)
                .setPlan(proto)
                .build();
        return dispatcher.execute(request);
    }

    /**
     * @param plan Substrait logical plan
     * @return streaming vector blocks with default fetch size
     */
    public VectorBlockIterator execute(Plan plan) {
        return execute(plan, defaultConfig());
    }

    private static QueryExecutionConfig defaultConfig() {
        return QueryExecutionConfig.newBuilder().setFetchSize(DEFAULT_FETCH_SIZE).build();
    }
}
