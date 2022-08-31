group = "com.jevzo"
version = "0.0.1"
description = "limitcloud"

plugins {
    kotlin("jvm") version "1.7.10"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    dependencies {
        api(group = "org.jetbrains.kotlin", name = "kotlin-script-util", version = "1.7.10")
        api(group = "org.jetbrains.kotlin", name = "kotlin-compiler", version = "1.7.10")
        api(group = "org.jetbrains.kotlin", name = "kotlin-scripting-compiler", version = "1.7.10")

        implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.7.10")
    }
}