import java.util.Properties
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
    maven
    `maven-publish`
}

run {
    val props = Properties()
    rootDir.listFiles { file -> file.extension == "properties" && file.nameWithoutExtension != "gradle" }
        ?.forEach {
            println("Loading ${it.name}...")
            it.inputStream().use {
                props.load(it)
            }
        }
    props.forEach {
        project.ext[it.key.toString()] = it.value
    }
}

group = "com.proximyst.fieldmod"
version = "0.1.0"

dependencies {
    implementation(project(":java-9-impl"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

tasks.withType<ShadowJar> {
    this.archiveClassifier.set(null as String?)
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

val javadocJar = tasks.create<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("java") {
            project.shadow.component(this)
            artifact(javadocJar)
            artifact(sourcesJar)
        }
    }
    repositories {
        maven {
            name = "proxi-nexus"
            url = uri("https://nexus.proximyst.com/repository/maven-any/")
            credentials {
                val proxiUser: String? by project
                val proxiPassword: String? by project
                username = proxiUser
                password = proxiPassword
            }
        }
    }
}