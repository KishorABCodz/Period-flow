import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for feature modules. Applies:
 * - Android Library
 * - Compose
 * - Hilt
 * - Common feature dependencies (Navigation, Lifecycle, Hilt Navigation)
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("periodflow.android.library")
                apply("periodflow.android.compose")
                apply("periodflow.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:ui"))

                add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
                add("implementation", "androidx.hilt:hilt-navigation-compose:1.2.0")
                add("implementation", "androidx.navigation:navigation-compose:2.9.8")
                add("implementation", "androidx.compose.material:material-icons-extended")
                add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
    }
}
