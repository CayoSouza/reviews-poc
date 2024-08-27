plugins {
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
}

group = "com.ifood"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Dependências do Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA para persistência de dados
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL Driver para conexão com o banco de dados
    implementation("org.postgresql:postgresql")

    // Comunicação com API Druid via RestTemplate
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Dependências do Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Dependências de Testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.18.0")
    testImplementation("org.testcontainers:mongodb:1.18.0")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:postgresql:1.18.0")
    testImplementation("org.testcontainers:kafka:1.18.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Suporte para testes com Kafka embutido
    testImplementation("org.springframework.kafka:spring-kafka-test")

    implementation("org.flywaydb:flyway-core")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
