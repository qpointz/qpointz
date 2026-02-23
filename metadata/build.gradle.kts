plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":metadata:mill-metadata-core"))
    dokka(project(":metadata:mill-metadata-autoconfigure"))

    dokka(project(":metadata:mill-metadata-service"))
}
