import org.gradle.internal.declarativedsl.schemaBuilder.isPublic

plugins {
    `java-library`
    java
    `java-library-distribution`
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill JDBC Driver and Mill Client."
    publishArtifacts = true
}

distributions {
    main {
    }
}

dependencies {
    implementation(project(":core:mill-core"))
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
                    implementation(project(":data:mill-data-backends"))
                    implementation(project(":data:mill-data-autoconfigure"))
                    implementation(project(":data:services:mill-data-grpc-service"))
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