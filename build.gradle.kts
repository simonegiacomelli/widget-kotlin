plugins {
    kotlin("js") version "1.5.10"
}

group = "pro.jako"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(LEGACY) { //this make tests work
//    js() { //this make source maps works :D
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}