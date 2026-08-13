plugins {
    id("periodflow.android.application")
    id("periodflow.android.compose")
    id("periodflow.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.periodflow.app"

    defaultConfig {
        applicationId = "com.periodflow.app"
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:ui"))
    implementation(project(":core:security"))

    // Feature modules
    implementation(project(":feature:home"))
    implementation(project(":feature:log"))
    implementation(project(":feature:stats"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:health-insights"))

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.splash.screen)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Debug
    debugImplementation(libs.leakcanary)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
}
