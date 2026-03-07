import org.gradle.kotlin.dsl.project

plugins {
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":core:mill-core"))
    dokka(project(":core:mill-sql"))
    dokka(project(":core:mill-security"))
    dokka(project(":core:mill-test-kit"))
    dokka(project(":core:mill-spring-support"))
}
