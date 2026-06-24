package io.qpointz.mill.data.odata.service.it;

import com.sdl.odata.api.parser.ODataParser;
import com.sdl.odata.api.processor.query.ODataQuery;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.service.ODataRequest;
import com.sdl.odata.api.service.ODataRequestContext;
import com.sdl.odata.processor.QueryModelBuilder;
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
import io.qpointz.mill.data.odata.exec.ODataQueryExecutor;
import io.qpointz.mill.data.odata.plan.ODataQueryOptions;
import io.qpointz.mill.data.odata.service.config.ODataMvcAutoConfiguration;
import io.qpointz.mill.data.odata.service.config.ODataWebAutoConfiguration;
import io.qpointz.mill.data.odata.service.edm.ODataEdmRegistryCache;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies RWS builds a filter-bearing {@link QueryOperation} tree and Mill extracts it.
 */
@SpringBootTest(classes = ODataQueryOperationTreeIT.Config.class)
@ActiveProfiles("skymill")
class ODataQueryOperationTreeIT {

    @DynamicPropertySource
    static void registerFlowDescriptor(DynamicPropertyRegistry registry) {
        var cwd = java.nio.file.Path.of("").toAbsolutePath().normalize();
        var localFlow = cwd.resolve("src/testIT/resources/flow-skymill-it.yaml");
        registry.add("mill.data.backend.flow.sources[0]", localFlow::toString);

        var skymillDir = System.getProperty("skymill.datasets.dir");
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
    private ODataParser oDataParser;

    @Autowired
    private ODataEdmRegistryCache edmRegistryCache;

    @Autowired
    private ODataQueryExecutor queryExecutor;

    @Test
    void shouldBuildOperationTreeWithFilterForCities() throws Exception {
        var edm = edmRegistryCache.registryFor("skymill").getEntityDataModel();
        var uri = oDataParser.parseUri(
                "http://localhost/services/odata/skymill.svc/cities?$filter=id%20eq%20138148",
                edm);
        var request = new ODataRequest.Builder()
                .setMethod(ODataRequest.Method.GET)
                .setUri("http://localhost/services/odata/skymill.svc/cities?$filter=id%20eq%20138148")
                .build();
        var context = new ODataRequestContext(request, uri, edm);
        ODataQuery query = new QueryModelBuilder(edm).build(context);
        QueryOperation operation = query.operation();

        assertThat(operation.toString()).contains("CriteriaFilterOperation");

        var options = ODataQueryOptions.Companion.from(operation);
        assertThat(options.getEntitySetName()).isEqualTo("cities");
        assertThat(options.getFilter()).isNotNull();
        assertThat(options.getExpands()).isEmpty();
        assertThat(options.getSelectDistinct()).isTrue();

        var rows = queryExecutor.executeToMaps(operation, "skymill", 1000);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("city")).isEqualTo("Stevenbury");
    }
}
