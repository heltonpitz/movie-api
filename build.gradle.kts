plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "br.outsera"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")
	implementation("com.opencsv:opencsv:5.9")
	runtimeOnly("io.r2dbc:r2dbc-h2")
	runtimeOnly("com.h2database:h2")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Testes reativos
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
