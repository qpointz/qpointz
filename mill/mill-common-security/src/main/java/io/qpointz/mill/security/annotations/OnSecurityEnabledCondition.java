package io.qpointz.mill.security.annotations;

import io.qpointz.mill.utils.SpringUtils;
import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnSecurityEnabledCondition implements Condition {

    private static final String SECURITY_ENABLE_KEY = "mill.security.enable";

    private static final String SECURITY_CONFIG_GROUP = "mill.security";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        val expected = metadata.getAnnotations()
                .stream(ConditionalOnSecurity.class.getName())
                .filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
                .map(MergedAnnotation::asAnnotationAttributes)
                .findAny()
                .map(annotationAttributes -> annotationAttributes.getBoolean("value"))
                .orElse(true);

        Environment environment = context.getEnvironment();

        val hasProperty = environment.containsProperty(SECURITY_ENABLE_KEY);

        if (hasProperty) {
            return expected == environment.getProperty(SECURITY_ENABLE_KEY, Boolean.class);
        }

        return expected == SpringUtils.hasPropertiesGroup(environment, SECURITY_CONFIG_GROUP);
    }
}
