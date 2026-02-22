plugins {
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    `java-library`
}

mill {
    description = "Library provides base Mill Service implementation"
    publishArtifacts = true
}


dependencies {
    api(project(":data:mill-data-backend-core"))
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
            dependencies {
                implementation(project(":data:mill-data-autoconfigure"))
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
