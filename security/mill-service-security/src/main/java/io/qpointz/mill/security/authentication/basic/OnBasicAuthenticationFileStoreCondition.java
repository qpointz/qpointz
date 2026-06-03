package io.qpointz.mill.security.authentication.basic;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Active when basic auth is enabled and the configured store is a file/classpath resource path.
 */
public class OnBasicAuthenticationFileStoreCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        if (!BasicAuthenticationStoreSupport.isBasicEnabled(environment)) {
            return false;
        }
        return BasicAuthenticationStoreSupport.isFileResourceStore(
                BasicAuthenticationStoreSupport.effectiveStore(environment)
        );
    }
}
