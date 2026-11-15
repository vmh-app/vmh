import mihon.gradle.tasks.GenerateLocalesConfigTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(mihonx.plugins.kotlin.multiplatform)
    alias(mihonx.plugins.spotless)

    alias(libs.plugins.moko.resources)
}

kotlin {
    android {
        namespace = "tachiyomi.i18n"

        // TODO(antsy): Remove when https://youtrack.jetbrains.com/issue/KT-83319 is resolved
        withHostTest { }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(libs.moko.resources)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

androidComponents {
    onVariants { variant ->
        val resSource = variant.sources.res ?: return@onVariants

        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val task = tasks.register<GenerateLocalesConfigTask>("generate${variantName}LocalesConfig") {
            // Solo los idiomas traducidos al 100 %: de este archivo salen el selector de la app
            // y el de Ajustes de Android. Las carpetas incompletas se quedan, pero no se ofrecen.
            val idiomasCompletos = setOf(
                "base", "ar", "de", "es", "fil", "fr", "in", "it", "ja", "ko",
                "pt-rBR", "ru", "tr", "vi", "zh-rCN", "zh-rTW",
            )
            localeFiles.from(
                layout.projectDirectory.dir("src/commonMain/moko-resources").asFileTree
                    .matching {
                        include("**/strings.xml", "**/plurals.xml")
                    }
                    .filter { it.parentFile.name in idiomasCompletos },
            )
        }
        resSource.addGeneratedSourceDirectory(task) { it.outputDir }
    }
}

multiplatformResources {
    resourcesPackage.set("tachiyomi.i18n")
}
