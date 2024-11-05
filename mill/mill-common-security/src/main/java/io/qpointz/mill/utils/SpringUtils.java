package io.qpointz.mill.utils;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringUtils {

    private SpringUtils() {
        //only static methods can be used
    }

    public static Set<String> getPropertiesKeys(Environment env) {
        if (!(env instanceof ConfigurableEnvironment)) {
            return Set.of();
        }

        return ((ConfigurableEnvironment)env).getPropertySources().stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .map(EnumerablePropertySource.class::cast)
                .flatMap(k-> Arrays.stream(k.getPropertyNames()))
                .collect(Collectors.toSet());
    }

    public static Set<String> getPropertiesKeysByPrefix(Environment env, String prefix) {
        return SpringUtils.getPropertiesKeys(env)
                .stream()
                .filter(entry -> entry.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    public static boolean hasPropertiesGroup(Environment environment, String prefix) {
        return !getPropertiesKeysByPrefix(environment, prefix).isEmpty();
    }
}
