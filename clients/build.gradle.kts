plugins {
    id ("org.sonarqube") version "5.0.0.4638"
    kotlin("jvm") version libs.versions.kotlin apply false
}

sonar {
    properties {
        property(
            "sonar.sources",
            listOf(project.layout.projectDirectory.dir("mill-py/millclient"))
        )
        property(
            "sonar.tests",
            listOf(project.layout.projectDirectory.dir("mill-py/tests"))
        )
    }
}

tasks.register("test") {
    description = "Runs all test tasks in Clients subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources in Clients subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("compileTestIT")
        }
    )
}

tasks.register("testIT") {
    description = "Runs all testIT tasks in Clients subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}
