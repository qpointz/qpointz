package io.qpointz.delta.calcite.providers;

import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.sql.VectorBlockIterator;
import io.qpointz.delta.sql.VectorBlockIterators;
import io.substrait.extension.ExtensionCollector;
import io.substrait.extension.ImmutableSimpleExtension;
import io.substrait.isthmus.SubstraitRelNodeConverter;
import io.substrait.isthmus.TypeConverter;
import io.substrait.isthmus.expression.AggregateFunctionConverter;
import io.substrait.isthmus.expression.ScalarFunctionConverter;
import io.substrait.isthmus.expression.WindowFunctionConverter;
import io.substrait.plan.Plan;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.tools.*;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class CalciteExecutionProvider implements ExecutionProvider {

    @Getter(AccessLevel.PROTECTED)
    private CalciteContext calciteCtx;

    private RelNode toRel(Plan plan) {
        val defaultSchemas = getCalciteCtx().getFrameworkConfig()
                .getDefaultSchema()
                .getSubSchemaNames()
                .stream().toList();

        val catalogReader = new CalciteCatalogReader(this.calciteCtx.getCalciteRootSchema(),
                defaultSchemas,
                this.calciteCtx.getTypeFactory(),
                this.calciteCtx.getCalciteConnection().config());

        val relOptCluster = RelOptCluster.create(new VolcanoPlanner(), new RexBuilder(this.calciteCtx.getTypeFactory()));
        val node = SubstraitRelNodeConverter.convert(plan.getRoots().get(0).getInput(), relOptCluster, catalogReader, this.calciteCtx.getParserConfig());
        return node;
    }


    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        try {
            val node = toRel(plan);
            val stmt = this.calciteCtx.getRelRunner().prepareStatement(node);
            val resultSet = stmt.executeQuery();
            return VectorBlockIterators.fromResultSet(resultSet, config.getBatchSize());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
