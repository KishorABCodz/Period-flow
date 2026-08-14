plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android {
    namespace = "com.periodflow.core.datastore"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.datastore.preferences)
}
