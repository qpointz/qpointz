plugins {
    base
    id("jacoco-report-aggregation")
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":ai:mill-ai"))
    dokka(project(":ai:mill-ai-data"))
    dokka(project(":ai:mill-ai-test"))
    dokka(project(":ai:mill-ai-cli"))
    dokka(project(":ai:mill-ai-persistence"))
    dokka(project(":ai:mill-ai-autoconfigure"))
    dokka(project(":ai:mill-ai-service"))
}
