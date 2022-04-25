import soa.conventions.Versions.protobufVersion

plugins {
    id("soa.proto")
}

dependencies {
    api(kotlin("stdlib"))
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    api("com.google.protobuf:protobuf-kotlin:$protobufVersion")
}
