plugins {
    id("periodflow.android.library")
}

android {
    namespace = "com.periodflow.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation("androidx.fragment:fragment-ktx:1.8.0")
    implementation("javax.inject:javax.inject:1")
    // Coroutines (transitively from common)
}
