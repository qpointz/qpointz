plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill ADLS / Azure Blob Storage backend"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-source-core"))
    implementation(libs.azure.storage.blob)
    implementation(libs.azure.identity)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.azure.storage.blob)
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.assertj.core)
                implementation(libs.slf4j.api)
                implementation(libs.logback.core)
                implementation(libs.logback.classic)
                implementation(project(":data:mill-data-testkit"))
                implementation(project(":data:mill-data-backends"))
                implementation(project(":data:mill-data-source-core"))
                implementation(project(":data:formats:mill-data-format-parquet"))
                implementation(project(":data:formats:mill-data-format-avro"))
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.slf4j.api)
                    implementation(libs.logback.core)
                    implementation(libs.logback.classic)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
