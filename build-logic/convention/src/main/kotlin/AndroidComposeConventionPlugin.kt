import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val commonExtension = extensions.findByType(CommonExtension::class.java)
            commonExtension?.apply {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val bom = project.dependencies.platform(
                    project.dependencies.create("androidx.compose:compose-bom:2024.05.00")
                )
                add("implementation", bom)
                add("androidTestImplementation", bom)
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("implementation", "androidx.compose.material3:material3")
                add("implementation", "androidx.compose.foundation:foundation")
                add("implementation", "androidx.compose.animation:animation")
                add("implementation", "androidx.compose.material3:material3-window-size-class")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
            }
        }
    }
}
