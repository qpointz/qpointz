package io.qpointz.mill.cloud.gcp.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ProtocolResolver

class GcsProtocolResolverRegistrationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(GcsAutoConfiguration::class.java))

    @Test
    fun shouldResolveGsLocationsThroughMillResourceImplementation() {
        runner.run { ctx ->
            assertThat(ctx).hasNotFailed()
            val pr = ctx.getBean("millGcsProtocolResolver", ProtocolResolver::class.java)
            val rl = DefaultResourceLoader()
            val res = pr.resolve("gs://any-bucket/path/file.yaml", rl)
            assertThat(res?.javaClass?.name).contains("MillGcs")
        }
    }
}
