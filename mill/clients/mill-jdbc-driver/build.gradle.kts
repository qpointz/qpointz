plugins {
    `java-library`
    `java-library-distribution`
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

fun shadow() {
    //archiveBaseName("lala")
    //baseN
}

distributions {
    main {
        //distributionBaseName = "my-name"
    }
}

dependencies {
    implementation(project(":mill-core"))
    implementation(libs.grpc.netty.shaded)
    compileOnly(libs.lombok)
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
                    implementation(project(":mill-core"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    implementation(libs.h2.database)
                    annotationProcessor(libs.lombok)
                    compileOnly(libs.lombok)
                }

                targets {
                    all {
                        testTask.configure {
                            testLogging {
                                showStandardStreams = true
                                outputs.upToDateWhen { false }
                            }
                        }
                    }
                }
            }
        }
    }
}