package io.qpointz.mill.data.odata.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Duration

class ODataServicePropertiesTest {

    @Configuration
    @EnableConfigurationProperties(ODataServiceProperties::class)
  private class TestConfiguration

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestConfiguration::class.java)
        .withPropertyValues("mill.data.services.odata.enable:true")

    @Test
    fun shouldBindEdmCacheProperties() {
        contextRunner
            .withPropertyValues(
                "mill.data.services.odata.cache.edm.enabled:true",
                "mill.data.services.odata.cache.edm.ttl:2m",
            )
            .run { context ->
                val props = context.getBean(ODataServiceProperties::class.java)
                assertThat(props.cache.getEdm().isEnabled).isTrue()
                assertThat(props.cache.getEdm().getTtl()).isEqualTo(Duration.ofMinutes(2))
            }
    }

    @Test
    fun shouldDefaultEdmCacheToDisabled() {
        contextRunner.run { context ->
            val props = context.getBean(ODataServiceProperties::class.java)
            assertThat(props.cache.getEdm().isEnabled).isFalse()
            assertThat(props.cache.getEdm().getTtl()).isNull()
        }
    }
}
