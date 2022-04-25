import com.google.protobuf.gradle.*

import soa.conventions.Versions.protobufVersion

plugins {
    id("soa.kotlin-conventions")
    id("com.google.protobuf")
}

repositories {
    google()
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
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                id("kotlin")
            }
        }
    }
}
