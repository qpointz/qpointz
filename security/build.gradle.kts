plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":security:mill-security-persistence"))
}

tasks.named("build") {
    dependsOn(
        ":security:mill-security-persistence:build",
    )
}
