import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.protobuf
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.protobuf")
}

android {

    defaultConfig {
        applicationId = "programatorus.client"

        val compileSdkVer: String by getRootProject()
        compileSdkVersion = compileSdkVer

        val minSdkVersion: String by getRootProject()
        minSdk = minSdkVersion.toInt()

        val targetSdkVersion: String by getRootProject()
        targetSdk = targetSdkVersion.toInt()

        val appVersionCode: String by getRootProject()
        versionCode = appVersionCode.toInt()

        val appVersion: String by getRootProject()
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_1_9
    }

    sourceSets {
        getByName("main") {
            java {

            }
            proto {
                srcDir("src/main/proto")
                include("**/*.proto")
            }
        }
        getByName("test") {
            proto {
                srcDir("src/test/proto")
                include("**/*.proto")
            }
        }
    }
}

dependencies {
    val kotlinVersion: String by getRootProject()

    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:+")
    testImplementation("io.mockk:mockk:+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation(kotlin("stdlib-jdk7", kotlinVersion))
    implementation("com.google.protobuf:protobuf-kotlin:3.19.4")
}

val grpcVersion = "1.24.0"
protobuf.run {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }

    plugins {
        id("java") { artifact = "com.google.protobuf:protoc:3.19.4" }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins{
                create("java") {
                    outputSubDir = "java"
                }
            }
        }
    }
}

// From: https://github.com/google/protobuf-gradle-plugin/issues/540
fun com.android.build.api.dsl.AndroidSourceSet.proto(action: SourceDirectorySet.() -> Unit) {
    (this as? ExtensionAware)
        ?.extensions
        ?.getByName("proto")
        ?.let { it as? SourceDirectorySet }
        ?.apply(action)
}