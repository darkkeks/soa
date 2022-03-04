import com.google.protobuf.gradle.*

val protobuf_version: String by project

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.17"
    idea
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    api(kotlin("stdlib"))

    api("com.google.protobuf:protobuf-java-util:$protobuf_version")
    api("com.google.protobuf:protobuf-kotlin:$protobuf_version")
}

sourceSets {
    main {
        proto {
            srcDir("../proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobuf_version"
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.builtins {
                id("kotlin")
            }
        }
    }
}
