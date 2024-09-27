plugins {
    `java-library`
    java
    `java-library-distribution`
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

fun shadow() {
}

distributions {
    main {
        //distributionBaseName = "my-name"
    }
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.boot.starter.test)
}

tasks.withType<ProcessResources>() {
    from(rootProject.layout.projectDirectory.dir("../").file("VERSION"))
}


//tasks.getByName("processResources").doFirst {
//    val distName = distributions.getByName("main").distributionBaseName.get()
//    val outdir = project.layout.buildDirectory.dir("install/${distName}").get()
//
    //val versionFile =
    //copy {
    //    from(versionFile)
    //    into(layout.buildDirectory.dir("resources/main"))
    //}
//    copy {
//        from(rootProject.layout.projectDirectory.dir("../etc/data/datasets/airlines/csv"))
//        into(outdir.dir("examples/data/airlines"))
//    }
//}

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
                    implementation(project(":common"))
                    implementation(project(":calcite-service"))
                    implementation(project(":common-backend-service"))
                    implementation(libs.bootGRPC.client)
                    implementation(libs.bootGRPC.server)
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    runtimeOnly("io.opencensus:opencensus-impl:0.31.1")
                    runtimeOnly(libs.grpc.census)
                    runtimeOnly(libs.grpc.context)
                    runtimeOnly(libs.grpc.all)
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