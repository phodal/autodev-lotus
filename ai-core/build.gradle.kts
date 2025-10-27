plugins {
    kotlin("jvm")
}

group = "com.phodal.lotus"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Koog framework for LLM integration
    implementation("ai.koog:koog-agents:0.5.1")

    implementation("ai.koog:prompt-executor-deepseek-client:0.5.1")
    implementation("ai.koog:prompt-executor-openai-client:0.5.1")
    implementation("ai.koog:prompt-executor-anthropic-client:0.5.1")
    implementation("ai.koog:prompt-executor-google-client:0.5.1")

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

