plugins {
    kotlin("js") version "1.5.30"
}

group = "pro.jako"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(IR) { //this make tests work
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}