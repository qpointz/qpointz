plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill persistence auto-configuration."
    publishArtifacts = true
}

dependencies {
    api(project(":persistence:mill-persistence"))
    implementation(libs.boot.starter)
    annotationProcessor(libs.boot.configuration.processor)
}
