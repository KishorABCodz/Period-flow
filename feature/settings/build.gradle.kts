plugins {
    id("periodflow.android.feature")
}

android {
    namespace = "com.periodflow.feature.settings"
}

dependencies {
    implementation(project(":core:datastore"))
}
