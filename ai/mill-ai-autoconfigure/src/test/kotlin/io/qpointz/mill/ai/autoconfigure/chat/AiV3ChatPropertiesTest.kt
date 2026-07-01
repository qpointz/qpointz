package io.qpointz.mill.ai.autoconfigure.chat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class AiV3ChatPropertiesTest {

    @Test
    fun shouldDefaultMaxIterations_toTwenty() {
        val props = AiV3ChatProperties()
        assertThat(props.maxIterations).isEqualTo(20)
    }

    @Test
    fun shouldBindMaxIterations_fromYamlMap() {
        val source = MapConfigurationPropertySource(
            mapOf("mill.ai.chat.max-iterations" to 5),
        )
        val props = Binder(source).bind("mill.ai.chat", AiV3ChatProperties::class.java).get()
        assertThat(props.maxIterations).isEqualTo(5)
    }

    @Test
    fun shouldRejectNonPositiveMaxIterations_atRuntimeBoundary() {
        assertThrows(IllegalArgumentException::class.java) {
            io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent.validateMaxIterations(
                AiV3ChatProperties(maxIterations = 0).maxIterations,
            )
        }
    }
}
