plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.network"
}

dependencies {
    implementation(project(":core:common"))
}
