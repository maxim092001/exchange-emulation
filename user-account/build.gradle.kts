plugins {
    kotlin("jvm") version "1.7.10"
}

group = "maximgran"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-netty:1.6.7")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
}

tasks.test {
    useJUnitPlatform()
}