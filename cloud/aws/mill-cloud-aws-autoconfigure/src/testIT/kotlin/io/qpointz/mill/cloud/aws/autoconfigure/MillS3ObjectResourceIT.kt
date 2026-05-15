package io.qpointz.mill.cloud.aws.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ProtocolResolver
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(S3AutoConfiguration::class)
open class MillS3ObjectResourceTestApplication

/**
 * Emulator-backed read for {@code s3://} [org.springframework.core.io.Resource] (MinIO).
 */
@SpringBootTest(classes = [MillS3ObjectResourceTestApplication::class], webEnvironment = WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MillS3ObjectResourceIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun shouldReadUtf8ObjectFromMinio() {
        val rl = protocolAwareResourceLoader()
        val text = rl.getResource("s3://$BUCKET/seed/hello.txt").inputStream.bufferedReader().readText()
        assertThat(text).isEqualTo("hello-cloud")
    }

    private fun protocolAwareResourceLoader(): ResourceLoader {
        val rl = DefaultResourceLoader((applicationContext as org.springframework.core.io.ResourceLoader).classLoader)
        applicationContext.getBeansOfType(ProtocolResolver::class.java).values.forEach { rl.addProtocolResolver(it) }
        return rl
    }

    companion object {
        private const val BUCKET = "mill-s3-resource-it"

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
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())
            s3.putObject(
                PutObjectRequest.builder().bucket(BUCKET).key("seed/hello.txt").build(),
                RequestBody.fromString("hello-cloud"),
            )
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerMinioProperties(registry: DynamicPropertyRegistry) {
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
