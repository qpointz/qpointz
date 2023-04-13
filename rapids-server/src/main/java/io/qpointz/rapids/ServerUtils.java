package io.qpointz.rapids;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServerUtils {
    public static void logConfig(Logger log, String prefix) {
        Config cfg1 = ConfigProvider.getConfig();
        var props1 = StreamSupport
                .stream(cfg1.getPropertyNames().spliterator(),false)
                .filter(x -> x.startsWith(prefix))
                .sorted()
                .collect(Collectors.toMap(k->k, k-> cfg1.getConfigValue(k)));
        for (var k : props1.entrySet()) {
            log.info("{} = {}", k.getKey(), k.getValue().getRawValue());
        }
    }
}
