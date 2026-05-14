package io.qpointz.mill.cloud.azure.blob

import com.azure.storage.blob.BlobServiceClientBuilder
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
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.StreamSupport

/**
 * Full-flow integration test for Azure ADLS (Azurite): emulator → upload
 * Skymill datasets → blob source (list / stream / seek) → flow backend SQL queries.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdlsFlowBackendIT {

    companion object {
        private const val BLOB_PORT = 10000
        private const val CONTAINER_NAME = "skymill"
        private const val ACCOUNT_NAME = "devstoreaccount1"
        private const val ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="

        @Container
        @JvmStatic
        val azurite: GenericContainer<*> = GenericContainer("mcr.microsoft.com/azure-storage/azurite")
            .withExposedPorts(BLOB_PORT)
            .withCommand("azurite-blob", "--blobHost", "0.0.0.0", "--blobPort", "$BLOB_PORT", "--skipApiVersionCheck")
    }

    private lateinit var parquetDescriptor: AdlsStorageDescriptor
    private lateinit var avroDescriptor: AdlsStorageDescriptor
    private lateinit var runner: FlowBackendContextRunner

    private fun connectionString(): String {
        val host = azurite.host
        val port = azurite.getMappedPort(BLOB_PORT)
        return "DefaultEndpointsProtocol=http;" +
                "AccountName=$ACCOUNT_NAME;" +
                "AccountKey=$ACCOUNT_KEY;" +
                "BlobEndpoint=http://$host:$port/$ACCOUNT_NAME"
    }

    @BeforeAll
    fun setUp() {
        val connStr = connectionString()

        val svc = BlobServiceClientBuilder()
            .connectionString(connStr)
            .buildClient()
        val container = svc.createBlobContainerIfNotExists(CONTAINER_NAME)

        val datasetsRoot = Path.of("../../../test/datasets/skymill")
        Files.walk(datasetsRoot.resolve("parquet"))
            .filter { Files.isRegularFile(it) }
            .forEach { file ->
                container.getBlobClient("parquet/${file.fileName}")
                    .uploadFromFile(file.toString(), true)
            }
        Files.walk(datasetsRoot.resolve("avro"))
            .filter { Files.isRegularFile(it) }
            .forEach { file ->
                container.getBlobClient("avro/${file.fileName}")
                    .uploadFromFile(file.toString(), true)
            }

        parquetDescriptor = AdlsStorageDescriptor(
            endpoint = "",
            container = CONTAINER_NAME,
            connectionString = connStr,
            prefix = "parquet/"
        )
        avroDescriptor = AdlsStorageDescriptor(
            endpoint = "",
            container = CONTAINER_NAME,
            connectionString = connStr,
            prefix = "avro/"
        )

        val sources = listOf(
            SourceDescriptor(
                name = "skymill_parquet",
                storage = parquetDescriptor,
                readers = listOf(ReaderDescriptor(type = "parquet", format = ParquetFormatDescriptor())),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = ".*?(?<table>[^/]+)\\.parquet$")
                )
            ),
            SourceDescriptor(
                name = "skymill_avro",
                storage = avroDescriptor,
                readers = listOf(ReaderDescriptor(type = "avro", format = AvroFormatDescriptor())),
                table = TableDescriptor(
                    mapping = RegexTableMappingDescriptor(pattern = ".*?(?<table>[^/]+)\\.avro$")
                )
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
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.contains("parquet") }
        }
    }

    @Test
    fun shouldListAvroBlobs() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(avroDescriptor).use { src ->
            val blobs = src.listBlobs().toList()
            assertThat(blobs).isNotEmpty
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.contains("avro") }
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
        val block = executeQuery(
            "SELECT * FROM `skymill_parquet`.`countries` WHERE `id` > 5 LIMIT 5"
        )
        assertThat(block.vectorSize).isGreaterThan(0)
        assertThat(block.vectorSize).isLessThanOrEqualTo(5)
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
}
