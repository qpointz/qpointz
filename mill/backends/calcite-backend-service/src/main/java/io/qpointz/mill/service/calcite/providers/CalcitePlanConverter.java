package io.qpointz.mill.service.calcite.providers;

import io.qpointz.mill.service.calcite.CalciteContext;
import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.substrait.isthmus.SubstraitRelNodeConverter;
import io.substrait.plan.Plan;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriterConfig;

import java.util.Objects;
import java.util.function.UnaryOperator;

@AllArgsConstructor
public class CalcitePlanConverter implements PlanConverter {

    @Getter(AccessLevel.PROTECTED)
    private CalciteContextFactory ctxFactory;

    @Getter(AccessLevel.PROTECTED)
    private SqlDialect dialect;

    public RelNode toRelNode(Plan plan) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            return toRelNode(plan, ctx);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RelNode toRelNode(Plan plan, CalciteContext ctx) {
        try {
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toSql(Plan plan) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            val relNode = toRelNode(plan, ctx);
            return toSql(relNode);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toSql(RelNode plan) {
        try {
            final RelToSqlConverter converter = new RelToSqlConverter(this.getDialect());
            final SqlNode sqlNode = converter.visitRoot(plan).asStatement();

            UnaryOperator<SqlWriterConfig> transform = c ->
                    c.withAlwaysUseParentheses(false)
                            .withSelectListItemsOnSeparateLines(false)
                            .withUpdateSetListNewline(false)
                            .withIndentation(0);

            return sqlNode.toSqlString(c -> transform.apply(c.withDialect(this.getDialect()))).getSql();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
