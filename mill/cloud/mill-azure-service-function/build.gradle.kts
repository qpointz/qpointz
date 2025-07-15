import com.microsoft.azure.plugin.functions.gradle.AzureFunctionsExtension

plugins {
    application
    mill
    alias(libs.plugins.spring.dependency.management)
    id("com.microsoft.azure.azurefunctions") version "1.11.0"
    id("org.springframework.boot.experimental.thin-launcher") version "1.0.31.RELEASE"
}

mill {
    description = "Provides azure function to run jdbc backend"
}

tasks.withType<Jar> {
    manifest {
        attributes.put("Main-Class", "io.qpointz.mill.azure.functions.FunctionApplication")
    }
}


dependencies {
    implementation(libs.protobuf.java.util)
    implementation(project(":mill-starter-backends"))
    implementation(project(":mill-common-security"))
    implementation(libs.calcite.core)
    implementation(libs.boot.starter)
    implementation(libs.spring.cloud.function.adapter.azure)
    implementation(libs.boot.starter.test)
    implementation(libs.h2.database)
}

val isntallDistTask = tasks.withType<Sync>().getByName("installDist")

azurefunctions {
    resourceGroup = "mill-calcite-backend-func-rg"
    appName = "mill-service-func"
    region = "westus"
    appServicePlanName = "java-functions-app-service-plan"
    pricingTier = "EP1"
    setRuntime(closureOf<com.microsoft.azure.gradle.configuration.GradleRuntimeConfig> {
        os("Linux")
        javaVersion("17")
    })
    setAuth(closureOf<com.microsoft.azure.gradle.auth.GradleAuthConfig> {
        type = "azure_cli"
    })

    val mainClass = application.mainClass

    setAppSettings(closureOf<MutableMap<String, String>> {
        put("FUNCTIONS_EXTENSION_VERSION" ,"~4")
        put("FUNCTIONS_WORKER_RUNTIME", "java")
        put("WEBSITE_RUN_FROM_PACKAGE" , "1")
        if (mainClass.isPresent) {
            put("MAIN_CLASS", mainClass.get())
        }
    })
    localDebug = "transport=dt_socket,server=y,suspend=n,address=5005"
}

tasks.getByName("azureFunctionsPackage") {
    doLast {
        val ext = extensions.findByType(AzureFunctionsExtension::class)
        if (ext!=null) {
            copy {
                from(isntallDistTask.outputs.files)
                into(project.layout.buildDirectory.dir("azure-functions/${ext.appName}/lib"))
            }
        }
    }
}.dependsOn(isntallDistTask)

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
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
