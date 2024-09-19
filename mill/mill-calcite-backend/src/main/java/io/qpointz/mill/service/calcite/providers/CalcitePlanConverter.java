package io.qpointz.mill.service.calcite.providers;

import com.google.common.collect.Streams;
import io.qpointz.mill.service.calcite.CalciteContext;
import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.substrait.extension.SimpleExtension;
import io.substrait.isthmus.SubstraitRelNodeConverter;
import io.substrait.plan.Plan;
import lombok.*;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriterConfig;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static class PlanConverter extends SubstraitRelNodeConverter {

        public PlanConverter(SimpleExtension.ExtensionCollection extensions, RelDataTypeFactory typeFactory, RelBuilder relBuilder) {
            super(extensions, typeFactory, relBuilder);
        }

        protected static final SimpleExtension.ExtensionCollection EXTENSION_COLLECTION;

        static {
            SimpleExtension.ExtensionCollection defaults;
            try {
                defaults = SimpleExtension.loadDefaults();
            } catch (IOException e) {
                throw new RuntimeException("Failure while loading defaults.", e);
            }

            EXTENSION_COLLECTION = defaults;
        }

        public static RelNode convertPlan(
                Plan relPlan,
                RelOptCluster relOptCluster,
                Prepare.CatalogReader catalogReader,
                SqlParser.Config parserConfig) {
            var relBuilder =
                    RelBuilder.create(
                            Frameworks.newConfigBuilder()
                                    .parserConfig(parserConfig)
                                    .defaultSchema(catalogReader.getRootSchema().plus())
                                    .traitDefs((List<RelTraitDef>) null)
                                    .programs()
                                    .build());
            val converter = new PlanConverter(EXTENSION_COLLECTION , relOptCluster.getTypeFactory(), relBuilder);
            return converter.convert(relPlan);
        }

        public RelNode convert(Plan plan) {
            val root = plan.getRoots().get(0);
            RelNode noProj = root.getInput().accept(this);
            val rexes = new ArrayList<RexNode>();
            for (int i=0;i<root.getNames().size();i++) {
                rexes.add(this.rexBuilder.makeInputRef(noProj, i));
            }
            return this.relBuilder.push(noProj)
                    .project(rexes, root.getNames())
                    .build();
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
            return PlanConverter.convertPlan(plan, relOptCluster, catalogReader, ctx.getParserConfig());
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
