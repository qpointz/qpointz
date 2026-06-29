package io.qpointz.mill.ai.scenario

/**
 * Root model for a YAML conversation scenario pack.
 *
 * @param name Human-readable pack name; also used to derive the regression record file slug.
 * @param profileId Agent profile id passed to the runtime (e.g. `hello-world`).
 * @param parameters Execution parameters such as scripted vs live mode.
 * @param run Ordered list of turn actions (currently `ask` steps).
 */
data class ScenarioPack(
    val name: String,
    val profileId: String,
    val parameters: ScenarioParameters = ScenarioParameters(),
    val run: List<AskRunItem> = emptyList(),
) {
    /** Stable slug for report filenames (`data-analysis-sql-emit` from name). */
    fun slug(): String =
        name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "scenario-pack" }
}

/**
 * Pack-level execution parameters.
 *
 * @param mode `scripted` uses [AskRunItem.script]; `live` requires a real model (not primary CI).
 */
data class ScenarioParameters(
    val mode: String = "scripted",
)

/**
 * A single user turn: prompt, optional scripted model steps, optional post-turn verification.
 *
 * @param ask User message sent to the agent for this turn.
 * @param script Ordered fake LLM responses (required when pack mode is `scripted`).
 * @param verify Optional assertions run against the collected turn outcome.
 */
data class AskRunItem(
    val ask: String,
    val script: List<ScriptStep>? = null,
    val verify: VerifySpec? = null,
)

/**
 * One scripted model invocation — either tool calls or a plain answer.
 *
 * @param toolCalls Model requests these tools (planner or pre-protocol step).
 * @param answer Plain text or structured JSON returned by the model.
 * @param expectTools Optional guard: assert tool names match before handlers run.
 */
data class ScriptStep(
    val toolCalls: List<ScriptToolCall>? = null,
    val answer: String? = null,
    val expectTools: List<String>? = null,
) {
    init {
        require(toolCalls != null || answer != null) {
            "ScriptStep must declare toolCalls and/or answer"
        }
        require(toolCalls == null || answer == null) {
            "ScriptStep must not declare both toolCalls and answer in the same entry"
        }
    }
}

/**
 * A single tool invocation requested by the scripted model.
 *
 * @param name Tool handler name (e.g. `say_hello`).
 * @param args Structured arguments map (serialized to JSON for LangChain4j).
 * @param id Optional tool request id; harness assigns `call-N` when omitted.
 */
data class ScriptToolCall(
    val name: String,
    val args: Map<String, Any?> = emptyMap(),
    val id: String? = null,
)

/**
 * Post-turn verification block from YAML `verify`.
 *
 * @param pass Minimum severity to fail the turn (`ERROR`, `WARN`, `INFO`).
 * @param check Polymorphic check specs (single-key maps, e.g. `events:`).
 */
data class VerifySpec(
    val pass: PassLevel = PassLevel.ERROR,
    val check: List<Map<String, Any?>> = emptyList(),
)

/** Severity threshold for a failed check to fail the test. */
enum class PassLevel {
    ERROR,
    WARN,
    INFO,
}
