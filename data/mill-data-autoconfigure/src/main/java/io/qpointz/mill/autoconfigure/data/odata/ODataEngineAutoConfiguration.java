package io.qpointz.mill.autoconfigure.data.odata;

import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration;
import io.qpointz.mill.data.backend.SubstraitPlanExecutor;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.data.backend.calcite.CalciteRelBuilderFactory;
import io.qpointz.mill.data.backend.calcite.CalciteRelToSubstraitPlanConverter;
import io.qpointz.mill.data.backend.calcite.RelBuilderFactory;
import io.qpointz.mill.data.backend.calcite.RelPlanDispatcherBridge;
import io.qpointz.mill.data.backend.calcite.RelToSubstraitPlanConverter;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.odata.edm.EntityDataModelFactory;
import io.qpointz.mill.data.odata.exec.ODataQueryExecutor;
import io.qpointz.mill.data.odata.expr.ODataExpressionToRex;
import io.qpointz.mill.data.odata.plan.ODataRelComposer;
import io.qpointz.mill.data.odata.resolve.EdmPropertyResolver;
import io.qpointz.mill.data.schema.SchemaFacetService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers Spring-free OData query engine beans when the data plane and Calcite context are available.
 */
@AutoConfiguration(after = {
        BackendAutoConfiguration.class,
        SchemaFacetServiceAutoConfiguration.class
})
@ConditionalOnBean({
        DataOperationDispatcher.class,
        CalciteContextFactory.class,
        SchemaFacetService.class,
        SubstraitDispatcher.class
})
public class ODataEngineAutoConfiguration {

    /**
     * @return RWS EDM builder backed by merged schema facets.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityDataModelFactory odataEntityDataModelFactory(SchemaFacetService schemaFacetService) {
        return new EntityDataModelFactory(schemaFacetService);
    }

    /**
     * @return EDM property to physical column resolver.
     */
    @Bean
    @ConditionalOnMissingBean
    public EdmPropertyResolver odataEdmPropertyResolver(SchemaFacetService schemaFacetService) {
        return new EdmPropertyResolver(schemaFacetService, null);
    }

    /**
     * @return catalog-bound relational builder factory for OData composers.
     */
    @Bean
    @ConditionalOnMissingBean
    public RelBuilderFactory odataRelBuilderFactory(CalciteContextFactory calciteContextFactory) {
        return new CalciteRelBuilderFactory(calciteContextFactory);
    }

    /**
     * @return RelRoot to Substrait plan converter.
     */
    @Bean
    @ConditionalOnMissingBean
    public RelToSubstraitPlanConverter odataRelToSubstraitPlanConverter(SubstraitDispatcher substraitDispatcher) {
        return new CalciteRelToSubstraitPlanConverter(substraitDispatcher);
    }

    /**
     * @return Substrait plan executor on the shared dispatcher path.
     */
    @Bean
    @ConditionalOnMissingBean
    public SubstraitPlanExecutor odataSubstraitPlanExecutor(
            DataOperationDispatcher dispatcher,
            SubstraitDispatcher substraitDispatcher) {
        return new SubstraitPlanExecutor(dispatcher, substraitDispatcher);
    }

    /**
     * @return composed Rel plan execution bridge.
     */
    @Bean
    @ConditionalOnMissingBean
    public RelPlanDispatcherBridge odataRelPlanDispatcherBridge(
            RelToSubstraitPlanConverter converter,
            SubstraitPlanExecutor executor) {
        return new RelPlanDispatcherBridge(converter, executor);
    }

    /**
     * @return OData filter AST to RexNode translator.
     */
    @Bean
    @ConditionalOnMissingBean
    public ODataExpressionToRex odataExpressionToRex(EdmPropertyResolver propertyResolver) {
        return new ODataExpressionToRex(propertyResolver);
    }

    /**
     * @return OData URI options to RelRoot composer.
     */
    @Bean
    @ConditionalOnMissingBean
    public ODataRelComposer odataRelComposer(
            RelBuilderFactory relBuilderFactory,
            EdmPropertyResolver propertyResolver,
            ODataExpressionToRex expressionToRex) {
        return new ODataRelComposer(relBuilderFactory, propertyResolver, expressionToRex);
    }

    /**
     * @return OData query orchestration entry point.
     */
    @Bean
    @ConditionalOnMissingBean
    public ODataQueryExecutor odataQueryExecutor(
            ODataRelComposer relComposer,
            RelPlanDispatcherBridge bridge) {
        return new ODataQueryExecutor(relComposer, bridge);
    }
}
