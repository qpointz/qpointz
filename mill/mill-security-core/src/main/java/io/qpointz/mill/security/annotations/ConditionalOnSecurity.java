package io.qpointz.mill.security.annotations;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSecurityEnabledCondition.class)
public @interface ConditionalOnSecurity {

    boolean value() default true;

}
