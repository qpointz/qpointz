package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.data.backend.rewriters.TableFacetFactory;
import io.qpointz.mill.data.backend.rewriters.TableFacetFactoryImpl;
import io.qpointz.mill.data.backend.rewriters.TableFacetPlanRewriter;
import io.qpointz.mill.security.authorization.policy.*;
import io.qpointz.mill.security.authorization.policy.repositories.PolicyActionDescriptorRepository;
import io.qpointz.mill.security.configuration.PolicyAuthorizationConfiguration;
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

/**
 * Auto-configuration for the default security filter chain on the data backend.
 *
 * <p>Registers the {@link PolicyRepository}, {@link PolicyEvaluator},
 * {@link PolicySelector}, and {@link TableFacetFactory} beans that together enforce
 * policy-based row/column filtering on query results. This configuration is only
 * active when {@code mill.security.enable=true}.
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnSecurity
public class DefaultFilterChainConfiguration {

    /**
     * Creates the {@link PolicyRepository} populated from the inline policy action
     * descriptors declared in {@code mill.security.authorization.policy.actions[*]}.
     *
     * @param configuration the policy authorization configuration; may be {@code null}
     *                      if the property prefix is absent
     * @return the {@link PolicyRepository} backed by the configured action descriptors,
     *         or an empty repository if no actions are configured
     */
    @Bean
    public PolicyRepository policyRepository(PolicyAuthorizationConfiguration configuration) {
        final Collection<PolicyActionDescriptor> actions = null == configuration ||
                null == configuration.getActions()
                ? List.of()
                : List.of(configuration.getActions());
        return new PolicyActionDescriptorRepository(actions);
    }

    /**
     * Creates the {@link PolicyEvaluator} that combines the policy repository with a
     * subject selector to decide whether a given request is authorized.
     *
     * @param policyRepository the repository of policy action rules
     * @param policySelector   the selector that resolves the policy subject for a request
     * @return the {@link PolicyEvaluator}
     */
    @Bean
    public PolicyEvaluator policyEvaluator(PolicyRepository policyRepository, PolicySelector policySelector) {
        return new PolicyEvaluatorImpl(policyRepository, policySelector);
    }

    /**
     * Creates the {@link PolicySelector} that maps Spring Security granted authorities
     * to policy subjects using the remapping rules declared in
     * {@code mill.security.authorization.policy.selector.granted-authority.remap}.
     *
     * @param securityDispatcher  the dispatcher that resolves the current security context
     * @param policyConfiguration the policy authorization configuration; may be
     *                            {@code null} if the property prefix is absent
     * @return the {@link PolicySelector}
     */
    @Bean
    PolicySelector policySelector(SecurityDispatcher securityDispatcher,
                                  PolicyAuthorizationConfiguration policyConfiguration) {
        val mappings = null == policyConfiguration ||
                null == policyConfiguration.getSelector() ||
                null == policyConfiguration.getSelector().grantedAuthority() ||
                null == policyConfiguration.getSelector().grantedAuthority().remap()
                ? Map.<String, String>of()
                : policyConfiguration.getSelector().grantedAuthority().remap();
        return new GrantedAuthoritiesPolicySelector(securityDispatcher, mappings);
    }

    /**
     * Creates the {@link TableFacetFactory} that applies policy evaluation and security
     * filtering to individual table facets during query planning.
     *
     * @param policyEvaluator     the policy evaluator
     * @param securityDispatcher  the security context dispatcher
     * @param schemaProvider      the data backend schema provider
     * @param sqlProvider         the SQL provider
     * @param substraitDispatcher the Substrait plan dispatcher
     * @return the {@link TableFacetFactory}
     */
    @Bean
    public TableFacetFactory tableFacetFactory(PolicyEvaluator policyEvaluator,
                                               SecurityDispatcher securityDispatcher,
                                               SchemaProvider schemaProvider,
                                               SqlProvider sqlProvider,
                                               SubstraitDispatcher substraitDispatcher) {
        return new TableFacetFactoryImpl(policyEvaluator, securityDispatcher, schemaProvider, sqlProvider, substraitDispatcher);
    }

    /**
     * Creates the {@link PlanRewriter} that intercepts Substrait query plans and injects
     * table-facet security filtering before dispatch.
     *
     * @param tableFacetFactory   the factory that produces per-table security facets
     * @param substraitDispatcher the Substrait plan dispatcher
     * @return the {@link PlanRewriter}
     */
    @Bean
    public PlanRewriter tableFacetPlanRewriter(TableFacetFactory tableFacetFactory,
                                               SubstraitDispatcher substraitDispatcher) {
        return new TableFacetPlanRewriter(tableFacetFactory, substraitDispatcher);
    }

}
