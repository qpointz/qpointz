package io.qpointz.mill.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for aggregating projects (e.g. source/, ai/, core/, services/).
 *
 * Registers lifecycle tasks that delegate to identically-named tasks in all
 * subprojects, eliminating copy-paste boilerplate in each aggregating build file.
 *
 * Registered tasks: test, compileTestIT, testIT, jacocoTestReport, jacocoTestCoverageVerification
 */
class MillAggregatePlugin : Plugin<Project> {

    private val aggregatedTasks = listOf(
        "test" to "Runs all test tasks",
        "compileTestIT" to "Compiles all integration test sources",
        "testIT" to "Runs all integration tests",
        "jacocoTestReport" to "Generates coverage reports for all subprojects",
        "jacocoTestCoverageVerification" to "Verifies coverage thresholds for all subprojects"
    )

    override fun apply(project: Project) {
        for ((taskName, description) in aggregatedTasks) {
            if (project.tasks.findByName(taskName) == null) {
                project.tasks.register(taskName) {
                    this.description = "$description in ${project.name}"
                    this.group = "verification"
                    dependsOn(
                        project.subprojects.mapNotNull { sub ->
                            sub.tasks.findByName(taskName)
                        }
                    )
                }
            }
        }
    }
}
