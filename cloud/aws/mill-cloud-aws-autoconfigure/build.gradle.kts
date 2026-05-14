plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill cloud — AWS S3 Spring Boot autoconfiguration"
    publishArtifacts = true
}

dependencies {
    api(project(":cloud:aws:mill-cloud-aws-blob"))
    implementation(libs.boot.starter)
    compileOnly(libs.aws.sdk.s3)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}
