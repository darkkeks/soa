import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.6.21")

    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")

    implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.18")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.6.7")
    implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
}
