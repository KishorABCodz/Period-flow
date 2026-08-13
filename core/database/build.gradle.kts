plugins {
    id("periodflow.android.library")
    id("periodflow.android.room")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.database"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
