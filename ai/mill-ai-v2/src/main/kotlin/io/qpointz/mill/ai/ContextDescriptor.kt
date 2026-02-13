package io.qpointz.mill.ai

import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.utils.YamlUtils
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.tool.ToolCallback
import java.io.InputStream

 data class StaticCapability(
     override val name: String,
     override val description: String,
     override val system: String?,
     override val tools: List<ToolCallback>,
     override val advisors: List<Advisor>,
     override val protocol: JsonNode?
 ) : Capability

data class ContextDescriptor(
    val source: String = "classpath:",
    val name: String,
    val description: String,
    val system: String? = null,
    val protocol: String? = null,
) {

    fun createCapability(): Capability {
        fun createProtocol():JsonNode? {
            return this.protocol?.let {
                rs(it)
                    ?.readText()
                    ?.let { json -> YamlUtils.defaultYamlMapper().readTree(json) }
            }
        }

        return StaticCapability(
            name = this.name,
            description = this.description,
            system = this.system,
            tools = emptyList(),
            advisors = emptyList(),
            protocol = createProtocol()
        )
    }

    companion object {

        private fun rs(path: String): InputStream? {
            val classLoader = Thread.currentThread().contextClassLoader
            return requireNotNull(classLoader.getResourceAsStream(path)) {
                "Resource not found: $path"
            }
        }

        private fun InputStream.readText(): String? =
            bufferedReader().use { it.readText() }


        private data class DescriptorCommon(val name: String,
                                            val description: String,
                                            val system: String?,
                                            val protocol: String?)

        /**
         * Loads a CapabilityDescriptor from a YAML resource file.
         * @param resourcePath the path to the resource file.
         * @throws IllegalArgumentException if the resource is not found.
         */
        fun fromResource(resourcePath: String): ContextDescriptor {
            val parent = resourcePath.substringBeforeLast("/")

            val mapper = YamlUtils.defaultYamlMapper()

            val common: DescriptorCommon = mapper.readValue(rs(resourcePath), DescriptorCommon::class.java)
            return ContextDescriptor(
                source = "classpath:",
                name = common.name,
                description = common.description,
                system = common.system,
                protocol = common.protocol ?: "${parent}/protocol.json"  )
        }
    }

}