import com.microsoft.azure.plugin.functions.gradle.AzureFunctionsExtension
import com.microsoft.azure.plugin.functions.gradle.AzureFunctionsPlugin

plugins {
    application
    mill
    //id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.microsoft.azure.azurefunctions") version "1.11.0"
   // id ("com.gradleup.shadow") version "8.3.2"
    id("org.springframework.boot.experimental.thin-launcher") version "1.0.31.RELEASE"
}

/*springBoot {
    mainClass = "io.qpointz.mill.azure.functions.FunctionApplication"
}*/
//
//application {
//    mainClass = springBoot.mainClass
//    applicationName = "calcite-backend-service"
//}

//shadow {
    //archiveB
//}



mill {
    description = "Provides azure function to run jdbc backend"
}

tasks.withType<Jar> {
    manifest {
        attributes.put("Main-Class", "io.qpointz.mill.azure.functions.FunctionApplication")
    }
}


dependencies {
    implementation(project(":mill-common"))
    implementation(libs.protobuf.java.util)
    implementation(project(":mill-jdbc-service"))
    implementation(project(":mill-calcite-service"))
    implementation(project(":mill-common-service"))
    //implementation(project(":mill-common-backend-service"))
    implementation(libs.calcite.core)
    implementation(libs.boot.starter)
    implementation(libs.spring.cloud.function.adapter.azure)
    implementation(libs.h2.database)
}

val isntallDistTask = tasks.withType<Sync>().getByName("installDist")

azurefunctions {
    resourceGroup = "mill-calcite-backend-func-rg"
    appName = "mill-calcite-backend-func"
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
