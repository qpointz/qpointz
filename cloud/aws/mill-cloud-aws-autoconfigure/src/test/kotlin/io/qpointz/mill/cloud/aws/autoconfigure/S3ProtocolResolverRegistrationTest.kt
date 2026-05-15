package io.qpointz.mill.cloud.aws.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ProtocolResolver

/**
 * Ensures the S3 [ProtocolResolver] bean builds Mill S3 resources when invoked with a
 * [org.springframework.core.io.ResourceLoader] delegate.
 */
class S3ProtocolResolverRegistrationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(S3AutoConfiguration::class.java))

    @Test
    fun shouldResolveS3LocationsThroughMillResourceImplementation() {
        runner.run { ctx ->
            assertThat(ctx).hasNotFailed()
            val pr = ctx.getBean("millS3ProtocolResolver", ProtocolResolver::class.java)
            val rl = DefaultResourceLoader()
            val res = pr.resolve("s3://any-bucket/path/file.yaml", rl)
            assertThat(res?.javaClass?.name).contains("MillS3")
        }
    }
}
