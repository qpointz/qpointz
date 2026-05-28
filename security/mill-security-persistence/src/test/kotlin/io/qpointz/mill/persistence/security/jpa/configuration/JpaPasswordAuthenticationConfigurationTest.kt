package io.qpointz.mill.persistence.security.jpa.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

class JpaPasswordAuthenticationConfigurationTest {

    /**
     * Mirrors [io.qpointz.mill.security.authentication.basic.PasswordAuthenticationConfiguration]
     * encoder declaration — unconditional.
     */
    @Configuration(proxyBeanMethods = false)
    open class BaseEncoderConfig {
        @Bean
        open fun passwordEncoder(): PasswordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    /**
     * Mirrors [JpaPasswordAuthenticationConfiguration] encoder declaration — conditional
     * on no existing [PasswordEncoder].
     */
    @Configuration(proxyBeanMethods = false)
    open class JpaEncoderConfig {
        @Bean
        @ConditionalOnMissingBean(PasswordEncoder::class)
        open fun jpaPasswordEncoder(): PasswordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    private val contextRunner = ApplicationContextRunner()

    @Test
    fun shouldRegisterSinglePasswordEncoder_whenBothConfigurationsPresent() {
        contextRunner
            .withUserConfiguration(BaseEncoderConfig::class.java, JpaEncoderConfig::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(PasswordEncoder::class.java)
            }
    }

    @Test
    fun shouldRegisterJpaPasswordEncoder_whenOnlyJpaConfigurationPresent() {
        contextRunner
            .withUserConfiguration(JpaEncoderConfig::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(PasswordEncoder::class.java)
            }
    }

    @Test
    fun shouldRegisterPasswordEncoder_whenOnlyBaseConfigurationPresent() {
        contextRunner
            .withUserConfiguration(BaseEncoderConfig::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(PasswordEncoder::class.java)
            }
    }
}
