plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill source format - Arrow IPC"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-source-core"))
    implementation(libs.apache.arrow.vector)
    implementation(libs.apache.arrow.memory.netty)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                targets.configureEach {
                    testTask.configure {
                        jvmArgs(
                            "--add-opens=java.base/java.nio=ALL-UNNAMED",
                            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
                        )
                        systemProperty("arrow.enable_unsafe_memory_access", "true")
                        systemProperty("io.netty.tryReflectionSetAccessible", "true")
                    }
                }

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
