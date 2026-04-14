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
    implementation(libs.bundles.jackson)
    implementation(libs.picocli)
    runtimeOnly(libs.bundles.logging)
}

repositories {
    mavenCentral()
}
