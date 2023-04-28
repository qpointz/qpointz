package io.qpointz.rapids.server.worker.config;

import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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

        probeFile(sources, Paths.get("./etc/application.yaml"), 140);
        probeFile(sources, Paths.get("./application.yaml"), 145);

        var additionalProbe = System.getenv("RAPIDS_APPLICATION_CONFIG_ADDITIONAL_DIR");
        if (additionalProbe!=null) {
            log.info("Probing additional configuration directory {}", additionalProbe);
            probeFile(sources, Paths.get(additionalProbe, "etc",  "application.yaml"), 150);
            probeFile(sources, Paths.get(additionalProbe, "application.yaml"), 155);
        }

        return sources;
    }

    private void probeFile(ArrayList<ConfigSource> sources, Path path, int priority) throws IOException {
        var probePath = path.toAbsolutePath();
        log.debug("Probing config file {}", probePath.toAbsolutePath());
        if (Files.exists(probePath)) {
            log.info("Config file {} used with {} priority", probePath, priority);
            sources.add(new YamlConfigSource(probePath.toUri().toURL(), priority));
        }
    }
}
