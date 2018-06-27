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

import com.google.common.collect.Lists
import groovy.io.FileType
import org.gradle.api.file.FileCollection

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON

/**
 * Various utility methods to find and draw the information on the app icon
 */
class IconUtils {
    static
    {
        // We want our font to come out looking pretty
        System.setProperty("awt.useSystemAAFontSettings", "on")
        System.setProperty("swing.aatext", "true")

        // Fix for Android Studio issue: Could not find class: apple.awt.CGraphicsEnvironment
        try {
            Class.forName(System.getProperty("java.awt.graphicsenv"))
        }
        catch (ClassNotFoundException e) {
            System.err.println("[WARN] java.awt.graphicsenv: " + e)
            System.setProperty("java.awt.graphicsenv", "sun.awt.CGraphicsEnvironment")
        }

        //  Fix for AS issue: Toolkit not found: apple.awt.CToolkit
        try {
            Class.forName(System.getProperty("awt.toolkit"))
        }
        catch (ClassNotFoundException e) {
            System.err.println("[WARN] awt.toolkit: " + e)
            System.setProperty("awt.toolkit", "sun.lwawt.macosx.LWCToolkit")
        }
    }

    /**
     * Icon name to search for in the app drawable folders
     * if none can be found in the app manifest
     */
    static final String DEFAULT_ICON_NAME = "ic_launcher"

    /**
     * Retrieve the app icon from the application manifest
     *
     * @param manifestFile The file pointing to the AndroidManifest
     * @return The icon name specified in the {@code <application/ >} node
     */
    static String getIconName(File manifestFile) {
        if (manifestFile == null || manifestFile.directory || !manifestFile.exists()) {
            return null
        }

        def manifestXml = new XmlSlurper().parse(manifestFile)
        def fileName = manifestXml?.application?.@'android:icon'?.text()
        return fileName ? fileName?.split("/")[1] : null
    }

    /**
     * Finds all icon files matching the icon specified in the given manifest.
     *
     * If no icon can be found in the manifest, a default of {@link IconUtils#DEFAULT_ICON_NAME} will be used
     */
    static List<File> findIcons(FileCollection paths, File manifest) {
        String iconName = getIconName(manifest) ?: DEFAULT_ICON_NAME
        List<File> result = Lists.newArrayList()

        paths.each
            { path ->
                if (path.directory) {
                    path.eachDirMatch(~/^drawable.*|^mipmap.*/)
                            { dir ->
                                dir.eachFileMatch(FileType.FILES, ~/(?i)^${iconName}.*.png/)
                                        { file ->
                                            result.add(file)
                                        }
                            }
                }
            }

        return result
    }

    private static Color intArrayToColor(int[] colorParts) {
        if (colorParts == null || colorParts.length != 4) {
            return new Color(0, 0, 0, 0)
        } else {
            return new Color(colorParts[0], colorParts[1], colorParts[2], colorParts[3])
        }
    }

    static BufferedImage convert(BufferedImage src, int bufImgType) {
        BufferedImage img = new BufferedImage(src.width, src.height, bufImgType)
        Graphics2D g2d = img.createGraphics()
        g2d.drawImage(src, 0, 0, null)
        g2d.dispose()
        return img
    }

    /**
     * Draws the given text over an image
     *
     * @param image The image file which will be written too
     * @param config The configuration which controls how the overlay will appear
     * @param lines The lines of text to be displayed
     */
    static void addTextToImage(File image, IconOverlayConfig config, String... lines) {
        final BufferedImage bufferedImage = convert(ImageIO.read(image), BufferedImage.TYPE_INT_ARGB)

        final Color backgroundOverlayColor = intArrayToColor(config.backgroundOverlayColor)
        final Color textColor = intArrayToColor(config.textColor)
        final float imgWidth = bufferedImage.width
        final float imgHeight = bufferedImage.width

        final float scale = imgWidth / 48
        final float fontSize = config.fontSize * scale
        final float linePadding = config.verticalLinePadding * scale
        final int lineCount = lines.length
        final float totalLineHeight = (fontSize + linePadding + 1) * lineCount
        final boolean isTopAligned = IconOverlayConfig.POSITION_TOP.equalsIgnoreCase(config.position)

        // Bottom aligned text is drawn from bottom up with lines reversed
        String[] linesToDraw
        if (!isTopAligned) {
            linesToDraw = lines.reverse()
        } else {
            linesToDraw = lines
        }

        GraphicsEnvironment.localGraphicsEnvironment.createGraphics(bufferedImage).with
                { g ->
                    g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)

                    // Draw our background overlay
                    g.color = backgroundOverlayColor
                    int backgroundOverlayY = isTopAligned ? 0 : (int) imgHeight - (int) totalLineHeight
                    g.fillRect(0, backgroundOverlayY, (int) imgWidth, (int) totalLineHeight)

                    // Draw each line of our text
                    g.font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize)
                    g.color = textColor
                    linesToDraw.eachWithIndex
                            { String line, int i ->
                                final int strWidth = g.fontMetrics.stringWidth(line)

                                int x = 0
                                if (imgWidth >= strWidth) {
                                    x = ((imgWidth - strWidth) / 2)
                                }

                                int y = imgHeight - (fontSize * i) - ((i + 1) * linePadding)
                                if (isTopAligned) {
                                    y = (fontSize * (i + 1)) + (i * linePadding)
                                }

                                g.drawString(line, x, y)
                            }
                }

        ImageIO.write(bufferedImage, "png", image)
    }
}
