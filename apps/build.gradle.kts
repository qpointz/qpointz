plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
}

tasks.register("test") {
    description = "Runs all test tasks in Apps subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources in Apps subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("compileTestIT")
        }
    )
}

tasks.register("testIT") {
    description = "Runs all testIT tasks in Apps subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}
