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
package com.nobleworks_software

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

class OverlayTask extends DefaultTask {
    File manifestFile
    FileCollection resourcesPaths
    String overlayText
    IconOverlayConfig config

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    def overlay() {
        IconUtils.findIcons(resourcesPaths, manifestFile).each
                { file ->
                    logger.debug("found file: ${file}")

                    IconUtils.addTextToImage(file, config, overlayText.split("\n"))
                }
    }
}
