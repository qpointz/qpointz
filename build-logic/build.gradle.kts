plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
}

gradlePlugin {
    plugins {
        create("mill-publish") {
            id = "io.qpointz.plugins.mill-publish"
            implementationClass = "io.qpointz.mill.plugins.MillPublishPlugin"
        }
        create("mill") {
            id = "io.qpointz.plugins.mill"
            implementationClass = "io.qpointz.mill.plugins.MillPlugin"
        }
    }
}