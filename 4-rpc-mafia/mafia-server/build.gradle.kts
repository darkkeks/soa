plugins {
    kotlin("jvm")

    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val grpcVersion: String by project

group = "me.darkkeks.soa.mafia.client"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("me.darkkeks.soa.mafia.server.ServerAppKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":proto-kt"))

    implementation("io.grpc:grpc-netty:$grpcVersion")

    implementation("com.github.ajalt.clikt:clikt:3.4.0")

    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
}
