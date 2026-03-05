plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill service API — cross-cutting conditional annotations, descriptors, and Spring utilities"
    publishArtifacts = true
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
