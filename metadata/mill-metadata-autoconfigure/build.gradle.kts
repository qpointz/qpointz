plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    implementation(libs.boot.starter)
    implementation(libs.json.schema.validator)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
