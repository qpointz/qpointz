plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":apps:mill-service"))
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

tasks.register("testITClasses") {
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
