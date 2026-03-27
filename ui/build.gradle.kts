plugins {
    base
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":services:mill-ui-service"))
}