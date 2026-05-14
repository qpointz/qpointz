plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":cloud:aws:mill-cloud-aws-blob"))
    dokka(project(":cloud:aws:mill-cloud-aws-autoconfigure"))
    dokka(project(":cloud:gcp:mill-cloud-gcp-blob"))
    dokka(project(":cloud:gcp:mill-cloud-gcp-autoconfigure"))
    dokka(project(":cloud:azure:mill-cloud-azure-blob"))
    dokka(project(":cloud:azure:mill-cloud-azure-autoconfigure"))
}

tasks.register("test") {
    description = "Runs all test tasks in cloud subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}

tasks.register("testITClasses") {
    description = "Compiles all testIT sources in cloud subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("compileTestIT")
        }
    )
}

tasks.register("testIT") {
    description = "Runs all testIT tasks in cloud subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}
