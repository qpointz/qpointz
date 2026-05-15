package io.qpointz.mill.autoconfigure.data.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.qpointz.mill.data.backend.resource.ResourceLocations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringResourceLoaderBackendResourceLoaderTest {

    @Test
    void shouldOpenClasspathResource() throws Exception {
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        try (var in = loader.open("classpath:io/qpointz/mill/autoconfigure/data/resource/test-descriptor.txt")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8).trim()).isEqualTo("ok");
        }
    }

    @Test
    void shouldOpenFileUri(@TempDir Path dir) throws Exception {
        var f = dir.resolve("d.txt");
        Files.writeString(f, "x");
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        try (var in = loader.open(f.toUri().toString())) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("x");
        }
    }

    @Test
    void shouldOpenBarePathAsFile(@TempDir Path dir) throws Exception {
        var f = dir.resolve("bare.yml");
        Files.writeString(f, "a: 1");
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        try (var in = loader.open(f.toString())) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("a: 1");
        }
    }

    @Test
    void shouldFailOnMissingClasspathResource() {
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        assertThatThrownBy(() -> loader.open("classpath:io/qpointz/mill/autoconfigure/data/resource/does-not-exist-12345.txt"))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void shouldFailOnEmptyLocation() {
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        assertThatThrownBy(() -> loader.open("  "))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void shouldStripQueryFromDisplayLocation() {
        var loader = new SpringResourceLoaderBackendResourceLoader(new DefaultResourceLoader());
        assertThat(loader.displayLocation("https://example.com/path?sig=secret&x=1"))
                .doesNotContain("sig")
                .doesNotContain("?");
    }

    @Test
    void shouldRecognizeUriScheme() {
        assertThat(ResourceLocations.hasUriScheme("classpath:a")).isTrue();
        assertThat(ResourceLocations.hasUriScheme("/tmp/a")).isFalse();
        assertThat(ResourceLocations.hasUriScheme("C:\\a")).isFalse();
    }
}
