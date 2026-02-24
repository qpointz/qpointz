plugins {
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":core:mill-core"))
    dokka(project(":core:mill-security"))
    dokka(project(":core:mill-service-security"))
    dokka(project(":core:mill-service-api"))
    dokka(project(":core:mill-test-kit"))
    dokka(project(":core:mill-service-starter"))
}
