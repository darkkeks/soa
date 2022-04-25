import com.google.protobuf.gradle.*
import soa.conventions.Versions.grpcKotlinVersion
import soa.conventions.Versions.grpcVersion
import soa.conventions.Versions.protobufVersion

plugins {
    id("soa.proto")
}

dependencies {
    api(kotlin("stdlib"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    api("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
}

protobuf {
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion" }
        id("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar" }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
