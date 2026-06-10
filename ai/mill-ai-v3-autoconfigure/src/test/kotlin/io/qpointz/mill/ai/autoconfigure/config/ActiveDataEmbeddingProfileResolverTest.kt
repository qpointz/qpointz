package io.qpointz.mill.ai.autoconfigure.config

import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ActiveDataEmbeddingProfileResolverTest {

    @Test
    fun shouldRejectMoreThanOneSourceInV1() {
        val data = DataEmbeddingConfigurationProperties().apply {
            embedding["default"] = DataEmbeddingConfigurationProperties.EmbeddingDataProfile().apply {
                sources = listOf(
                    DataEmbeddingConfigurationProperties.EmbeddingSource().apply { type = "metadata-facets" },
                    DataEmbeddingConfigurationProperties.EmbeddingSource().apply { type = "metadata-facets" },
                )
            }
        }
        val resolver = ActiveDataEmbeddingProfileResolver(AiV3ChatProperties(), data)
        assertThatThrownBy { resolver.validateProfile() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("at most one")
    }
}
