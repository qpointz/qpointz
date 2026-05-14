package io.qpointz.mill.cloud.aws.blob

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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.StreamSupport

/**
 * Full-flow integration test for S3 (MinIO): emulator → upload Skymill
 * datasets → blob source (list / stream / seek) → flow backend SQL queries.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3FlowBackendIT {

    companion object {
        private const val BUCKET = "flow-test-bucket"
        private const val ACCESS_KEY = "minioadmin"
        private const val SECRET_KEY = "minioadmin"

        @Container
        @JvmStatic
        val minio: GenericContainer<*> = GenericContainer("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data")
    }

    private lateinit var s3Client: S3Client
    private lateinit var parquetDescriptor: S3StorageDescriptor
    private lateinit var avroDescriptor: S3StorageDescriptor
    private lateinit var runner: FlowBackendContextRunner

    @BeforeAll
    fun setUp() {
        val endpoint = "http://${minio.host}:${minio.getMappedPort(9000)}"
        val credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
        )

        s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.US_EAST_1)
            .credentialsProvider(credentials)
            .forcePathStyle(true)
            .build()

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())

        val datasetsRoot = Path.of("../../../test/datasets/skymill")
        uploadDirectory(datasetsRoot.resolve("parquet"), "parquet/")
        uploadDirectory(datasetsRoot.resolve("avro"), "avro/")

        val auth = S3AuthDescriptor(accessKey = ACCESS_KEY, secretKey = SECRET_KEY)
        parquetDescriptor = S3StorageDescriptor(
            bucket = BUCKET, prefix = "parquet/",
            region = "us-east-1", endpoint = endpoint, auth = auth
        )
        avroDescriptor = S3StorageDescriptor(
            bucket = BUCKET, prefix = "avro/",
            region = "us-east-1", endpoint = endpoint, auth = auth
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

    @AfterAll
    fun tearDown() {
        s3Client.close()
    }

    // ── blob source: list ──────────────────────────────────────────

    @Test
    fun shouldListParquetBlobs() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(parquetDescriptor).use { src ->
            val blobs = src.listBlobs().toList()
            assertThat(blobs).isNotEmpty
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.startsWith("s3://$BUCKET/parquet/") }
            assertThat(blobs).allMatch { path ->
                path is S3BlobPath && path.contentLength != null && path.contentLength!! > 0L
            }
        }
    }

    @Test
    fun shouldListAvroBlobs() {
        val materializer = SourceMaterializer()
        materializer.createBlobSource(avroDescriptor).use { src ->
            val blobs = src.listBlobs().toList()
            assertThat(blobs).isNotEmpty
            assertThat(blobs.map { it.uri.toString() }).allMatch { it.startsWith("s3://$BUCKET/avro/") }
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
        val block = executeQuery("SELECT * FROM `skymill_parquet`.`aircraft_types` WHERE `id` = 1")
        assertThat(block.vectorSize).isGreaterThanOrEqualTo(1)
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

    private fun uploadDirectory(localDir: Path, s3Prefix: String) {
        Files.walk(localDir)
            .filter { Files.isRegularFile(it) }
            .forEach { file ->
                s3Client.putObject(
                    PutObjectRequest.builder().bucket(BUCKET).key(s3Prefix + file.fileName).build(),
                    file
                )
            }
    }
}
