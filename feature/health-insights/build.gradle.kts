plugins {
    id("periodflow.android.feature")
    id("periodflow.android.compose")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.feature.health_insights"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:health-analysis"))
    implementation(project(":core:export"))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
}
