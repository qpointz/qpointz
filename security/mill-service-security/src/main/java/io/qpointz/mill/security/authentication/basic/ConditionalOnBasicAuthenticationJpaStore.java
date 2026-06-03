package io.qpointz.mill.security.authentication.basic;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Matches when {@code mill.security.authentication.basic.enable=true} and
 * {@code mill.security.authentication.basic.store=jpa}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBasicAuthenticationJpaStoreCondition.class)
public @interface ConditionalOnBasicAuthenticationJpaStore {
}
