plugins {
    id("periodflow.android.feature")
    id("periodflow.android.compose")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.feature.onboarding"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
}
