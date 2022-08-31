import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.jevzo"
version = "0.0.1"
description = "limitcloud-slave"

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":limitcloud-library"))
}

application {
    mainClass.set("com.jevzo.limitcloud.slave.bootstrap.SlaveBootstrapKt")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }

    shadowJar
}