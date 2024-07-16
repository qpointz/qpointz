package io.qpointz.mill.service.calcite.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.calcite.CalciteContextFactory;
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

import java.sql.SQLException;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class CalciteExecutionProvider implements ExecutionProvider {

    @Getter(AccessLevel.PROTECTED)
    private CalciteContextFactory ctxFactory;

    private RelNode toRel(Plan plan) throws Exception {
        val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
        val defaultSchemas = ctx.getFrameworkConfig()
                .getDefaultSchema()
                .getSubSchemaNames()
                .stream().toList();

        val catalogReader = new CalciteCatalogReader(ctx.getCalciteRootSchema(),
                defaultSchemas,
                ctx.getTypeFactory(),
                ctx.getCalciteConnection().config());

        val relOptCluster = RelOptCluster.create(new VolcanoPlanner(), new RexBuilder(ctx.getTypeFactory()));
        return SubstraitRelNodeConverter.convert(
                plan.getRoots().get(0).getInput(),
                relOptCluster,
                catalogReader,
                ctx.getParserConfig());
    }


    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            val node = toRel(plan);
            val stmt = ctx.getRelRunner().prepareStatement(node);
            val resultSet = stmt.executeQuery();
            return new ResultSetVectorBlockIterator(resultSet, config.getBatchSize());
        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }

}
