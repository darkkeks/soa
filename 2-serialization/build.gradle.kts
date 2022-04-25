import soa.conventions.Versions.jacksonVersion

plugins {
    id("soa.application")
}

application {
    mainClass.set("me.darkkeks.soa.serialization.MainKt")
}

dependencies {
    implementation(project(":2-serialization:proto-kt"))

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.12.0")
}

