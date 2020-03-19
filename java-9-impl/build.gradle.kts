plugins {
    java
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = sourceCompatibility
}