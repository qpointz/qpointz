plugins {
    application
    mill
    libs.plugins.spring.dependency.management
    //libs.plugins.spring.boot
    id("com.microsoft.azure.azurefunctions") version "1.11.0"
}
apply(plugin = "com.microsoft.azure.azurefunctions")

//springBoot {
//    mainClass = "io.qpointz.mill.services.CalciteMillService"
//}
//
//application {
//    mainClass = springBoot.mainClass
//    applicationName = "calcite-backend-service"
//}

mill {
    description = "Provides azure function to run jdbc backend"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.qpointz.mill.azure.functions.FunctionApplication"
    }
}


dependencies {
    implementation(project(":mill-common"))
    implementation(project(":mill-common-service"))
    implementation(project(":mill-calcite-service"))
    /*implementation(project(":calcite-backend-service"))
    implementation(project(":common-backend-service"))*/
    implementation(libs.boot.starter)
    implementation(libs.spring.cloud.function.adapter.azure)
    //implementation(libs.spring.cloud.function.context)
    //implementation(libs.spring.cloud.function.grpc)
    //implementation(libs.spring.cloud.starter.function.web)
}


azurefunctions {
    resourceGroup = "java-functions-group"
    appName = "scff-azure-gradle-sample"
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
    setAppSettings(closureOf<MutableMap<String, String>> {
        put("FUNCTIONS_EXTENSION_VERSION" ,"~4")
        put("FUNCTIONS_WORKER_RUNTIME", "java")
        put("WEBSITE_RUN_FROM_PACKAGE" , "1")
    })
    localDebug = "transport=dt_socket,server=y,suspend=n,address=5005"
}