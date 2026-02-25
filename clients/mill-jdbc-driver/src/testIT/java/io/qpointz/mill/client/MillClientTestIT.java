package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.TestITProfile;
import io.qpointz.mill.proto.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.qpointz.mill.TestITProfile.Protocol.HTTP;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MillClientTestIT  {
    private static final Set<String> EXPECTED_TABLES = Set.of(
            "cities",
            "segments",
            "aircraft",
            "aircraft_types",
            "passenger",
            "flight_instances",
            "cargo_flights",
            "bookings",
            "loyalty_earnings",
            "delays",
            "cancellations",
            "ticket_prices",
            "ratings",
            "countries",
            "cargo_clients",
            "cargo_types",
            "cargo_shipments"
    );

    private static final List<String> CITIES_FIELDS = List.of(
            "id",
            "state",
            "city",
            "population",
            "airport",
            "airport_iata"
    );

    public MillClientConfiguration.MillClientConfigurationBuilder clientConfig(TestITProfile profile) {
        val config = MillClientConfiguration.builder();

        config.host(profile.host())
              .port(profile.port());

        switch (profile.protocol()) {
            case GRPC -> config.protocol(profile.tls() ? "grpcs" : "grpc");
            case HTTP -> config.protocol(profile.tls() ? "https" : "http");
        }

        if (profile.protocol() == HTTP) {
            config.path(profile.basePath());
        }

        switch (profile.auth()) {
            case BASIC -> config.basicCredentials(profile.username(), profile.password());
            case BEARER -> config.bearerToken(profile.token());
            case NO_AUTH -> {
            }
        }

        if (profile.tls()) {
            if (profile.tlsCa() != null && !profile.tlsCa().isBlank()) {
                config.tlsTrustRootCert(profile.tlsCa());
            }
            if (profile.tlsCert() != null && !profile.tlsCert().isBlank()) {
                config.tlsKeyCertChain(profile.tlsCert());
            }
            if (profile.tlsKey() != null && !profile.tlsKey().isBlank()) {
                config.tlsKeyPrivateKey(profile.tlsKey());
            }
        }

        return config;
    }

    public MillClient client(TestITProfile profile) {
        log.info("Integration parameters: {}", profile.debugSummary());
        log.info("JDBC URL (reference): {}", profile.jdbcUrl());
        log.info("Connection properties: {}", profile.maskedConnectionProperties());
        return MillClient.fromConfig(
                clientConfig(profile).build());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies client handshake succeeds and reports expected protocol version.
    void handshake(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).handshake(
                HandshakeRequest.newBuilder().build());
        assertNotNull(resp);
        assertTrue(resp.getVersion() == ProtocolVersion.V1_0);
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies listSchemas contains the configured integration schema.
    void listSchemas(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).listSchemas(
                ListSchemasRequest.newBuilder().build());
        assertNotNull(resp);
        assertFalse(resp.getSchemasList().isEmpty());
        assertTrue(
                resp.getSchemasList().stream().anyMatch(s -> s.equalsIgnoreCase(profile.schemaName())),
                "Expected schema " + profile.schemaName() + " in " + resp.getSchemasList()
        );
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies getSchema returns expected table/field model for skymill.
    void getSchema(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).getSchema(
                GetSchemaRequest.newBuilder()
                        .setSchemaName(profile.schemaName())
                        .build());
        assertNotNull(resp);
        assertFalse(resp.getSchema().getTablesList().isEmpty());

        val actualNames = resp.getSchema().getTablesList().stream()
                .map(t -> t.getName().toLowerCase())
                .collect(Collectors.toSet());
        assertTrue(actualNames.containsAll(EXPECTED_TABLES), "Missing tables from schema: " + EXPECTED_TABLES);

        val cities = resp.getSchema().getTablesList().stream()
                .filter(t -> t.getName().equalsIgnoreCase("cities"))
                .findFirst()
                .orElse(null);
        assertNotNull(cities, "cities table must exist");
        val cityFields = cities.getFieldsList().stream().map(f -> f.getName().toLowerCase()).collect(Collectors.toList());
        assertEquals(CITIES_FIELDS.size(), cityFields.size(), "Unexpected cities field count: " + cityFields);
        assertTrue(cityFields.containsAll(CITIES_FIELDS), "Missing cities fields: " + CITIES_FIELDS + " actual: " + cityFields);
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies sync execQuery returns schema-bearing blocks.
    void execQuery(TestITProfile profile) throws MillCodeException {
        val sql = "SELECT * FROM `" + profile.schemaName() + "`.`cities` LIMIT 10";
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql(sql)
                        .build())
                .build();

        val result = client(profile).execQuery(request);
        assertNotNull(result);
        assertTrue(result.hasSchema());
        assertTrue(result.getSchema().getFieldsList().size() > 0);
        val vectors = result.getVectorBlocks();
        assertTrue(vectors.hasNext());
        val vector = vectors.next();
        assertTrue(vector.hasSchema());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies async execQuery returns schema-bearing blocks.
    void execQueryAsync(TestITProfile profile) throws Exception {
        val sql = "SELECT * FROM `" + profile.schemaName() + "`.`cities` LIMIT 10";
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql(sql)
                        .build())
                .build();

        val result = client(profile).execQueryAsync(request).get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.hasSchema());
        assertTrue(result.getSchema().getFieldsList().size() > 0);

        val vectors = result.getVectorBlocks();
        assertTrue(vectors.hasNext());
        val vector = vectors.next();
        assertTrue(vector.hasSchema());
    }

}