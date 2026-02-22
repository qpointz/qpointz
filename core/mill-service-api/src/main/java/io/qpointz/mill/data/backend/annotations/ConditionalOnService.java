package io.qpointz.mill.data.backend.annotations;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnServiceEnabledCondition.class)
public @interface ConditionalOnService {

    String value();

    boolean enabled() default true;

}
