package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.data.backend.rewriters.TableFacetFactory;
import io.qpointz.mill.data.backend.rewriters.TableFacetFactoryImpl;
import io.qpointz.mill.data.backend.rewriters.TableFacetPlanRewriter;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authorization.policy.*;
import io.qpointz.mill.security.authorization.policy.repositories.PolicyActionDescriptorRepository;
import io.qpointz.mill.security.configuration.PolicyActionsConfiguration;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.PlanRewriter;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.dispatchers.SecurityDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConditionalOnSecurity
public class DefaultFilterChainConfiguration {


    @Bean
    public PolicyRepository policyRepository(PolicyActionsConfiguration configuration) {
        final Collection<PolicyActionDescriptor> actions = null == configuration ||
                null == configuration.actions
                ? List.of()
                : List.of(configuration.actions);
        return new PolicyActionDescriptorRepository(actions);
    }

    @Bean
    public PolicyEvaluator policyEvaluator(PolicyRepository policyRepository, PolicySelector policySelector) {
        return new PolicyEvaluatorImpl(policyRepository, policySelector);
    }

    @Bean
    PolicySelector policySelector(SecurityDispatcher securityDispatcher,
                                  PolicyConfiguration policyConfiguration) {
       val mappings = null == policyConfiguration ||
                null == policyConfiguration.getSelector() ||
                null == policyConfiguration.getSelector().grantedAuthority() ||
                null == policyConfiguration.getSelector().grantedAuthority().remap()
                ? Map.<String,String>of()
                : policyConfiguration.getSelector().grantedAuthority().remap();
        return new GrantedAuthoritiesPolicySelector(securityDispatcher, mappings);
    }

    @Bean
    public TableFacetFactory tableFacetFactory(PolicyEvaluator policyEvaluator,
                                               SecurityDispatcher securityDispatcher,
                                               SchemaProvider schemaProvider,
                                               SqlProvider sqlProvider,
                                               SubstraitDispatcher substraitDispatcher) {
        return new TableFacetFactoryImpl(policyEvaluator, securityDispatcher, schemaProvider, sqlProvider, substraitDispatcher);
    }

    @Bean
    public PlanRewriter tableFacetPlanRewriter(TableFacetFactory tableFacetFactory, SubstraitDispatcher substraitDispatcher) {
        return new TableFacetPlanRewriter(tableFacetFactory, substraitDispatcher);
    }

}
