package io.qpointz.rapids.server.worker.config;

import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class RapidsConfigSourceProvider implements ConfigSourceProvider {
    @SneakyThrows
    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        var sources = new ArrayList<ConfigSource>();

        sources.add(new YamlConfigSource(Objects.requireNonNull(RapidsConfigSourceProvider.class.getResource("/application.yaml")), 120));

        var etcApp = Paths.get("./etc/application.yaml").toAbsolutePath();
        log.debug("Probing config file {}", etcApp.toAbsolutePath());
        if (Files.exists(etcApp)) {
            log.info("Config file {} used with {} priority", etcApp, 140);
            sources.add(new YamlConfigSource(etcApp.toUri().toURL(), 140));
        }

        var app = Paths.get("./application.yaml").toAbsolutePath();
        log.debug("Probing configuration path {}", app.toAbsolutePath());
        if (Files.exists(app)) {
            log.info("Config file {} used with {} priority", app, 160);
            sources.add(new YamlConfigSource(app.toUri().toURL(), 160));
        }

        return sources;
    }
}
