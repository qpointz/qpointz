package io.qpointz.mill.data.backend.calcite.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class CalciteExecutionProvider implements ExecutionProvider {

    @Getter(AccessLevel.PROTECTED)
    private CalciteContextFactory ctxFactory;

    @Getter(AccessLevel.PROTECTED)
    private PlanConverter planConverter;

    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            val node = planConverter.toRelNode(plan);
            val stmt = ctx.getRelRunner().prepareStatement(node.node());
            val resultSet = stmt.executeQuery();
            return new ResultSetVectorBlockIterator(resultSet, config.getFetchSize(), node.names());
        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }

}
