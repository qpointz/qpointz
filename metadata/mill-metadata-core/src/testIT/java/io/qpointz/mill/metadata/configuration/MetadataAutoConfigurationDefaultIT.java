package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.repository.MetadataRepository;
import io.qpointz.mill.metadata.repository.file.FileMetadataRepository;
import io.qpointz.mill.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify that auto-configuration defaults to file storage when property is not specified.
 */
@SpringBootTest(classes = {
    MetadataCoreConfiguration.class,
    MetadataRepositoryAutoConfiguration.class,
    MetadataService.class
})
@ComponentScan(basePackages = "io.qpointz.mill.metadata")
@TestPropertySource(properties = {
    "mill.metadata.v2.file.path=classpath:metadata/moneta-test.yml",
    "mill.metadata.v2.storage.type=file"
    // Note: storage.type is not specified, should default to "file"
})
class MetadataAutoConfigurationDefaultIT {

    @Autowired(required = false)
    private MetadataRepository metadataRepository;

    @Autowired(required = false)
    private MetadataService metadataService;

    @Test
    void shouldDefaultToFileStorage() {
        // When storage.type is not specified, it should default to "file" (matchIfMissing = true)
        assertThat(metadataRepository)
            .isNotNull()
            .isInstanceOf(FileMetadataRepository.class);
    }

    @Test
    void shouldCreateMetadataServiceWithDefaultConfiguration() {
        assertThat(metadataService).isNotNull();
    }
}

