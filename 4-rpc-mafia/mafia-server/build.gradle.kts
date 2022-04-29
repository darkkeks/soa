import soa.conventions.Versions.cliktVersion
import soa.conventions.Versions.grpcVersion
import soa.conventions.Versions.log4jVersion

plugins {
    id("soa.application")
}

application {
    mainClass.set("me.darkkeks.soa.mafia.server.ServerAppKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":4-rpc-mafia:proto-kt"))

    implementation("io.grpc:grpc-netty:$grpcVersion")

    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
}
