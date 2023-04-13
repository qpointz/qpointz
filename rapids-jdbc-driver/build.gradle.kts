plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    java
}

shadow {
    //archiveBaseName("lala")
}



dependencies {
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)

    implementation(libs.avatica.core)
    implementation(libs.avatica.server)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}