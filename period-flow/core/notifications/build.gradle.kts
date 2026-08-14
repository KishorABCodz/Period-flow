plugins {
    id("periodflow.android.library")
    id("periodflow.android.hilt")
}

android { 
    namespace = "com.periodflow.core.notifications" 
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    implementation(libs.kotlinx.datetime)
}
