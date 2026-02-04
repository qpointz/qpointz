plugins {
    base
    id("jacoco-report-aggregation")
    kotlin("jvm") version "2.3.0" apply false
}

tasks.register("testIT") {
    description = "Runs all testIT tasks across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":services", ":apps", ":clients")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("testIT")
            }
    )
}


tasks.register("test") {
    description = "Runs all test tasks across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":services", ":apps", ":clients")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("test")
            }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":services", ":apps", ":clients")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("compileTestIT")
            }
    )
}