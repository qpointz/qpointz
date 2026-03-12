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
    implementation(project(":ai:mill-ai-v3-core"))
    implementation(project(":ai:mill-ai-v3-capabilities"))
    implementation(project(":ai:mill-ai-v3-langchain4j"))
    implementation(project(":data:mill-data-schema-core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.bundles.jackson)
    runtimeOnly(libs.bundles.logging)
}

repositories {
    mavenCentral()
}
