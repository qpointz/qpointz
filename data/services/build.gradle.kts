plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":data:services:mill-data-grpc-service"))
    dokka(project(":data:services:mill-data-http-service"))
}
