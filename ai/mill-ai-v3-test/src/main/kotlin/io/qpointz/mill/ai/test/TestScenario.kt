package io.qpointz.mill.ai.test

/**
 * Placeholder scenario model for future v3 scenario-based tests.
 *
 * Keeping a tiny shared test type now makes it easier to grow from single integration tests into
 * table-driven workflow validation later.
 */
data class TestScenario(
    val name: String,
    val input: String,
)
