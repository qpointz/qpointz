package io.qpointz.mill.data.backend.export;

import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.dispatchers.PlanHelper;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.RequiredArgsConstructor;

/**
 * Runs data-plane queries for HTTP export: builds {@link QueryRequest} instances and returns
 * {@link VectorBlockIterator} results without format or transport awareness.
 */
@RequiredArgsConstructor
public class ExportVectorBlockSource {

    private static final int DEFAULT_FETCH_SIZE = 512;

    private final DataOperationDispatcher dispatcher;
    private final PlanHelper planHelper;
    private final SubstraitDispatcher substraitDispatcher;

    /**
     * Plan-native full table scan (Substrait named scan; no hand-written SQL string).
     *
     * @param schemaName physical schema
     * @param tableName  physical table
     * @return streaming vector blocks
     */
    public VectorBlockIterator exportTable(String schemaName, String tableName) {
        Plan plan = planHelper.createPlan(planHelper.createNamedScan(schemaName, tableName));
        io.substrait.proto.Plan proto = substraitDispatcher.planToProto(plan);
        QueryRequest request = QueryRequest.newBuilder()
                .setConfig(defaultConfig())
                .setPlan(proto)
                .build();
        return dispatcher.execute(request);
    }

    /**
     * Ad-hoc SQL using the configured SQL parse path on the dispatcher.
     *
     * @param sql SQL text
     * @return streaming vector blocks
     */
    public VectorBlockIterator exportSql(String sql) {
        QueryRequest request = QueryRequest.newBuilder()
                .setConfig(defaultConfig())
                .setStatement(SQLStatement.newBuilder().setSql(sql).build())
                .build();
        return dispatcher.execute(request);
    }

    private static QueryExecutionConfig defaultConfig() {
        return QueryExecutionConfig.newBuilder().setFetchSize(DEFAULT_FETCH_SIZE).build();
    }
}
