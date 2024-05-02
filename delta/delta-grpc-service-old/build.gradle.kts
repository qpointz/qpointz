plugins {
    java
    application
    scala
    jacoco
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)

    implementation(project(":delta-grpc"))
    //implementation(project(":rapids-common"))
    //implementation(project(":rapids-core-legacy"))
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)

    implementation(libs.spring.context)

    implementation(libs.vertx.core)
    implementation(libs.vertx.grpc)
    implementation(libs.vertx.grpc.server)
    implementation(libs.vertx.grpc.client)

    implementation(libs.protobuf.java)
    implementation(libs.javax.annotation.api)
}

application {
    mainClass.set("io.qpointz.rapids.server.GrpcService")
    executableDir = "bin"
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
                    implementation(project(":rapids-test-kit"))
                    implementation(libs.apache.commons.lang3)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}