plugins {
    application
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)

    implementation(project(":rapids-grpc"))
    implementation(project(":rapids-core-legacy"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)

    implementation(libs.vertx.grpc)
    implementation(libs.vertx.grpc.server)
    implementation(libs.vertx.grpc.client)
    implementation(libs.protobuf.java)
    implementation(libs.javax.annotation.api)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}