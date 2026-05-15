package io.qpointz.mill.cloud.azure.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ProtocolResolver

class AzureBlobProtocolResolverRegistrationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AdlsAutoConfiguration::class.java))
        .withPropertyValues(
            "mill.cloud.azure.adls.connection-string=" +
                "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;" +
                "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
                "BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1",
        )

    @Test
    fun shouldResolveAzureBlobLocationsThroughMillResourceImplementation() {
        runner.run { ctx ->
            assertThat(ctx).hasNotFailed()
            val pr = ctx.getBean("millAzureBlobProtocolResolver", ProtocolResolver::class.java)
            val rl = DefaultResourceLoader()
            val res = pr.resolve("azure-blob://c/blob/path.yaml", rl)
            assertThat(res?.javaClass?.name).contains("MillAzure")
        }
    }
}
