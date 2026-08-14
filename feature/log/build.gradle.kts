plugins {
    id("periodflow.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.periodflow.feature.log"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:ai"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
