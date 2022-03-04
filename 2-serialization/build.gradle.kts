import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jackson_version: String by project

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "me.darkkeks"
version = "1.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":proto-kt"))

    implementation("com.fasterxml.jackson.core:jackson-core:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

    implementation("org.apache.commons:commons-lang3:3.12.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

tasks.build {
    dependsOn("shadowJar")
}
