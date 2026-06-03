package io.qpointz.mill.security.authentication.basic;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Matches when {@code mill.security.authentication.basic.enable=true} and {@code store} (or legacy
 * {@code file-store}) points at a YAML resource, not {@code jpa}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBasicAuthenticationFileStoreCondition.class)
public @interface ConditionalOnBasicAuthenticationFileStore {
}
