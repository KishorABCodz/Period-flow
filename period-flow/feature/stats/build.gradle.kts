plugins {
    id("periodflow.android.feature")
}

android {
    namespace = "com.periodflow.feature.stats"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.vico.compose)
    implementation(libs.kotlinx.datetime)
}
