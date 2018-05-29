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

class IconOverlayConfig
{
    static final String POSITION_BOTTOM = 'bottom'
    static final String POSITION_TOP = 'top'

    static IconOverlayConfig getDefault()
    {
        return new IconOverlayConfig(
                enabled: false,
                fontSize: 8,
                verticalLinePadding: 2,
                backgroundOverlayColor: [0, 0, 0, 136],
                textColor: [255, 255, 255, 255],
                text: { "$flavorName $buildType.name\n$versionName" },
                position: POSITION_BOTTOM)
    }

    IconOverlayConfig mergeOver(IconOverlayConfig base)
    {
        return new IconOverlayConfig(
                enabled: choose(enabled, base.enabled),
                fontSize: choose(fontSize, base.fontSize),
                verticalLinePadding: choose(verticalLinePadding, base.verticalLinePadding),
                backgroundOverlayColor: choose(backgroundOverlayColor, base.backgroundOverlayColor),
                textColor: choose(textColor, base.textColor),
                text: choose(text, base.text),
                position: choose(position, base.position))
    }

    private static <T> T choose(T override, T base)
    {
        return override == null ? base : override
    }

    private static int choose(int override, int base)
    {
        return override < 0 ? base : override
    }

    /**
     * Whether the plugin is enabled
     */
    public Boolean enabled = null

    /**
     * The text to display
     */
    public Closure<String> text = null

    /**
     * The size of the text to draw on top of the icon
     */
    public int fontSize = -1

    /**
     * The amount of vertical space between each line of text
     */
    public int verticalLinePadding = -1

    /**
     * Text color to draw behind the text
     */
    public int[] backgroundOverlayColor = null

    /**
     * The text color to draw the overlaid text.
     */
    public int[] textColor = null

    /**
     * The position of the overlay
     */
    public String position = null
}
