plugins {
    scala
    jacoco
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

dependencies {
    compileOnly(libs.scala.lang)
    implementation(libs.apache.spark.core)
    implementation(libs.apache.spark.sql)
    /*implementation(libs.javax.annotation.api)
    api(libs.boot.starter.security)
    api(libs.boot.starter.security.oauth2.client)
    api(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.jakarta.servlet.api)
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)*/
}

testing {
    suites {
        /*register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }*/

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    /*implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)*/
                }
            }
        }
    }
}
