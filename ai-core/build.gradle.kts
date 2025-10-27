plugins {
    kotlin("jvm")
}

group = "com.phodal.lotus"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Koog framework for LLM integration
    implementation("ai.koog:koog-agents:0.1.0")

    // Kotlin coroutines
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

