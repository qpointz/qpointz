plugins {
    kotlin("jvm") version "1.9.23" apply false
}

tasks.register("test") {
    description = "Runs all test tasks in Core subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources in Core subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("compileTestIT")
        }
    )
}

tasks.register("testIT") {
    description = "Runs all testIT tasks in Core subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}
