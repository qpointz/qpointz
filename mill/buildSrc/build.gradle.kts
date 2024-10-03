plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("mill-publish") {
            id = "mill-publish"
            implementationClass = "io.qpointz.mill.plugins.MillPublishPlugin"
        }
        create("mill") {
            id = "mill"
            implementationClass = "io.qpointz.mill.plugins.MillPlugin"
        }
    }
}