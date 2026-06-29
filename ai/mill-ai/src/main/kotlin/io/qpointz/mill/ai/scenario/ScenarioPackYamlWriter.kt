package io.qpointz.mill.ai.scenario

import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.Instant

/**
 * Serializes [ScenarioPackExport] to YAML with header comments for manual verify tuning.
 */
class ScenarioPackYamlWriter(
    private val yamlMapper: YAMLMapper = YAML_MAPPER,
) {

    /**
     * Writes export metadata comments followed by the scenario pack YAML body.
     *
     * @param export Pack and hints produced by [ConversationScenarioExporter].
     */
    fun write(export: ScenarioPackExport): String {
        val header = buildString {
            appendLine("# source chatId: ${export.chatId}")
            appendLine("# exportedAt: ${export.exportedAt}")
            appendLine("# Add verify: manually — suggested checks:")
            if (export.verifyHints.isEmpty()) {
                appendLine("#   (none inferred from artifacts)")
            } else {
                export.verifyHints.forEach { appendLine("#   - $it") }
            }
            appendLine()
        }
        return header + yamlMapper.writeValueAsString(export.pack)
    }

    companion object {
        private val YAML_MAPPER: YAMLMapper = YAMLMapper.builder()
            .addModule(kotlinModule())
            .build()

        val jsonMapper: JsonMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .build()
    }
}

/**
 * Result of exporting a live chat to a draft scenario pack.
 *
 * @param chatId Source conversation id.
 * @param exportedAt Export timestamp (UTC).
 * @param pack Draft scenario pack without auto-generated [VerifySpec].
 * @param verifyHints Human-readable suggestions emitted as YAML comments.
 */
data class ScenarioPackExport(
    val chatId: String,
    val exportedAt: Instant,
    val pack: ScenarioPack,
    val verifyHints: List<String> = emptyList(),
)
