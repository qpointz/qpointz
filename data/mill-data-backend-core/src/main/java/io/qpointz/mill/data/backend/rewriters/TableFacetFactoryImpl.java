package io.qpointz.mill.data.backend.rewriters;

import io.qpointz.mill.security.authorization.policy.PolicyEvaluator;
import io.qpointz.mill.security.authorization.policy.actions.ExpressionFilterAction;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.dispatchers.SecurityDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.substrait.dsl.SubstraitBuilder;
import io.substrait.expression.Expression;
import io.substrait.extension.DefaultExtensionCatalog;
import io.substrait.type.TypeCreator;
import lombok.val;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.qpointz.mill.security.authorization.policy.ActionVerb.ALLOW;
import static io.qpointz.mill.security.authorization.policy.ActionVerb.DENY;

public class TableFacetFactoryImpl implements TableFacetFactory {

    private final PolicyEvaluator policyEvaluator;
    private final SchemaProvider schemaProvider;
    private final SecurityDispatcher securityDispatcher;
    private final SqlProvider sqlProvider;
    private final SubstraitDispatcher substraitDispatcher;

    public TableFacetFactoryImpl(PolicyEvaluator policyEvaluator,
                             SecurityDispatcher securityDispatcher,
                             SchemaProvider schemaProvider,
                             SqlProvider sqlProvider,
                             SubstraitDispatcher substraitDispatcher) {
        this.policyEvaluator = policyEvaluator;
        this.securityDispatcher = securityDispatcher;
        this.schemaProvider = schemaProvider;
        this.sqlProvider = sqlProvider;
        this.substraitDispatcher = substraitDispatcher;

    }

    public TableFacetsCollection facets() {
        val facets = new HashMap<List<String>, TableFacet>();
        StreamSupport.stream(this.schemaProvider.getSchemaNames().spliterator(), false)
                .map(k-> getFacets(k))
                .forEach(facets::putAll);
        return new TableFacetsCollection(facets);
    }

    private Map<List<String>, TableFacet> getFacets(String schemaName) {
        return this.schemaProvider.getSchema(schemaName).getTablesList().stream()
                .map(z-> List.of(schemaName, z.getName()))
                .collect(Collectors.toMap(z -> z , o-> getFacet(o)));
    }

    static final TypeCreator R = TypeCreator.of(false);
    static final TypeCreator N = TypeCreator.of(true);

    private TableFacet getFacet(List<String> subject) {
        Expression allowExpression = null;
        val allExpressions = new ArrayList<Expression>();

        val allowFilter = policyEvaluator.actionsBy(ExpressionFilterAction.class, ALLOW, subject);
        if (!allowFilter.isEmpty()) {
            allowExpression = buildAllowExpression(allowFilter, SubstraitBuilder::or);
            allExpressions.add(allowExpression);
        }

        val tableFacetBuilder = TableFacet.builder()
                .attributeFacet(null);

        val denyExpressions = buildExpressions(new ArrayList(policyEvaluator.actionsBy(ExpressionFilterAction.class, DENY, subject)));
        if (!denyExpressions.isEmpty()) {
            allExpressions.addAll(denyExpressions);
        }

        if (allExpressions.isEmpty()) {
            return tableFacetBuilder.build();
        }

        val args = new Expression[] {};
        allExpressions.toArray(args);

        Expression filterExpression = null;

        if (allExpressions.size()>1) {
            filterExpression = substraitDispatcher.newSubstraitBuilder()
                    .scalarFn(DefaultExtensionCatalog.FUNCTIONS_BOOLEAN, "and:bool", N.BOOLEAN, args);
        } else {
            filterExpression = allExpressions.get(0);
        }

        return tableFacetBuilder
                .recordFacet(new RecordFacet(filterExpression))
                .build();
    }

    private Expression buildAllowExpression(Collection<ExpressionFilterAction> allowFilter, BiFunction<SubstraitBuilder, Expression[], Expression> combine) {
        final var exps = buildExpressions(allowFilter);
        val expsA = new Expression[] {};
        exps.toArray(expsA);
        return combine.apply(this.substraitDispatcher.newSubstraitBuilder(), expsA);
    }

    private List<Expression> buildExpressions(Collection<ExpressionFilterAction> allowFilter) {
        val parsed = allowFilter.stream()
                .map(k-> this.sqlProvider.parseSqlExpression(k.getTableName(), k.getExpression()))
                .toList();
        val anyException = parsed.stream()
                .filter(k-> !k.isSuccess()).findAny();

        if (anyException.isPresent()) {
            throw new RuntimeException(anyException.get().exception());
        }

        return parsed.stream()
                .map(SqlProvider.ExpressionParseResult::expression)
                .toList();
    }


}
