plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    api(project(":metadata:mill-metadata-service"))
    compileOnly(project(":metadata:mill-metadata-persistence"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.web)
    implementation(libs.json.schema.validator)
    annotationProcessor(libs.boot.configuration.processor)
}
