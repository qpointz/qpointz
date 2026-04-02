plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    `java-library`
}

mill {
    description = "Mill data backends implementation (calcite, jdbc, flow) and adapters"
    publishArtifacts = true
}


dependencies {
    api(project(":data:mill-data-backend-core"))
    api(project(":data:mill-data-source-calcite"))
    api(project(":core:mill-sql"))
    implementation(project(":data:mill-data-metadata"))
    implementation(libs.caffeine)
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.calcite.file)
    implementation(libs.substrait.isthmus)
    compileOnly(libs.lombok)
    runtimeOnly(libs.bundles.logging)
    annotationProcessor(libs.lombok)
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
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(libs.boot.starter)
                implementation(libs.boot.starter.test)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-testkit"))
                    implementation(project(":data:formats:mill-data-format-text"))
                    implementation(libs.calcite.core)
                    implementation(libs.protobuf.java.util)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.assertj.core)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
