plugins {
    application
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill AI v3 interactive CLI for manual agent testing"
    publishArtifacts = false
}

application {
    mainClass = "io.qpointz.mill.ai.cli.CliAppKt"
}

dependencies {
    implementation(project(":ai:mill-ai-v3"))
    implementation(project(":ai:mill-ai-v3-data"))
    implementation(project(":data:mill-data-schema-core"))
    implementation(project(":core:mill-sql"))
    implementation(libs.bundles.jackson)
    implementation(libs.picocli)
    runtimeOnly(libs.bundles.logging)
}

repositories {
    mavenCentral()
}
