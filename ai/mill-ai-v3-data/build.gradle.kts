plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill AI v3 — data-layer adapters (schema port, SQL validation)"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3"))
    implementation(project(":data:mill-data-schema-core"))
    implementation(project(":data:mill-data-backend-core"))
    implementation(libs.calcite.core)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    testImplementation(project(":data:mill-data-autoconfigure"))
    testImplementation(project(":metadata:mill-metadata-autoconfigure"))
    testImplementation(libs.boot.starter)
    testImplementation(libs.boot.starter.test)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            targets {
                all {
                    testTask.configure {
                        systemProperty(
                            "flow.facet.it.root",
                            rootProject.projectDir.absolutePath,
                        )
                    }
                }
            }
            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(project(":data:formats:mill-data-format-text"))
                implementation(libs.boot.starter)
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                implementation(libs.mockito.core)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}

repositories {
    mavenCentral()
}
