buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.2.2" apply false
    id("com.android.library") version "7.2.2" apply false
}

extra.apply {
    set("compileSdkVersion", 30)
    set("buildToolsVersion", "29.0.2")
    set("minSdkVersion", 22)
    set("targetSdkVersion", 32)
    set("versionCode", 248)
    set("versionName", "1.0.106")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}