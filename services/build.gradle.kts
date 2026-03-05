plugins {
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":services:mill-service-security"))
    dokka(project(":services:mill-service-api"))
    dokka(project(":services:mill-well-known-service"))
    dokka(project(":services:mill-data-grpc-service"))
    dokka(project(":services:mill-data-http-service"))
}
