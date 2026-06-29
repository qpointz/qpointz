package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.utils.YamlMultiDocumentReader
import java.io.InputStream

/**
 * Loads [AgentProfile] rows from multi-document YAML seed resources (`kind: AgentProfile`).
 *
 * Later resources in the ordered list override earlier profiles with the same [AgentProfile.id].
 */
class ResourceProfileRegistry private constructor(
    private val profilesById: Map<String, AgentProfile>,
) : ProfileRegistry {

    override fun resolve(profileId: String): AgentProfile? = profilesById[profileId]

    override fun registeredProfiles(): List<AgentProfile> =
        profilesById.values.sortedBy { it.id }

    /** @return registered profile ids */
    val knownIds: Set<String> get() = profilesById.keys

    /**
     * Opens a seed resource by Spring-style location (e.g. {@code classpath:…}, {@code file:…}).
     */
    fun interface SeedInput {
        /**
         * @param location trimmed resource location
         * @return stream for the resource body
         */
        fun open(location: String): InputStream
    }

    companion object {

        /**
         * Parses all profiles from ordered seed locations via [input].
         *
         * @param input resolves each location to an [InputStream] (Spring {@code ResourceLoader} in hosts)
         * @param locations ordered resource location strings
         */
        @JvmStatic
        fun load(input: SeedInput, locations: List<String>): ResourceProfileRegistry {
            val merged = linkedMapOf<String, AgentProfile>()
            for (location in locations) {
                val loc = location.trim()
                if (loc.isEmpty()) continue
                input.open(loc).use { stream ->
                    parseDocuments(stream).forEach { profile ->
                        merged[profile.id] = profile
                    }
                }
            }
            return ResourceProfileRegistry(merged)
        }

        /**
         * Classpath convenience for tests and CLI harnesses.
         *
         * @param classLoader loader for {@code classpath:} locations
         * @param locations ordered locations ({@code classpath:…}, {@code file:…}, or bare filesystem path)
         */
        @JvmStatic
        fun load(classLoader: ClassLoader, locations: List<String>): ResourceProfileRegistry =
            load(
                SeedInput { location ->
                    when {
                        location.startsWith("classpath:") -> {
                            val path = location.removePrefix("classpath:")
                            classLoader.getResourceAsStream(path)
                                ?: throw IllegalArgumentException("Agent profile seed resource not found: $location")
                        }
                        location.startsWith("file:") ->
                            java.io.File(location.removePrefix("file:")).inputStream()
                        else -> java.io.File(location).inputStream()
                    }
                },
                locations,
            )

        /**
         * @param yaml multi-document YAML text (for unit tests)
         */
        @JvmStatic
        fun parse(yaml: String): ResourceProfileRegistry {
            val merged = linkedMapOf<String, AgentProfile>()
            if (yaml.isBlank()) {
                return ResourceProfileRegistry(merged)
            }
            yaml.byteInputStream().use { stream ->
                parseDocuments(stream).forEach { merged[it.id] = it }
            }
            return ResourceProfileRegistry(merged)
        }

        private fun parseDocuments(stream: InputStream): List<AgentProfile> =
            YamlMultiDocumentReader.readRootMaps(stream).mapNotNull { root ->
                @Suppress("UNCHECKED_CAST")
                val typed = root as Map<String, Any?>
                val kind = typed["kind"]?.toString() ?: return@mapNotNull null
                if (kind != "AgentProfile") return@mapNotNull null
                parseProfile(typed)
            }

        private fun parseProfile(root: Map<String, Any?>): AgentProfile {
            val id = root["id"]?.toString()?.trim()
                ?: error("AgentProfile missing id")
            val capabilities = (root["capabilities"] as? List<*>)
                ?.mapNotNull { it?.toString()?.trim() }
                ?.filter { it.isNotEmpty() }
                ?: error("AgentProfile '$id' missing capabilities")
            require(capabilities.isNotEmpty()) { "AgentProfile '$id' must list at least one capability" }
            return AgentProfile(
                id = id,
                capabilityIds = capabilities.toSet(),
                description = root["description"]?.toString()?.trim()?.takeIf { it.isNotEmpty() },
                prompts = parsePrompts(root),
            )
        }

        @Suppress("UNCHECKED_CAST")
        private fun parsePrompts(root: Map<String, Any?>): List<PromptAsset> {
            val promptsMap = root["prompts"] as? Map<String, Any?> ?: return emptyList()
            return promptsMap.map { (promptId, raw) ->
                val entry = raw as? Map<String, Any?>
                    ?: error("AgentProfile prompt '$promptId' must be a map with description and content")
                PromptAsset(
                    id = promptId,
                    description = entry["description"]?.toString()?.trim().orEmpty(),
                    content = entry["content"]?.toString()?.trim().orEmpty(),
                )
            }.sortedBy { it.id }
        }
    }
}
