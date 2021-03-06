import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import soa.conventions.Versions.cliktVersion
import soa.conventions.Versions.jacksonVersion
import soa.conventions.Versions.log4jVersion

plugins {
    id("soa.kotlin-conventions")
    id("com.github.johnrengelman.shadow")
}

val lwjglVersion = "3.3.1"

val lwjglNatives: String = project.findProperty("lwjgl-natives") as? String
    ?: resolveLwjglNatives()

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("io.ktor:ktor-network:1.6.7")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opus")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opus", classifier = lwjglNatives)
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

val serverJar = task("serverJar", ShadowJar::class) {
    archiveClassifier.set("server")
    from(sourceSets.main.get().output)
    configurations.add(project.configurations.runtimeClasspath.get())
    manifest { attributes["Main-Class"] = "me.darkkeks.soa.voicechat.server.ServerAppKt" }
}

val clientJar = task("clientJar", ShadowJar::class) {
    archiveClassifier.set("client")
    from(sourceSets.main.get().output)
    configurations.add(project.configurations.runtimeClasspath.get())
    manifest { attributes["Main-Class"] = "me.darkkeks.soa.voicechat.client.ClientAppKt" }
}

tasks.build {
    dependsOn.add(serverJar)
    dependsOn.add(clientJar)
}
