plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
    id("periodflow.android.compose") // If UI needed
}
android { namespace = "com.periodflow.core.security" }
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.biometric)
}
