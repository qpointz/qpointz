plugins {
    java
    jacoco
}

dependencies {
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.avatica.core)
    implementation(libs.avatica.server)
    implementation(libs.parquet.avro)
    implementation(libs.avro)
    implementation(libs.avro.mapred)
    implementation(libs.hadoop.common) {
        exclude("javax.ws.rs")
    }
    implementation(libs.hadoop.client) {
        exclude("javax.ws.rs")
    }

    implementation(libs.azure.storage.file.datalake)
    implementation(libs.azure.storage.blob.nio)

    implementation(libs.lombok)
    testImplementation(project(mapOf("path" to ":rapids-core")))
    annotationProcessor(libs.lombok)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.2")
        }


        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter("5.9.2")
            testType.set(TestSuiteType.INTEGRATION_TEST)

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}