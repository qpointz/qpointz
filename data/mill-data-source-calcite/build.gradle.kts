plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill source Calcite adapter"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-source-core"))
    api(project(":core:mill-core"))
    implementation(libs.calcite.core)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-testkit"))
                implementation(project(":data:formats:mill-data-format-text"))
                implementation(project(":data:formats:mill-data-format-parquet"))
                implementation(project(":data:formats:mill-data-format-avro"))
                implementation(libs.calcite.core)
                implementation(libs.assertj.core)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                targets {
                    all {
                        testTask.configure {
                            systemProperty(
                                "mill.repo.root",
                                rootProject.projectDir.absolutePath,
                            )
                        }
                    }
                }

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-testkit"))
                    implementation(project(":data:formats:mill-data-format-text"))
                    implementation(project(":data:formats:mill-data-format-parquet"))
                    implementation(project(":data:formats:mill-data-format-avro"))
                    implementation(libs.calcite.core)
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
