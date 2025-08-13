import org.gradle.internal.declarativedsl.schemaBuilder.isPublic

plugins {
    `java-library`
    java
    `java-library-distribution`
    id("io.qpointz.plugins.mill")
}

distributions {
    main {
    }
}

dependencies {
    implementation("io.qpointz.mill:mill-core")
    implementation(libs.protobuf.java.util)
    implementation(libs.okhttp)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.boot.starter.test)
}

tasks.withType<ProcessResources>() {
    from(rootProject.layout.projectDirectory.dir("../").file("VERSION"))
}


testing {
    suites {
        register<JvmTestSuite>("testIT") {
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation("io.qpointz.mill:mill-starter-backends")
                    implementation("io.qpointz.mill:mill-jet-grpc-service")
                    implementation(libs.okhttp.mock.webserver)
                    implementation(libs.bootGRPC.client)
                    implementation(libs.bootGRPC.server)
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    implementation(libs.h2.database)
                    compileOnly(libs.lombok)
                    runtimeOnly(libs.opencensus.impl)
                    runtimeOnly(libs.grpc.census)
                    runtimeOnly(libs.grpc.context)
                    runtimeOnly(libs.grpc.all)
                    annotationProcessor(libs.lombok)

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