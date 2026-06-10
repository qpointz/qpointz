package io.qpointz.mill.ai.test

fun interface ScenarioRunner {
    fun run(userInput: String): AgentScenarioResult
}
