package io.qpointz.mill.service.annotations;

import io.qpointz.mill.utils.SpringUtils;
import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnServiceEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        val mayBeAnnotation = metadata.getAnnotations()
                .stream(ConditionalOnService.class.getName())
                .filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
                .map(MergedAnnotation::asAnnotationAttributes)
                .findAny();

        val expected = mayBeAnnotation
                .map(k-> k.getBoolean("enabled"))
                .orElse(true);

        val serviceName = mayBeAnnotation
                .map(k-> k.getString("value"))
                .orElse("no-such-service");

        val mayBeGroupName = mayBeAnnotation
                .map(k -> k.getString("group"))
                .orElse("");

        val groupName = mayBeGroupName != null && !mayBeGroupName.isEmpty()
                ? mayBeGroupName + ".services"
                : "services";

        val serviceConfigGroup = String.format("mill.%s.%s", groupName, serviceName);
        val serviceEnableKey = String.format("%s.enable", serviceConfigGroup);

        Environment environment = context.getEnvironment();

        val hasServiceGroup = SpringUtils.hasPropertiesGroup(environment, serviceConfigGroup);
        if (!hasServiceGroup) {
            return !expected;
        }

        val hasProperty = environment.containsProperty(serviceEnableKey);
        if (!hasProperty) {
            return expected;
        }
        return expected == Boolean.TRUE.equals(environment.getProperty(serviceEnableKey, boolean.class));
    }
}
