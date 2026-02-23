plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":metadata:mill-metadata-core"))
    dokka(project(":metadata:mill-metadata-autoconfigure"))

    dokka(project(":metadata:mill-metadata-service"))
}

// Aggregate metadata module builds under a single stable CI target.
tasks.named("build") {
    dependsOn(
        ":metadata:mill-metadata-core:build",
        ":metadata:mill-metadata-autoconfigure:build",
        ":metadata:mill-metadata-service:build",
    )
}
