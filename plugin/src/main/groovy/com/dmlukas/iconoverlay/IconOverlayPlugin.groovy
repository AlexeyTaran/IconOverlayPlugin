/**
 * Copyright 2016 Dale King
 * Also based on https://github.com/splatte/gradle-android-appiconoverlay
 * and https://github.com/akonior/icon-version
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dmlukas.iconoverlay

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The Plugin class called by gradle.
 */
class IconOverlayPlugin implements Plugin<Project> {
    // returns a positive integer if the two version strings are the same
    // returns a negative integer if str1 is less than str2
    // returns a positive integer if str1 is bigger than str2
    private static int versionCompare(String version1, String version2) {
        return [version1, version2]
                *.tokenize('.')
                *.collect { it as int }
                .with { v1, v2 ->
            [v1, v2].transpose()
                    .findResult(v1.size() <=> v2.size()) { a, b -> a <=> b ?: null }
        }
    }

    void apply(Project project) {
        // Abort if the project does not have the Android app plugin applied
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new IllegalStateException("'android' application plugin required.")
        }

        def log = project.logger

        // Add our config as an extension to all flavors
        project.android.productFlavors.whenObjectAdded
                { flavor ->
                    log.debug("Added iconOverlay configuration to flavor $flavor.name")
                    flavor.extensions.create("iconOverlay", IconOverlayConfig)
                }

        // Add our config as an extension to the existing build types (debug and release)
        project.android.buildTypes.all
                { buildType ->
                    log.debug("Added iconOverlay configuration to build type $buildType.name")
                    buildType.extensions.create("iconOverlay", IconOverlayConfig)
                }

        // Add our config default values as an extension to the defaultConfig
        log.debug("Added default iconOverlay configuration to defaultConfig")
        project.android.defaultConfig.extensions.add("iconOverlay", IconOverlayConfig.default)

        project.android.applicationVariants.all
                { BaseVariant variant ->

                    log.debug("iconOverlay processing variant $variant.name")

                    // Start with the values from the default config
                    IconOverlayConfig variantConfig = project.android.defaultConfig.iconOverlay

                    // Merge configuration from product flavors on top of it
                    for (int i = variant.productFlavors.size() - 1; i >= 0; i--) {
                        variantConfig = variant.productFlavors[i].iconOverlay.mergeOver(variantConfig)
                    }

                    // Merge the configuration from the build type
                    variantConfig = variant.buildType.iconOverlay.mergeOver(variantConfig)

                    // Only do something if it is enabled for this variant
                    if (variantConfig.enabled) {
                        Closure<String> text = variantConfig.text
                        text.resolveStrategy = Closure.DELEGATE_FIRST
                        text.delegate = variant

                        variant.outputs.each
                                { BaseVariantOutput output ->
                                    def overlayTask = project.task(type: OverlayTask, "iconOverlay${variant.name.capitalize()}")
                                            {
                                                // if version of android gradle plugin is 3.0 or more then use new syntax
                                                // else use the old syntax for backwards compatibility
                                                if (versionCompare(project.gradle.gradleVersion, "3.0") >= 0) {
                                                    resourcesPaths = output.processResources.inputResourcesDir
                                                    manifestFile = new File(output.processManifest.manifestOutputDirectory, "AndroidManifest.xml")
                                                } else {
                                                    resourcesPaths = project.files([output.processResources.resDir])
                                                    //noinspection GrDeprecatedAPIUsage
                                                    manifestFile = output.processManifest.manifestOutputFile
                                                }
                                                overlayText = text.call().toString()
                                                config = variantConfig
                                            }

                                    // hook overlay task into android build chain
                                    overlayTask.dependsOn(output.processManifest, variant.mergeResources)
                                    output.processResources.dependsOn overlayTask
                                }
                    }
                }
    }
}
