package io.qpointz.delta.lineage.reports;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.ApiKeyTokenProvider;
import io.openlineage.client.transports.ConsoleTransport;
import io.openlineage.client.transports.HttpConfig;
import io.openlineage.client.transports.HttpTransport;
import io.qpointz.delta.lineage.model.LineageRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@AllArgsConstructor
public class OpenLineageReport implements Report {

    @Getter
    private final LineageRepository repository;

    public void report() {
        ApiKeyTokenProvider apiKeyTokenProvider = new ApiKeyTokenProvider();
        apiKeyTokenProvider.setApiKey("f38d2189-c603-4b46-bdea-e573a3b5a7d5");

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setUrl(URI.create("http://localhost:5000"));
        httpConfig.setAuth(apiKeyTokenProvider);

        val client = OpenLineageClient.builder()
                .transport(new HttpTransport(httpConfig))
                .build();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        URI producer = URI.create("producer");
        OpenLineage ol = new OpenLineage(producer);
        UUID runId = UUID.randomUUID();

        val root = this.repository.getRootSchema();
        val tables = this.repository.getRootSchema().getTableNames();

        for (val t : tables) {

            val table = root.getTable(t);

            val fields = table.getRowType(this.repository.getTypeFactory()).getFieldList().stream()
                    .map(k-> ol.newSchemaDatasetFacetFields(k.getName(),
                            k.getType().getFullTypeString(), "", List.of()))
                    .collect(Collectors.toList());

            val dataset = ol.newSchemaDatasetFacetBuilder()
                    .fields(fields)
                    .build();

            val facets = ol.newDatasetFacetsBuilder()
                    .schema(dataset)
                    .build();

            val staticDatasez = ol.newStaticDatasetBuilder()
                    .name(t)
                    .namespace("test")
                    .facets(facets)
                    .build();

            val dataSetEvent = ol.newDatasetEvent(now, staticDatasez);
            client.emit(dataSetEvent);

        }

    }

}
