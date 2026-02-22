package io.qpointz.mill.data.backend.calcite.providers;

import io.qpointz.mill.data.backend.calcite.CalciteContext;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.substrait.extension.SimpleExtension;
import io.substrait.isthmus.SubstraitRelNodeConverter;
import io.substrait.plan.Plan;
import lombok.*;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriterConfig;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.util.Objects;
import java.util.function.UnaryOperator;

@AllArgsConstructor
public class CalcitePlanConverter implements PlanConverter {

    @Getter(AccessLevel.PROTECTED)
    private CalciteContextFactory ctxFactory;

    @Getter(AccessLevel.PROTECTED)
    private SqlDialect dialect;

    @Getter
    private SimpleExtension.ExtensionCollection extensionCollection;

    public ConvertedPlanRelNode toRelNode(Plan plan) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            return toRelNode(plan, ctx);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static class PlanConverter extends SubstraitRelNodeConverter {

        public PlanConverter(SimpleExtension.ExtensionCollection extensions, RelDataTypeFactory typeFactory, RelBuilder relBuilder) {
            super(extensions, typeFactory, relBuilder);
        }

        public static ConvertedPlanRelNode convertPlan(
                Plan relPlan,
                RelOptCluster relOptCluster,
                Prepare.CatalogReader catalogReader,
                SqlParser.Config parserConfig,
                SimpleExtension.ExtensionCollection extensionCollection) {
            var relBuilder = RelBuilder.create(
                            Frameworks.newConfigBuilder()
                                    .parserConfig(parserConfig)
                                    .defaultSchema(catalogReader.getRootSchema().plus())
                                    .traitDefs()
                                    .programs()
                                    .build());
            val converter = new PlanConverter(extensionCollection , relOptCluster.getTypeFactory(), relBuilder);
            return converter.convert(relPlan);
        }

        public ConvertedPlanRelNode convert(Plan plan) {
            val root = plan.getRoots().get(0);
            RelNode topMost = root.getInput().accept(this);
            topMost.getCluster().getTypeFactory();
            return new ConvertedPlanRelNode(topMost, root.getNames());
        }
    }

    public ConvertedPlanRelNode toRelNode(Plan plan, CalciteContext ctx) {
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
            return PlanConverter.convertPlan(plan, relOptCluster, catalogReader, ctx.getParserConfig(), this.extensionCollection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConvertedPlanSql toSql(Plan plan) {
        try {
            val ctx = Objects.requireNonNull(this.getCtxFactory().createContext());
            val relNode = toRelNode(plan, ctx);
            val sql = toSql(relNode.node());
            return new ConvertedPlanSql(sql.sql(), plan.getRoots().get(0).getNames());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConvertedPlanSql toSql(RelNode plan) {
        try {
            final RelToSqlConverter converter = new RelToSqlConverter(this.getDialect());
            final SqlNode sqlNode = converter.visitRoot(plan).asStatement();

            UnaryOperator<SqlWriterConfig> transform = c ->
                    c.withAlwaysUseParentheses(false)
                            .withSelectListItemsOnSeparateLines(false)
                            .withUpdateSetListNewline(false)
                            .withIndentation(0);

            return new ConvertedPlanSql(
                    sqlNode.toSqlString(c -> transform.apply(c.withDialect(this.getDialect()))).getSql(),
                    plan.getRowType().getFieldNames());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
