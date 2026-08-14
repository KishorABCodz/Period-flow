import java.util.Properties

plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.ai"

    // Load GEMINI_API_KEY from local.properties (never commit it).
    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    val geminiKey: String = localProps.getProperty("GEMINI_API_KEY", "")

    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
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
}
