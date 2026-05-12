package io.qpointz.mill.data.query.it;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Skymill-backed integration tests for {@code /api/v1/query/**} (dispatcher + MVC).
 */
@SpringBootTest(classes = MillDataQuerySkymillIT.Config.class)
@AutoConfigureMockMvc
@ActiveProfiles("skymill")
class MillDataQuerySkymillIT {

    /**
     * Registers an absolute path to the Skymill flow descriptor on the test classpath.
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
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            SqlAutoConfiguration.class,
            BackendAutoConfiguration.class,
            FlowBackendAutoConfiguration.class,
            DefaultServiceConfiguration.class,
    })
    static class Config {
    }

    private static final String SCHEMA = "skymill";

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void shouldCreatePageBackwardStaleEpochAndDelete() throws Exception {
        String sql = "SELECT `id`, `city` FROM `" + SCHEMA + "`.`cities`";

        Map<String, Object> createBody = new LinkedHashMap<>();
        createBody.put("sql", sql);
        createBody.put("includeFirstPage", true);
        createBody.put("firstPageSize", 2);
        createBody.put("defaultFormat", "rows-objects");

        String createJson = mockMvc.perform(
                        post("/api/v1/query")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(createBody))
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = mapper.readTree(createJson);
        String executionId = created.get("executionId").asText();
        assertThat(executionId).isNotBlank();

        String metaJson = mockMvc.perform(get("/api/v1/query/{id}", executionId).with(user("alice")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode meta = mapper.readTree(metaJson);
        assertThat(meta.get("executionId").asText()).isEqualTo(executionId);
        assertThat(meta.get("epoch").asInt()).isZero();

        mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageSize", "2")
                                .with(user("alice")))
                .andExpect(status().isBadRequest());

        JsonNode first = created.get("firstPage");
        assertThat(first.get("pageIndex").asInt()).isZero();
        assertThat(first.get("rowCount").asInt()).isEqualTo(2);
        assertThat(first.get("hasPrevious").asBoolean()).isFalse();
        assertThat(first.get("data").isArray()).isTrue();
        JsonNode schema = first.get("schema");
        assertThat(schema.isArray()).isTrue();
        assertThat(schema).hasSize(2);
        assertThat(schema.get(0).get("name").asText()).isEqualTo("id");
        assertThat(schema.get(1).get("name").asText()).isEqualTo("city");
        assertThat(schema.get(0).has("type")).isTrue();
        assertThat(schema.get(0).has("idx")).isTrue();

        String rows1 = mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "1")
                                .param("pageSize", "2")
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode page1 = mapper.readTree(rows1);
        assertThat(page1.get("hasPrevious").asBoolean()).isTrue();

        String rows0 = mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "0")
                                .param("pageSize", "2")
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode page0 = mapper.readTree(rows0);
        assertThat(page0.get("hasNext").asBoolean()).isTrue();

        String compact = mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "0")
                                .param("pageSize", "2")
                                .param("format", "rows-compact-batch")
                                .with(user("alice")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode compactNode = mapper.readTree(compact);
        assertThat(compactNode.get("data").get("fields").isArray()).isTrue();
        assertThat(compactNode.get("data").get("rows").isArray()).isTrue();
        assertThat(compactNode.get("schema").isArray()).isTrue();
        assertThat(compactNode.get("schema")).hasSize(2);

        mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "0")
                                .param("pageSize", "2")
                                .param("format", "no-such-format")
                                .with(user("alice")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "0")
                                .param("pageSize", "2")
                                .header("Accept", "application/vnd.nope")
                                .with(user("alice")))
                .andExpect(status().isNotAcceptable());

        mockMvc.perform(
                        get("/api/v1/query/{id}", executionId)
                                .param("pageIndex", "0")
                                .param("pageSize", "2")
                                .param("epoch", "1")
                                .with(user("alice")))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/query/{id}", executionId).with(user("alice")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/query/{id}", executionId).with(user("alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn201WhenFirstPageExcluded() throws Exception {
        String sql = "SELECT `id` FROM `" + SCHEMA + "`.`cities` LIMIT 1";
        Map<String, Object> createBody = Map.of(
                "sql", sql,
                "includeFirstPage", false
        );
        mockMvc.perform(
                        post("/api/v1/query")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(createBody))
                                .with(user("bob")))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn422OnInvalidSql() throws Exception {
        Map<String, Object> createBody = Map.of(
                "sql", "SELECT * FROM `" + SCHEMA + "`.`not_a_table`",
                "includeFirstPage", false
        );
        mockMvc.perform(
                        post("/api/v1/query")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(createBody))
                                .with(user("bob")))
                .andExpect(status().isUnprocessableEntity());
    }
}
