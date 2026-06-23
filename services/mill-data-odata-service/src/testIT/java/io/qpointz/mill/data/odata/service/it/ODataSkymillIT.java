package io.qpointz.mill.data.odata.service.it;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowDescriptorMetadataSourceAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.odata.ODataEngineAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.resource.BackendResourceLoaderAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.schema.LogicalLayoutMetadataSourceAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.schema.MetadataEntityUrnCodecAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.odata.service.config.ODataMvcAutoConfiguration;
import io.qpointz.mill.data.odata.service.config.ODataWebAutoConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration;
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Skymill-backed integration tests for {@code /services/odata/{schema}.svc/**} (RWS controller + Mill push-down).
 */
@SpringBootTest(classes = ODataSkymillIT.Config.class)
@AutoConfigureMockMvc
@ActiveProfiles("skymill")
class ODataSkymillIT {

    /**
     * Registers Skymill flow descriptor and file-backed metadata seed paths.
     *
     * @param registry Spring dynamic property registry
     */
    @DynamicPropertySource
    static void registerFlowDescriptor(DynamicPropertyRegistry registry) {
        var cwd = java.nio.file.Path.of("").toAbsolutePath().normalize();
        var localFlow = cwd.resolve("src/testIT/resources/flow-skymill-it.yaml");
        if (!java.nio.file.Files.isReadable(localFlow)) {
            throw new IllegalStateException("Skymill flow descriptor not found at " + localFlow);
        }
        registry.add("mill.data.backend.flow.sources[0]", localFlow::toString);

        var skymillDir = System.getProperty("skymill.datasets.dir");
        if (skymillDir == null || skymillDir.isBlank()) {
            throw new IllegalStateException("System property 'skymill.datasets.dir' not set");
        }
        registry.add("mill.metadata.repository.type", () -> "file");
        registry.add("mill.metadata.seed.resources[0]", () -> "classpath:metadata/platform-bootstrap.yaml");
        registry.add("mill.metadata.seed.resources[1]", () -> "file:" + skymillDir + "/skymill-meta-seed-canonical.yaml");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            MetadataCoreConfiguration.class,
            SqlAutoConfiguration.class,
            BackendAutoConfiguration.class,
            BackendResourceLoaderAutoConfiguration.class,
            FlowBackendAutoConfiguration.class,
            FlowDescriptorMetadataSourceAutoConfiguration.class,
            LogicalLayoutMetadataSourceAutoConfiguration.class,
            MetadataEntityUrnCodecAutoConfiguration.class,
            MetadataFileRepositoryAutoConfiguration.class,
            MetadataRepositoryAutoConfiguration.class,
            MetadataImportExportAutoConfiguration.class,
            MetadataEntityServiceAutoConfiguration.class,
            MetadataSeedAutoConfiguration.class,
            DefaultServiceConfiguration.class,
            SchemaFacetServiceAutoConfiguration.class,
            ODataEngineAutoConfiguration.class,
            ODataWebAutoConfiguration.class,
            ODataMvcAutoConfiguration.class,
    })
    static class Config {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListSchemaCatalog() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/schemas")
                                .accept(MediaType.APPLICATION_JSON)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("skymill");
        assertThat(body).contains("/services/odata/skymill.svc/");
    }

    @Test
    void shouldReturnMetadataDocument() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/skymill.svc/$metadata")
                                .accept(MediaType.APPLICATION_XML)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("EntityType");
        assertThat(body).contains("EntitySet Name=\"cities\"");
    }

    @Test
    void shouldReadEntitySet() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/skymill.svc/cities")
                                .accept(MediaType.APPLICATION_JSON)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("value");
    }

    @Test
    void shouldFilterEntitySetWithOrOfEquals() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/skymill.svc/bookings")
                                .param("$filter", "seat_number eq 'AA5' or seat_number eq 'AA8'")
                                .accept(MediaType.APPLICATION_JSON)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("value");
    }

    @Test
    void shouldFilterEntitySet() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/skymill.svc/cities")
                                .param("$filter", "id eq 1")
                                .accept(MediaType.APPLICATION_JSON)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("value");
    }

    @Test
    void shouldReturnNotFoundForUnknownSchema() throws Exception {
        mockMvc.perform(
                        get("/services/odata/unknown_schema.svc/$metadata")
                                .accept(MediaType.APPLICATION_XML)
                                .with(user("alice")))
                .andExpect(status().isNotFound());
    }
}
