plugins {
    // El toolchain de Java de Nova (ADR-044): Java 25, Spring Boot con los starters de Nova, formato,
    // Checkstyle, cobertura, validación de commits, OWASP, el SBOM y la imagen.
    id("pe.edu.nova.java.spring-boot-service") version "3.0.0"
}

group = "pe.edu.nova.plaza"
version = findProperty("version") as String

val novaSecrets = "1.2.0"

dependencies {
    implementation("pe.edu.nova.java.starters:nova-observability-spring-boot-starter:3.0.1")
    implementation("pe.edu.nova.java.starters:nova-secrets-spring-boot-starter:$novaSecrets")
    runtimeOnly("pe.edu.nova.java.libs:nova-secrets-vault:$novaSecrets")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Los eventos de pedidos llegan por Kafka y se guardan en MongoDB (ADR-048).
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    testImplementation("org.testcontainers:testcontainers-mongodb")
    testImplementation("org.testcontainers:testcontainers-vault")
}
