plugins {
	id("soa.spring-boot-app")
}

dependencies {
	implementation(project(":6-rest:common"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}
