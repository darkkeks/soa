plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("com.google.protobuf") version "0.8.17" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
