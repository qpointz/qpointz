package io.qpointz.rapids.config;

import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class RapidsConfigSourceProvider implements ConfigSourceProvider {
    @SneakyThrows
    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        var sources = new ArrayList<ConfigSource>();

        sources.add(new YamlConfigSource(Objects.requireNonNull(RapidsConfigSourceProvider.class.getResource("/application.yaml")), 120));

        probeFile(sources, Paths.get("./application.yaml"), 120);
        probeFile(sources, Paths.get("./etc/application.yaml"), 119);


        var additionalProbe = System.getenv("RAPIDS_APPLICATION_CONFIG_ADDITIONAL_DIR");
        if (additionalProbe!=null) {
            var probeDir = Paths.get(additionalProbe).normalize().toAbsolutePath();
            log.info("Probing additional configuration directory {} ({})",additionalProbe, probeDir.toString());
            if (!Files.exists(probeDir)) {
                log.warn("Additional probe dir: {} doesn't exists", additionalProbe);
            }
            probeFile(sources, Paths.get(additionalProbe, "application.yaml"), 130);
            probeFile(sources, Paths.get(additionalProbe, "etc",  "application.yaml"), 129);
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
