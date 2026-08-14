plugins {
    id("periodflow.android.library")
    id("periodflow.android.compose")
}

android {
    namespace = "com.periodflow.core.ui"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(project(":core:domain"))
    implementation(libs.compose.material.icons.extended)
    api(libs.coil.compose)
}
