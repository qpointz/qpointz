plugins {
    application
    mill
}

mill {
    publishToSonatype = false
}

application {
    mainClass = "sqlline.SqlLine"
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
