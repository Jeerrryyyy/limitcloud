import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.jevzo"
version = "0.0.1"
description = "limitcloud-library"

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    api(group = "com.google.code.gson", name = "gson", version = "2.9.1")
    api(group = "io.netty", name = "netty-all", version = "4.1.79.Final")
    api(group = "org.kodein.di", name = "kodein-di-jvm", version = "7.14.0")
    api(group = "javax.activation", name = "activation", version = "1.1.1")
    api(group = "ch.qos.logback", name = "logback-classic", version = "1.3.0-beta0")
    api(group = "org.bouncycastle", name = "bcpkix-jdk15on", version = "1.70")
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