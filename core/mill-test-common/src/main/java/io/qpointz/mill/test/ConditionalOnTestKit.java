package io.qpointz.mill.test;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnTestKitCondition.class)
public @interface ConditionalOnTestKit {
}
