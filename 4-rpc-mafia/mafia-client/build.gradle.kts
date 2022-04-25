import soa.conventions.Versions.grpcVersion

plugins {
    id("soa.application")
}

application {
    mainClass.set("me.darkkeks.soa.mafia.client.ClientAppKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":4-rpc-mafia:proto-kt"))

    implementation("io.grpc:grpc-netty:$grpcVersion")

    implementation("com.github.ajalt.clikt:clikt:3.4.0")

    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
}
