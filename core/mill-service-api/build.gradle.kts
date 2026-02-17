plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill service API — cross-cutting conditional annotations, descriptors, and Spring utilities"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-security"))
    api(libs.boot.starter)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
