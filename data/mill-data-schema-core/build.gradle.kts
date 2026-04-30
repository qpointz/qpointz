plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill schema facet boundary — aggregation of physical schema and schema-bound metadata"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-metadata"))
    implementation(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())

            targets {
                all {
                    testTask.configure {
                        systemProperty(
                            "skymill.datasets.dir",
                            rootProject.file("test/datasets/skymill").absolutePath
                        )
                    }
                }
            }

            dependencies {
                implementation(project())
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.jackson)
            }
        }

        configureEach {
            if (this is JvmTestSuite && this.name != "testIT") {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}
