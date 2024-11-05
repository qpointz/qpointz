package io.qpointz.mill.test;

import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OnTestKitCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        val props = getByPrefix(context.getEnvironment(), "mill.security");
        return props.size()>0;
    }

    private Map<String, Object> getByPrefix(Environment env, String prefix) {
        if (!(env instanceof ConfigurableEnvironment)) {
            return Map.of();
        }

        val map = new HashMap<String, Object>();

        ((ConfigurableEnvironment)env).getPropertySources().stream()
                .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .map(propertySource -> ((EnumerablePropertySource)propertySource))
                .flatMap(k-> Arrays.stream(k
                        .getPropertyNames())
                        .filter(name -> name.startsWith(prefix))
                        .map(z-> Map.entry(z, k.getProperty(z))))
                .forEach(k-> map.put(k.getKey(), k.getValue()));
        return map;
    }
}
