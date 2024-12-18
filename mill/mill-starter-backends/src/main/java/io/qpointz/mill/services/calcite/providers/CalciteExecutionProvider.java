package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.isthmus.SubstraitRelNodeConverter;
import io.substrait.plan.Plan;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexBuilder;

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
            val stmt = ctx.getRelRunner().prepareStatement(node);
            val resultSet = stmt.executeQuery();
            return new ResultSetVectorBlockIterator(resultSet, config.getFetchSize());
        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }

}
