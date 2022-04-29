plugins {
    id("soa.spring-boot-app")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jdbc")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
