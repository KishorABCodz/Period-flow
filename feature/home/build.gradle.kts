plugins {
    id("periodflow.android.feature")
}

android {
    namespace = "com.periodflow.feature.home"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:domain"))
    implementation(project(":core:health-analysis"))
    implementation(libs.kotlinx.datetime)
}
