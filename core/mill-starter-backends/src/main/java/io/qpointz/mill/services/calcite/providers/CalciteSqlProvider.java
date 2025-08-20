package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.services.calcite.CalciteContextFactory;
//import io.qpointz.mill.services.calcite.istmus.SqlToSubstrait;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.substrait.expression.Expression;
import io.substrait.isthmus.CallConverter;
import io.substrait.isthmus.SubstraitRelVisitor;
import io.substrait.isthmus.TypeConverter;
import io.substrait.isthmus.expression.*;
import io.substrait.proto.Plan;
import io.substrait.proto.PlanRel;
import lombok.*;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class CalciteSqlProvider implements SqlProvider {

    @Getter
    private final CalciteContextFactory ctxFactory;

    @Getter
    private final SubstraitDispatcher substraitDispatcher;

    @Override
    public PlanParseResult parseSql(String sql) {
        try (val ctx = this.ctxFactory.createContext()) {
            val config = ctx.getFrameworkConfig();
            val planner = Frameworks.getPlanner(config);

            val parsed = planner.parse(sql);
            val validated = planner.validate(parsed);
            val relRoot = planner.rel(validated);

            val root = SubstraitRelVisitor.convert(relRoot, substraitDispatcher.getExtensionCollection());
            val plan = io.substrait.plan.Plan.builder().addRoots(root)
                            .build();
            return PlanParseResult.success(plan);
        }
        catch (Exception e) {
            return PlanParseResult.fail(e);
        }
    }

    @Override
    public ExpressionParseResult parseSqlExpression(List<String> tableName, String expression) {
        try (val ctx = this.getCtxFactory().createContext()) {
            val frameworkConfig = ctx.getFrameworkConfig();
            val typeFactory = ctx.getTypeFactory();

            val calciteCatalogReader = new CalciteCatalogReader(
                    ctx.getCalciteRootSchema(),
                    frameworkConfig.getDefaultSchema().getSubSchemaNames().stream().toList(),
                    typeFactory,
                    ctx.getCalciteConnection().config()
            );

            val defaultValidator = SqlValidatorUtil.newValidator(
                    frameworkConfig.getOperatorTable(),
                    calciteCatalogReader, typeFactory
            );

            val relExpressionOptimizationClustet = RelOptCluster.create(
                    new VolcanoPlanner(),
                    new RexBuilder(typeFactory)
            );

            val relConverter = new SqlToRelConverter(
                    frameworkConfig.getViewExpander(),
                    defaultValidator,
                    calciteCatalogReader,
                    relExpressionOptimizationClustet,
                    frameworkConfig.getConvertletTable(),
                    frameworkConfig.getSqlToRelConverterConfig()
            );

            val nameToNode = new HashMap<String, RexNode>();
            val rexBuilder = new RexBuilder(typeFactory);
            val rowType = calciteCatalogReader.getTable(tableName).getRowType();

            val relBulder = RelBuilder.create(frameworkConfig);
            val sta = relBulder
                    .scan(tableName)
                            .build();

            rowType.getFieldList().stream().forEach(field -> {
                val inputRef = rexBuilder.makeInputRef(sta, field.getIndex());
                nameToNode.put(field.getName(), inputRef);
            });

            val sqlParser = SqlParser.create(
                    expression,
                    frameworkConfig.getParserConfig()
            );

            val sqlNode = sqlParser.parseExpression();
            val rxNode =relConverter.convertExpression(sqlNode, nameToNode);

            val extensionCollection = this.getSubstraitDispatcher()
                    .getExtensionCollection();

            val substVisitor = new SubstraitRelVisitor(typeFactory, extensionCollection, null);

            val typeConverter = TypeConverter.DEFAULT;
            val converters = new ArrayList<CallConverter>();
            converters.addAll(CallConverters.defaults(typeConverter));
            converters.add(new ScalarFunctionConverter(extensionCollection.scalarFunctions(), typeFactory));
            converters.add(CallConverters.CREATE_SEARCH_CONV.apply(new RexBuilder(typeFactory)));

            val windowFunctionConverter =
                    new WindowFunctionConverter(extensionCollection.windowFunctions(), typeFactory);
            val rexExpressionConverter =
                    new RexExpressionConverter(substVisitor, converters, windowFunctionConverter, typeConverter);

            Expression exp = rxNode.accept(rexExpressionConverter);

            return new ExpressionParseResult(true, exp, null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
