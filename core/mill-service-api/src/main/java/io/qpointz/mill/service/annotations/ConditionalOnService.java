package io.qpointz.mill.service.annotations;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnServiceEnabledCondition.class)
public @interface ConditionalOnService {

    String value();

    String group() default "";

    boolean enabled() default true;

}
