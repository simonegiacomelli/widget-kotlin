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