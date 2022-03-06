import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.6.10"

    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val lwjglVersion = "3.3.1"

val lwjglNatives: String = project.findProperty("lwjgl-natives") as? String
    ?: resolveLwjglNatives()

group = "me.darkkeks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("io.ktor:ktor-network:1.6.7")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")

    implementation("com.github.ajalt.clikt:clikt:3.4.0")

    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opus")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opus", classifier = lwjglNatives)
}

task("serverJar", ShadowJar::class) {
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.findByName("runtimeClasspath"))

    archiveClassifier.set("server")
    manifest {
        attributes["Main-Class"] = "server.ServerAppKt"
    }
}

task("clientJar", ShadowJar::class) {
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.findByName("runtimeClasspath"))

    archiveClassifier.set("client")
    manifest {
        attributes["Main-Class"] = "client.ClientAppKt"
    }
}

fun resolveLwjglNatives(): String {
    return listOf("os.name", "os.arch").map { System.getProperty(it)!! }.let { (name, arch) ->
        when {
            arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
                if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                    "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
                else
                    "natives-linux"
            arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
                "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            arrayOf("Windows").any { name.startsWith(it) } ->
                if (arch.contains("64"))
                    "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
                else
                    "natives-windows-x86"
            else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
        }
    }
}
