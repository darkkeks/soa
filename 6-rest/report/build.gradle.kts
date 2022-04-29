plugins {
	id("soa.spring-boot-app")
}

dependencies {
	implementation(project(":6-rest:common"))
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}
