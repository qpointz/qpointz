plugins {
    base
    id("jacoco-report-aggregation")
    id("io.qpointz.plugins.mill-aggregate")
    id("org.jetbrains.dokka")
}

dependencies {
    dokka(project(":ai:mill-ai-v1-core"))
    dokka(project(":ai:mill-ai-v1-nlsql-chat-service"))
    dokka(project(":ai:mill-ai-v3"))
    dokka(project(":ai:mill-ai-v3-data"))
    dokka(project(":ai:mill-ai-v3-test"))
    dokka(project(":ai:mill-ai-v3-cli"))
    dokka(project(":ai:mill-ai-v3-persistence"))
    dokka(project(":ai:mill-ai-v3-autoconfigure"))
    dokka(project(":ai:mill-ai-v3-service"))
}
