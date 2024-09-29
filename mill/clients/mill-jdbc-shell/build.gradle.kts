plugins {
    application
    mill
}

mill {
    publishToSonatype = false
}

application {
    mainClass = "sqlline.SqlLine"
    applicationName = "mill-sql-line"
}

tasks.getByName("installDist").doLast {
//    val distName = distributions.getByName("main").distributionBaseName.get()
//    val outdir = project.layout.buildDirectory.dir("install/${distName}").get()
//
//    copy {
//        from(layout.projectDirectory.dir("etc/config/default"))
//        into(outdir.dir("config"))
//    }
//
//    copy {
//        from(rootProject.layout.projectDirectory.dir("../etc/data/datasets/airlines/csv"))
//        into(outdir.dir("examples/data/airlines"))
//    }
}

dependencies {
    implementation(project(":clients:mill-jdbc-driver"))
    implementation("sqlline:sqlline:1.12.0")
    runtimeOnly(libs.bundles.logging)
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
                }
            }
        }
    }
}
