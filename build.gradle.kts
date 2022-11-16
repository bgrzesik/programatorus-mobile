buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath(kotlin("gradle-plugin", "+"))
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
    repositories {
        mavenCentral()
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val androidPluginsVersion: String by project

    id("com.android.application") version androidPluginsVersion apply false
    id("com.android.library") version androidPluginsVersion apply false
    id("com.google.protobuf") version "0.9.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}