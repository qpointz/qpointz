package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.cloud.aws.autoconfigure.S3AutoConfiguration
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.persistence.metadata.jpa.it.MetadataSeedLedgerITApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets

@Configuration
@Import(S3AutoConfiguration::class)
open class MetadataSeedS3AutoImport

/**
 * Metadata seed pipeline loads a follow-on YAML from {@code s3://} (MinIO) alongside classpath seeds.
 */
@SpringBootTest(classes = [MetadataSeedLedgerITApplication::class, MetadataSeedS3AutoImport::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = [
        "mill.metadata.repository.type=jpa",
        "mill.metadata.seed.resources[0]=classpath:metadata/platform-bootstrap.yaml",
        "mill.metadata.seed.resources[1]=s3://mill-metadata-seed-s3/metadata/one-entity.yaml",
    ],
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetadataStartupSeedS3IT {

    @Autowired
    private lateinit var entityRepository: EntityRepository

    @Test
    fun shouldApplyEntityFromS3SeedResource() {
        val id = "urn:mill/model/table:seed.startup.one"
        assertThat(entityRepository.exists(id)).isTrue
    }

    companion object {
        @JvmField
        val MINIO: GenericContainer<*> = GenericContainer(DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server", "/data")

        init {
            MINIO.start()
            val endpoint = "http://${MINIO.host}:${MINIO.getMappedPort(9000)}"
            val s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create("minioadmin", "minioadmin")),
                )
                .forcePathStyle(true)
                .build()
            s3.createBucket(CreateBucketRequest.builder().bucket("mill-metadata-seed-s3").build())
            val yaml = MetadataStartupSeedS3IT::class.java.getResourceAsStream("/metadata-seed/one-entity.yaml")
                ?: error("missing test resource")
            val bytes = yaml.use { it.readBytes() }
            s3.putObject(
                PutObjectRequest.builder().bucket("mill-metadata-seed-s3").key("metadata/one-entity.yaml").build(),
                RequestBody.fromBytes(bytes),
            )
        }

        @JvmStatic
        @DynamicPropertySource
        fun minioProps(registry: DynamicPropertyRegistry) {
            val endpoint = "http://${MINIO.host}:${MINIO.getMappedPort(9000)}"
            registry.add("mill.cloud.aws.s3.endpoint") { endpoint }
            registry.add("mill.cloud.aws.s3.region") { "us-east-1" }
            registry.add("mill.cloud.aws.s3.access-key") { "minioadmin" }
            registry.add("mill.cloud.aws.s3.secret-key") { "minioadmin" }
        }

        @JvmStatic
        @AfterAll
        fun stopMinio() {
            MINIO.stop()
        }
    }
}
