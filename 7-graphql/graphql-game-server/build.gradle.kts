import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import soa.conventions.Versions.spqrStarterVersion

plugins {
    id("soa.spring-boot-app")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-Xemit-jvm-type-annotations"
    }
}

dependencies {
    implementation(project(":6-rest:common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("io.leangen.graphql:graphql-spqr-spring-boot-starter:$spqrStarterVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

