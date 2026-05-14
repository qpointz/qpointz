package io.qpointz.mill.cloud.gcp.blob

import io.qpointz.mill.data.backend.flow.SourceDefinitionRepository
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.source.descriptor.ReaderDescriptor
import io.qpointz.mill.source.descriptor.RegexTableMappingDescriptor
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.descriptor.TableDescriptor
import io.qpointz.mill.source.factory.SourceMaterializer
import io.qpointz.mill.source.format.avro.AvroFormatDescriptor
import io.qpointz.mill.source.format.parquet.ParquetFormatDescriptor
import io.qpointz.mill.test.data.backend.FlowBackendContextRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.StreamSupport

/**
 * Full-flow integration test for GCS (fake-gcs-server): emulator → upload
 * Skymill datasets → blob source (list / stream / seek) → flow backend SQL queries.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GcsFlowBackendIT {

    companion object {
        private const val BUCKET = "skymill-test"
        private const val GCS_PORT = 4443

        @Container
        @JvmStatic
        val gcsContainer: GenericContainer<*> = GenericContainer("fsouza/fake-gcs-server:latest")
            .withExposedPorts(GCS_PORT)
            .withCommand("-scheme", "http", "-port", GCS_PORT.toString())
    }

    private lateinit var endpoint: String
    private lateinit var parquetDescriptor: GcsStorageDescriptor
    private lateinit var avroDescriptor: GcsStorageDescriptor
    private lateinit var runner: FlowBackendContextRunner

    @BeforeAll
    fun setUp() {
        endpoint = "http://${gcsContainer.host}:${gcsContainer.getMappedPort(GCS_PORT)}"
        updateExternalUrl(endpoint)
        createBucket(BUCKET)
        uploadDatasetFiles("parquet")
        uploadDatasetFiles("avro")

        parquetDescriptor = GcsStorageDescriptor(
            bucket = BUCKET, prefix = "parquet/", endpoint = endpoint
        )
        avroDescriptor = GcsStorageDescriptor(
            bucket = BUCKET, prefix = "avro/", endpoint = endpoint
        )

        val sources = listOf(
            SourceDescriptor(
                name = "skymill_parquet",
                storage = parquetDescriptor,
                readers = listOf(ReaderDescriptor(type = "parquet", format = ParquetFormatDescriptor())),
                table = TableDescriptor(mapping = RegexTableMappingDescriptor(pattern = "(?<table>[^/]+)\\.parquet"))
            ),
            SourceDescriptor(
                name = "skymill_avro",
                storage = avroDescriptor,
                readers = listOf(ReaderDescriptor(type = "avro", format = AvroFormatDescriptor())),
                table = TableDescriptor(mapping = RegexTableMappingDescriptor(pattern = "(?<table>[^/]+)\\.avro"))
            )
        )

        val repository = object : SourceDefinitionRepository {
            override fun getSourceDefinitions(): Iterable<SourceDescriptor> = sources
        }
        runner = FlowBackendContextRunner.flowContext(repository)
    }

    // ── blob source: list ──────────────────────────────────────────

    @Test
    fun shouldListParquetBlobs() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(parquetDescriptor).use { src ->
            val blobs = src.listBlobs().toList()
            assertThat(blobs).isNotEmpty
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.startsWith("gs://$BUCKET/parquet/") }
        }
    }

    @Test
    fun shouldListAvroBlobs() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(avroDescriptor).use { src ->
            val blobs = src.listBlobs().toList()
            assertThat(blobs).isNotEmpty
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.startsWith("gs://$BUCKET/avro/") }
        }
    }

    // ── blob source: streaming read ────────────────────────────────

    @Test
    fun shouldStreamReadParquetBlob() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(parquetDescriptor).use { src ->
            val blob = src.listBlobs().first()
            src.openInputStream(blob).use { stream ->
                val bytes = stream.readNBytes(4)
                assertThat(bytes).hasSize(4)
                assertThat(String(bytes)).isEqualTo("PAR1")
            }
        }
    }

    // ── blob source: seekable channel ──────────────────────────────

    @Test
    fun shouldSeekableReadParquetBlob() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(parquetDescriptor).use { src ->
            val blob = src.listBlobs().first()
            src.openSeekableChannel(blob).use { ch ->
                assertThat(ch.size()).isGreaterThan(0)
                val buf = ByteBuffer.allocate(4)
                ch.read(buf)
                buf.flip()
                assertThat(String(ByteArray(4).also { buf.get(it) })).isEqualTo("PAR1")

                ch.position(0)
                assertThat(ch.position()).isEqualTo(0)
            }
        }
    }

    // ── flow backend: schema listing ───────────────────────────────

    @Test
    fun shouldListSchemaNames() {
        val schemas = StreamSupport
            .stream(runner.schemaProvider.schemaNames.spliterator(), false)
            .toList()
        assertThat(schemas).contains("skymill_parquet", "skymill_avro")
    }

    @Test
    fun shouldListTablesInParquetSchema() {
        val schema = runner.schemaProvider.getSchema("skymill_parquet")
        val tableNames = schema.tablesList.map { it.name }
        assertThat(tableNames).contains("cities", "countries", "aircraft", "aircraft_types")
    }

    // ── flow backend: SQL queries ──────────────────────────────────

    @Test
    fun shouldQueryParquetCities() {
        val block = executeQuery("SELECT * FROM `skymill_parquet`.`cities` LIMIT 10")
        assertThat(block.vectorSize).isGreaterThan(0)
        assertThat(block.vectorSize).isLessThanOrEqualTo(10)
    }

    @Test
    fun shouldQueryAvroCountries() {
        val block = executeQuery("SELECT * FROM `skymill_avro`.`countries` LIMIT 10")
        assertThat(block.vectorSize).isGreaterThan(0)
        assertThat(block.vectorSize).isLessThanOrEqualTo(10)
    }

    @Test
    fun shouldQueryWithFilter() {
        val block = executeQuery("SELECT * FROM `skymill_parquet`.`countries` WHERE `id` > 5")
        assertThat(block.vectorSize).isGreaterThan(0)
    }

    // ── helpers ────────────────────────────────────────────────────

    private fun executeQuery(sql: String): io.qpointz.mill.proto.VectorBlock {
        val parseResult = runner.sqlProvider.parseSql(sql)
        assertThat(parseResult.isSuccess).isTrue()
        val result = runner.executionProvider.execute(
            parseResult.plan,
            QueryExecutionConfig.newBuilder().setFetchSize(100).build()
        )
        assertThat(result.hasNext()).isTrue()
        return result.next()
    }

    private fun uploadDatasetFiles(format: String) {
        val datasetsRoot = Path.of("../../../test/datasets/skymill/$format")
        Files.walk(datasetsRoot)
            .filter { Files.isRegularFile(it) }
            .forEach { file ->
                uploadObject(BUCKET, "$format/${file.fileName}", Files.readAllBytes(file))
            }
    }

    private fun updateExternalUrl(externalUrl: String) {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$externalUrl/_internal/config"))
            .PUT(HttpRequest.BodyPublishers.ofString("""{"externalUrl":"$externalUrl"}"""))
            .header("Content-Type", "application/json")
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun createBucket(bucket: String) {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$endpoint/storage/v1/b"))
            .POST(HttpRequest.BodyPublishers.ofString("""{"name":"$bucket"}"""))
            .header("Content-Type", "application/json")
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun uploadObject(bucket: String, name: String, content: ByteArray) {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$endpoint/upload/storage/v1/b/$bucket/o?uploadType=media&name=$name"))
            .POST(HttpRequest.BodyPublishers.ofByteArray(content))
            .header("Content-Type", "application/octet-stream")
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}
