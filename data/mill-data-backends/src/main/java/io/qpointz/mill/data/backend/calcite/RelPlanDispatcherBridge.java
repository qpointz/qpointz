package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.data.backend.SubstraitPlanExecutor;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.vectors.VectorBlockIterator;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.rel.RelRoot;

/**
 * Executes composed {@link RelRoot} plans via Rel→Substrait conversion and {@link SubstraitPlanExecutor}.
 */
@RequiredArgsConstructor
public class RelPlanDispatcherBridge {

    private final RelToSubstraitPlanConverter converter;
    private final SubstraitPlanExecutor executor;

    /**
     * @param relRoot composed relational plan
     * @param config execution options
     * @return streaming vector blocks from the dispatcher path
     */
    public VectorBlockIterator execute(RelRoot relRoot, QueryExecutionConfig config) {
        var plan = converter.convert(relRoot);
        return executor.execute(plan, config);
    }

    /**
     * @param relRoot composed relational plan
     * @return streaming vector blocks with default fetch size
     */
    public VectorBlockIterator execute(RelRoot relRoot) {
        return execute(relRoot, QueryExecutionConfig.newBuilder().setFetchSize(512).build());
    }
}
