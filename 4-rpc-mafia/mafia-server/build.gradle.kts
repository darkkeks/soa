plugins {
    kotlin("jvm")
}

group = "me.darkkeks.soa.mafia.client"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":proto-kt"))
}
