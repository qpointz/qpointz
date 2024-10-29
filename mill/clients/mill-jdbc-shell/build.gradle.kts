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
    implementation(libs.sqlline)
    runtimeOnly(libs.bundles.logging)
    runtimeOnly(libs.bundles.jdbc.pack)
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
