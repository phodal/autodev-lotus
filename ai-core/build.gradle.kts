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
    implementation(libs.langchain4j)
    implementation(libs.langchain4jOpenai)
    implementation(libs.langchain4jAnthropic)
    implementation(libs.langchain4jGoogleaigemini)

    // Token counting with jtokkit (optional, for accurate token counting)
    compileOnly(libs.jtokkit)

    // Kotlin coroutines
    implementation(libs.kotlinxCoroutinesCore)

    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

