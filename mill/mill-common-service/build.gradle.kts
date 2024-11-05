plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    mill
    `mill-publish`
}

mill {
    description = "Mill core library"
    publishToSonatype = true
}


dependencies {
    api(project(":mill-common"))
    api(project(":mill-common-security"))

    api(libs.boot.starter)
    api(libs.jackson.core)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)
    api(libs.jakarta.servlet.api)
    api(libs.javax.annotation.api)

    runtimeOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
    testImplementation(libs.boot.starter.test)
}


testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
