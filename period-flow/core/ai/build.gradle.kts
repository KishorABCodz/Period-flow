import java.util.Properties

plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.ai"

    // Load GEMINI_API_KEY + GEMMA_MODEL_URL from local.properties (never commit them).
    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    val geminiKey: String = localProps.getProperty("GEMINI_API_KEY", "")
    val gemmaUrl: String = localProps.getProperty("GEMMA_MODEL_URL", "")

    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "GEMMA_MODEL_URL", "\"$gemmaUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.gemini.generativeai)
    implementation(libs.mediapipe.tasks.genai)
}
