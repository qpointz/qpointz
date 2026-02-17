package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.repository.MetadataRepository;
import io.qpointz.mill.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify that auto-configuration is disabled when property is missing.
 */
@SpringBootTest(classes = {
    MetadataCoreConfiguration.class,
    MetadataRepositoryAutoConfiguration.class
})
@TestPropertySource(properties = {
    "mill.metadata.v2.storage.type=none"
})
class MetadataAutoConfigurationDisabledIT {

    @Autowired(required = false)
    private MetadataRepository metadataRepository;

    @Autowired(required = false)
    private MetadataService metadataService;

    @Test
    void shouldNotCreateMetadataRepositoryWhenDisabled() {
        // When storage.type is not "file", the auto-configuration should not create the bean
        assertThat(metadataRepository).isNull();
    }

    @Test
    void shouldNotCreateMetadataServiceWhenRepositoryMissing() {
        // MetadataService is conditional on MetadataRepository, so it should also be null
        assertThat(metadataService).isNull();
    }
}

