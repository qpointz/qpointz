plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
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