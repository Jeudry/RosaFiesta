import com.android.build.gradle.LibraryExtension
import com.example.convention.addUiLayerDependencies
import com.example.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureUiConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("rosafiesta.android.library.compose")
            }

            dependencies {
                addUiLayerDependencies(target)
            }
        }
    }
}