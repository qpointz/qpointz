plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    `java-library`
    jacoco
    id("com.google.protobuf") version "0.9.4"
}

shadow {
    //archiveBaseName("lala")
    //baseN
}

sourceSets {
    main {
        proto {
            srcDir("../proto")
            exclude("substrait/**")
        }
    }
}

dependencies {
    api(libs.substrait.core)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    //implementation(libs.calcite.core)
    //implementation(libs.calcite.csv)
    //implementation(project(":delta-grpc"))
    //implementation(libs.avatica.core)
    //implementation(libs.avatica.server)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
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
                    //implementation(project(":rapids-test-kit"))
                    //implementation(project(":delta-grpc-service"))
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    //implementation(libs.h2.database)
                    //implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}