package io.qpointz.mill.cloud.gcp.autoconfigure

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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(GcsAutoConfiguration::class)
open class MillGcsObjectResourceTestApplication

/**
 * Emulator-backed read for {@code gs://} resources (fake-gcs-server).
 */
@SpringBootTest(classes = [MillGcsObjectResourceTestApplication::class], webEnvironment = WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MillGcsObjectResourceIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun shouldReadUtf8ObjectFromFakeGcs() {
        val rl = protocolAwareResourceLoader()
        val text = rl.getResource("gs://$BUCKET/seed/hello.txt").inputStream.bufferedReader().readText()
        assertThat(text).isEqualTo("hello-gcs")
    }

    private fun protocolAwareResourceLoader(): ResourceLoader {
        val rl = DefaultResourceLoader((applicationContext as org.springframework.core.io.ResourceLoader).classLoader)
        applicationContext.getBeansOfType(ProtocolResolver::class.java).values.forEach { rl.addProtocolResolver(it) }
        return rl
    }

    companion object {
        private const val BUCKET = "mill-gcs-resource-it"
        private const val GCS_PORT = 4443

        @JvmField
        val GCS: GenericContainer<*> = GenericContainer(DockerImageName.parse("fsouza/fake-gcs-server:latest"))
            .withExposedPorts(GCS_PORT)
            .withCommand("-scheme", "http", "-port", GCS_PORT.toString())

        init {
            GCS.start()
            val endpoint = "http://${GCS.host}:${GCS.getMappedPort(GCS_PORT)}"
            updateExternalUrl(endpoint)
            createBucket(endpoint, BUCKET)
            uploadObject(endpoint, BUCKET, "seed/hello.txt", "hello-gcs".toByteArray())
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerGcsProperties(registry: DynamicPropertyRegistry) {
            val endpoint = "http://${GCS.host}:${GCS.getMappedPort(GCS_PORT)}"
            registry.add("mill.cloud.gcp.gcs.emulator-host") { endpoint }
            registry.add("mill.cloud.gcp.gcs.project-id") { "mill-test" }
        }

        @JvmStatic
        @AfterAll
        fun stopGcs() {
            GCS.stop()
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

        private fun createBucket(endpoint: String, bucket: String) {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$endpoint/storage/v1/b"))
                .POST(HttpRequest.BodyPublishers.ofString("""{"name":"$bucket"}"""))
                .header("Content-Type", "application/json")
                .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }

        private fun uploadObject(endpoint: String, bucket: String, name: String, content: ByteArray) {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$endpoint/upload/storage/v1/b/$bucket/o?uploadType=media&name=$name"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .header("Content-Type", "application/octet-stream")
                .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
    }
}
