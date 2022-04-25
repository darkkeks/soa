plugins {
    id("soa.kotlin-conventions")
    id("com.github.johnrengelman.shadow")
    application
}

tasks.build {
    dependsOn("shadowJar")
}
