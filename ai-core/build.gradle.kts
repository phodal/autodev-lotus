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
    // LangChain4j for LLM integration
    implementation("dev.langchain4j:langchain4j:1.8.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.8.0")
    implementation("dev.langchain4j:langchain4j-anthropic:1.8.0")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:1.8.0")

    // Token counting with jtokkit (optional, for accurate token counting)
    compileOnly("com.knuddels:jtokkit:1.1.0")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

