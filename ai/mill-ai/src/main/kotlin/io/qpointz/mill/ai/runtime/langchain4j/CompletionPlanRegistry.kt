package io.qpointz.mill.ai.runtime.langchain4j

/**
 * Declares turn-scoped completion plan recipes (G-26 Option D).
 */
object CompletionPlanRegistry {

    /** Completion mode declared on `validate_sql` or implied by `enrich-existing` entry. */
    enum class Mode(val wireValue: String) {
        SQL_ONLY("sql-only"),
        SQL_WITH_CHART("sql-with-chart"),
        ENRICH_EXISTING("enrich-existing"),
        ;

        companion object {
            /** @param value wire value from tool input; defaults to [SQL_ONLY] */
            fun fromWire(value: String?): Mode =
                entries.firstOrNull { it.wireValue == value?.trim()?.lowercase() } ?: SQL_ONLY
        }
    }

    /** Status of a single plan step. */
    enum class StepStatus { PENDING, SUCCEEDED, FAILED }

    /**
     * One step in a completion plan recipe.
     *
     * @param toolName capability tool that satisfies this step
     * @param repeatable when true, multiple successful invocations are allowed (e.g. chart specs)
     */
    data class PlanStep(
        val toolName: String,
        val repeatable: Boolean = false,
        var status: StepStatus = StepStatus.PENDING,
    )

    /**
     * Turn-scoped completion plan tracking draft assembly until finalize.
     *
     * @param id unique plan id within the turn
     * @param mode completion recipe
     * @param steps ordered required steps
     * @param draft mutable artifact draft
     * @param targetArtifactId existing row id for `enrich-existing` updates
     * @param normalizedSqlKey deduplication key for multiple SQL per turn
     * @param terminal when true, plan cannot finalize (failed terminal rule)
     */
    data class Plan(
        val id: String,
        val mode: Mode,
        val steps: MutableList<PlanStep>,
        val draft: GeneratedSqlPayload.Draft,
        val targetArtifactId: String? = null,
        val normalizedSqlKey: String,
        var terminal: Boolean = false,
    ) {
        /** @return true when all non-repeatable steps succeeded and plan is not terminal */
        fun canFinalize(): Boolean {
            if (terminal) return false
            return when (mode) {
                Mode.SQL_ONLY -> stepStatus("validate_sql") == StepStatus.SUCCEEDED
                Mode.SQL_WITH_CHART -> {
                    stepStatus("validate_sql") == StepStatus.SUCCEEDED &&
                        stepStatus("describe_sql") == StepStatus.SUCCEEDED &&
                        hasSucceededChartStep()
                }
                Mode.ENRICH_EXISTING -> {
                    val schemaOk = draft.schema.isNotEmpty() || stepStatus("describe_sql") != StepStatus.PENDING
                    schemaOk && hasSucceededChartStep() && stepStatus("describe_sql") != StepStatus.FAILED
                }
            }
        }

        private fun hasSucceededChartStep(): Boolean =
            steps.any { it.toolName == "validate_chart_spec" && it.status == StepStatus.SUCCEEDED }

        private fun stepStatus(toolName: String): StepStatus =
            steps.firstOrNull { it.toolName == toolName && !it.repeatable }?.status ?: StepStatus.PENDING

        /** Marks the first matching pending step as succeeded. */
        fun markStepSucceeded(toolName: String) {
            val step = if (toolName == "validate_chart_spec") {
                steps.firstOrNull { it.toolName == toolName && it.repeatable }
                    ?: steps.firstOrNull { it.toolName == toolName }
            } else {
                steps.firstOrNull { it.toolName == toolName && it.status == StepStatus.PENDING }
            } ?: return
            step.status = StepStatus.SUCCEEDED
        }

        /** Marks a required non-repeatable step failed and sets terminal when retry cannot recover it. */
        fun markStepFailed(toolName: String) {
            steps.filter { it.toolName == toolName && !it.repeatable }.forEach { it.status = StepStatus.FAILED }
            if (mode == Mode.SQL_WITH_CHART && toolName == "describe_sql") {
                terminal = true
            }
        }
    }

    /**
     * Builds the step list for [mode].
     *
     * @param mode completion mode
     * @param schemaAlreadyPresent when true, `describe_sql` is omitted for enrich-existing
     */
    fun stepsFor(mode: Mode, schemaAlreadyPresent: Boolean = false): List<PlanStep> =
        when (mode) {
            Mode.SQL_ONLY -> listOf(PlanStep("validate_sql"))
            Mode.SQL_WITH_CHART -> listOf(
                PlanStep("validate_sql"),
                PlanStep("describe_sql"),
                PlanStep("validate_chart_spec", repeatable = true),
            )
            Mode.ENRICH_EXISTING -> buildList {
                if (!schemaAlreadyPresent) add(PlanStep("describe_sql"))
                add(PlanStep("validate_chart_spec", repeatable = true))
            }
        }
}
