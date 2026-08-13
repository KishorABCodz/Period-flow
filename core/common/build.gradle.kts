plugins {
    id("periodflow.android.library")
}

android {
    namespace = "com.periodflow.core.common"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.coroutines.test)
}
