plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":persistence:mill-persistence"))
    dokka(project(":persistence:mill-persistence-autoconfigure"))
}

tasks.named("build") {
    dependsOn(
        ":persistence:mill-persistence:build",
        ":persistence:mill-persistence-autoconfigure:build",
    )
}
