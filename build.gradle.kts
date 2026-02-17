plugins {
    base
    id("jacoco-report-aggregation")
    alias(libs.plugins.kotlin) apply false
    id("org.jetbrains.dokka") version "2.2.0-Beta"
    id("org.jetbrains.dokka-javadoc") version "2.2.0-Beta" apply false
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}

dependencies {
    dokka(project(":ai"))
    dokka(project(":core"))
    dokka(project(":data"))
    dokka(project(":metadata"))
    dokka(project(":apps"))
    dokka(project(":clients"))
    dokka(project(":ui"))
}

tasks.register<Zip>("publishSonatypeBundle") {
    description = "Zips Sonatype Bundle to be published to Maven Central Repository"
    group = "publishing"
    from(layout.buildDirectory.dir("repo"))
    include("**/*")
    archiveBaseName.set("sonatype-bundle")
    destinationDirectory.set(layout.buildDirectory.dir("sonatype-bundle"))
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("publish") })
}

tasks.register("testIT") {
    description = "Runs all testIT tasks across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":data", ":metadata", ":apps", ":clients", ":ui")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("testIT")
            }
    )
}


tasks.register("test") {
    description = "Runs all test tasks across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":data", ":metadata", ":apps", ":clients", ":ui")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("test")
            }
    )
}

tasks.register("compileTestIT") {
    description = "Compiles all testIT sources across all parent projects"
    group = "verification"

    dependsOn(
        listOf(":ai", ":core", ":data", ":metadata", ":apps", ":clients", ":ui")
            .mapNotNull { projectPath ->
                project(projectPath).tasks.findByName("compileTestIT")
            }
    )
}