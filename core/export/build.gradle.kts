plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.export"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
}
