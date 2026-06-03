package io.qpointz.mill.security.authentication.basic;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Active when basic auth is enabled and {@code store} is {@code jpa}.
 */
public class OnBasicAuthenticationJpaStoreCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        if (!BasicAuthenticationStoreSupport.isBasicEnabled(environment)) {
            return false;
        }
        return BasicAuthenticationStoreSupport.isJpaStore(
                BasicAuthenticationStoreSupport.effectiveStore(environment)
        );
    }
}
