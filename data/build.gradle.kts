plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":data:mill-data-service"))
    dokka(project(":data:mill-data-backends"))
    dokka(project(":data:mill-data-autoconfigure"))
    dokka(project(":data:mill-data-grpc-service"))
    dokka(project(":data:mill-data-http-service"))
    dokka(project(":data:mill-data-source-core"))
    dokka(project(":data:mill-data-source-calcite"))
    dokka(project(":data:formats:mill-source-format-text"))
    dokka(project(":data:formats:mill-source-format-excel"))
    dokka(project(":data:formats:mill-source-format-avro"))
    dokka(project(":data:formats:mill-source-format-parquet"))
}

tasks.register("test") {
    description = "Runs all test tasks in data subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("test")
        }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources in data subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("compileTestIT")
        }
    )
}

tasks.register("testIT") {
    description = "Runs all testIT tasks in data subprojects"
    group = "verification"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testIT")
        }
    )
}
