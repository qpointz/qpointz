package io.qpointz.mill.cloud.azure.autoconfigure

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
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceClientBuilder

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(AdlsAutoConfiguration::class)
open class MillAzureBlobObjectResourceTestApplication

/**
 * Emulator-backed read for {@code azure-blob://} resources (Azurite).
 */
@SpringBootTest(classes = [MillAzureBlobObjectResourceTestApplication::class], webEnvironment = WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MillAzureBlobObjectResourceIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun shouldReadUtf8BlobFromAzurite() {
        val rl = protocolAwareResourceLoader()
        val text =
            rl.getResource("azure-blob://$CONTAINER/seed/hello.txt").inputStream.bufferedReader().readText()
        assertThat(text).isEqualTo("hello-azure")
    }

    private fun protocolAwareResourceLoader(): ResourceLoader {
        val rl = DefaultResourceLoader((applicationContext as org.springframework.core.io.ResourceLoader).classLoader)
        applicationContext.getBeansOfType(ProtocolResolver::class.java).values.forEach { rl.addProtocolResolver(it) }
        return rl
    }

    companion object {
        private const val BLOB_PORT = 10000
        private const val CONTAINER = "mill-azure-res-it"
        private const val ACCOUNT_NAME = "devstoreaccount1"
        private const val ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="

        @JvmField
        val AZURITE: GenericContainer<*> = GenericContainer(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite"))
            .withExposedPorts(BLOB_PORT)
            .withCommand("azurite-blob", "--blobHost", "0.0.0.0", "--blobPort", "$BLOB_PORT", "--skipApiVersionCheck")

        private fun connectionString(): String {
            val host = AZURITE.host
            val port = AZURITE.getMappedPort(BLOB_PORT)
            return "DefaultEndpointsProtocol=http;" +
                "AccountName=$ACCOUNT_NAME;" +
                "AccountKey=$ACCOUNT_KEY;" +
                "BlobEndpoint=http://$host:$port/$ACCOUNT_NAME"
        }

        init {
            AZURITE.start()
            val conn = connectionString()
            val svc = BlobServiceClientBuilder().connectionString(conn).buildClient()
            val container = svc.getBlobContainerClient(CONTAINER)
            container.createIfNotExists()
            container.getBlobClient("seed/hello.txt").upload(BinaryData.fromString("hello-azure"), true)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerAzuriteProperties(registry: DynamicPropertyRegistry) {
            registry.add("mill.cloud.azure.adls.connection-string") { connectionString() }
        }

        @JvmStatic
        @AfterAll
        fun stopAzurite() {
            AZURITE.stop()
        }
    }
}
