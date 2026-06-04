package io.qpointz.mill.security.authentication.basic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Active when basic auth is enabled and {@code store} is {@code jpa}.
 */
@Slf4j
public class OnBasicAuthenticationJpaStoreCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        boolean basicEnabled = BasicAuthenticationStoreSupport.isBasicEnabled(environment);
        String store = BasicAuthenticationStoreSupport.effectiveStore(environment);
        boolean active = basicEnabled && BasicAuthenticationStoreSupport.isJpaStore(store);
        log.info(
                "Basic auth JPA-store condition: basic.enable={}, store='{}', active={}",
                basicEnabled,
                store,
                active
        );
        return active;
    }
}
