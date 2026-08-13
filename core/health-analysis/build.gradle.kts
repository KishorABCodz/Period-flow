plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}
android { namespace = "com.periodflow.core.health_analysis" }
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(libs.kotlinx.datetime)
}
