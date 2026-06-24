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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

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
     * Typical browser {@code Accept} header (no {@code application/json}). RWS Atom renderer used to win
     * content negotiation and fail on Mill's pre-serialized {@code RAW_JSON} feeds.
     */
    private static final String BROWSER_ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8";

    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();

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
        registry.add("mill.metadata.seed.resources[1]", () -> "file:" + skymillDir + "/skymill-canonical.yaml");
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
        assertThat(body).contains("EntitySet");
        assertThat(body).contains("Name=\"cities\"");
        assertThat(body).contains("NavigationProperty");
        assertThat(body).contains("Name=\"origin_segments\"");
        assertThat(body).contains("Core.Description");
        assertThat(body).contains("Airport cities served by the airline.");
    }

    @Test
    void shouldExpandNavigationFromRelationMetadata() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities")
                                .param("$expand", "origin_segments")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = parseJson(body);
        JsonNode value = root.get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).isNotEmpty();
        assertThat(value.get(0).has("origin")).isTrue();
        assertThat(value.get(0).has("destination")).isTrue();
    }

    @Test
    void shouldReadEntitySetWithoutExplicitAcceptHeader() throws Exception {
        String body = performEntityGet(get("/services/odata/skymill.svc/cities"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertODataJsonFeed(body, "cities");
    }

    @Test
    void shouldReadEntitySet_whenBrowserLikeAcceptHeader() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities").accept(BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertODataJsonFeed(body, "cities");
        assertThat(body).doesNotContain("Edm.String");
    }

    @Test
    void shouldReadEntitySetWithDollarFormatJson_whenNoAcceptHeader() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities").param("$format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertODataJsonFeed(body, "cities");
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

        assertODataJsonFeed(body, "cities");
    }

    @Test
    void shouldFilterEntitySetWithOrOfEquals() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/bookings")
                                .param("$filter", "seat_number eq 'AA5' or seat_number eq 'AA8'")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFilteredBookingsBySeat(body, "AA5", "AA8");
    }

    @Test
    void shouldFilterEntitySetWithOrOfEquals_whenBrowserLikeAcceptHeader() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/bookings")
                                .param("$filter", "seat_number eq 'AA5' or seat_number eq 'AA8'")
                                .accept(BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFilteredBookingsBySeat(body, "AA5", "AA8");
    }

    @Test
    void shouldFilterEntitySetWithOrOfEquals_withoutAcceptHeader() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/bookings")
                                .param("$filter", "seat_number eq 'AA5' or seat_number eq 'AA8'"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFilteredBookingsBySeat(body, "AA5", "AA8");
    }

    @Test
    void shouldFilterEntitySet() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities")
                                .param("$filter", "id eq 138148")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode value = parseJson(body).get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).get("city").asString()).isEqualTo("Stevenbury");
    }

    @Test
    void shouldFilterDateColumnWithCalendarDateLiteral() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cargo_flights")
                                .param("$filter", "departure_date ge 2026-03-30")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode value = parseJson(body).get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).isNotEmpty();
    }

    @Test
    void shouldFilterDateColumnWithPowerBiDateTimeOffsetLiteral() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cargo_flights")
                                .param("$filter", "departure_date ge 2026-03-30T22:00:00.000Z")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode value = parseJson(body).get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).isNotEmpty();
    }

    @Test
    void shouldFilterDateColumnWithQuotedDateTimeOffsetLiteral() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cargo_flights")
                                .param("$filter", "departure_date ge datetimeoffset'2026-03-30T22:00:00.000Z'")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(parseJson(body).get("value")).isNotEmpty();
    }

    @Test
    void shouldFilterEntitySet_whenBrowserLikeAcceptHeader() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities")
                                .param("$filter", "id eq 138148")
                                .accept(BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode value = parseJson(body).get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).get("city").asString()).isEqualTo("Stevenbury");
    }

    @Test
    void shouldReturnEmptyValueArray_whenFilterMatchesNoRows() throws Exception {
        String body = performEntityGet(
                        get("/services/odata/skymill.svc/cities")
                                .param("$filter", "id eq 201")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = parseJson(body);
        assertThat(root.get("@odata.context").asString()).contains("cities");
        JsonNode value = root.get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).isEmpty();
    }

    @Test
    void shouldReturnMetadataDocument_whenBrowserLikeAcceptHeader() throws Exception {
        String body = mockMvc.perform(
                        get("/services/odata/skymill.svc/$metadata")
                                .accept(BROWSER_ACCEPT)
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("EntitySet");
        assertThat(body).contains("Name=\"cities\"");
    }

    @Test
    void shouldReturnNotFoundForUnknownSchema() throws Exception {
        mockMvc.perform(
                        get("/services/odata/unknown_schema.svc/$metadata")
                                .accept(MediaType.APPLICATION_XML)
                                .with(user("alice")))
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions performEntityGet(
            MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(user("alice")));
    }

    private static void assertODataJsonFeed(String body, String entitySetName) throws Exception {
        JsonNode root = parseJson(body);
        assertThat(root.get("@odata.context").asString())
                .contains("/services/odata/skymill.svc/$metadata#" + entitySetName);
        JsonNode value = root.get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).isNotEmpty();
    }

    private static void assertFilteredBookingsBySeat(String body, String... seatNumbers) throws Exception {
        JsonNode value = parseJson(body).get("value");
        assertThat(value.isArray()).isTrue();
        assertThat(value).hasSize(seatNumbers.length);
        List<String> seats = new ArrayList<>();
        for (JsonNode row : value) {
            seats.add(row.get("seat_number").asString());
        }
        assertThat(seats).containsExactlyInAnyOrder(seatNumbers);
    }

    private static JsonNode parseJson(String body) throws Exception {
        return JSON.readTree(body);
    }
}
