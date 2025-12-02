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
 * Integration test for metadata auto-configuration.
 * Verifies that MetadataRepository and MetadataService beans are created when configuration is present.
 */
@SpringBootTest(classes = {
    MetadataCoreConfiguration.class,
    MetadataRepositoryAutoConfiguration.class,
    TestMetadataConfiguration.class
})
@ComponentScan(basePackages = "io.qpointz.mill.metadata")
@TestPropertySource(properties = {
    "mill.metadata.v2.storage.type=file",
    "mill.metadata.v2.file.path=classpath:metadata/moneta-test.yml"
})
class MetadataAutoConfigurationIT {

    @Autowired(required = false)
    private MetadataRepository metadataRepository;

    @Autowired(required = false)
    private MetadataService metadataService;

    @Test
    void shouldCreateMetadataRepositoryBean() {
        assertThat(metadataRepository)
            .isNotNull()
            .isInstanceOf(FileMetadataRepository.class);
    }

    @Test
    void shouldCreateMetadataServiceBean() {
        assertThat(metadataService)
            .isNotNull();
    }

    @Test
    void shouldLoadMetadataFromFile() {
        assertThat(metadataRepository).isNotNull();
        assertThat(metadataService).isNotNull();

        // Verify that metadata can be loaded
        var entities = metadataService.findAll();
        assertThat(entities).isNotEmpty();

        // Verify that we can find a specific entity
        var entity = metadataService.findById("moneta.clients");
        assertThat(entity).isPresent();
        assertThat(entity.get().getType()).isNotNull();
    }
}

