This is a clone of the [repository](https://gitlab.com/NobleworksSoftware/icon-overlay-plugin), for customization and support.

# IconOverlayPlugin
This is an Android gradle plugin that allows you to overlay text on top of an android application's
icon to easily convey information about the app to testers, such as version or flavor.

This plugin is available from the gradle plugin portal. Build script snippet for use in all Gradle versions:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.IconOverlayPlugin:plugin:1.0"
  }
}

apply plugin: "com.dmlukas.iconoverlay"
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:

```
plugins {
   id "com.dmlukas.iconoverlay" version "1.0"
}
```

By default applying the plugin does nothing. You have to configure it to be enabled. Configuration
is done in one or more iconOverlay blocks:

```
iconOverlay {
    enabled = true
    fontSize = 6
    textColor = [0, 255, 0, 255] // [r, g, b, a]
    verticalLinePadding = 2 // vertical gap between each line of text
    backgroundOverlayColor = [255, 0, 0, 255]  // [r, g, b, a]
    text = { "$flavorName\n$versionName\n$versionCode" }
    position = 'top' // 'top' or 'bottom'
}
```

This block can be placed in the following parts of the android configuration of the
build.gradle. They are listed in order from lowest priority to highest priority.
Higher priority overrides lower priority configuration just as in Android.

 * defaultConfig
 * product flavor
 * buildType (e.g. debug or release)

For example, given the following in your build.gradle will mean an overlay will only
be enabled on debug builds and will by default have a green tinted background, but
for staging builds will have a blue background.


 ```groovy
 android {
     defaultConfig {
         iconOverlay {
             backgroundOverlayColor = [0, 128, 0, 128]
         }
     }

     productFlavors {
         staging {
             iconOverlay {
                 backgroundOverlayColor = [0, 0, 128, 128]
             }
         }
         production {
             ...
         }
     }

     buildTypes {
         debug {
             iconOverlay { enabled = true }
         }
     }
 }
 ```

The configurable properties are as follows:

| property | explanation |
| --- | --- |
| enabled | boolean property that controls whether the overlay is enabled. Defaults to false. |
| fontSize | size of the font to use in DIPs (based on MDPI resolution 48x48 icon). Defaults to 8 |
| verticalLinePadding | vertical gap to add between lines of text in DIPs (MDPI resolution). Defaults to 2 |
| textColor | The color of the text, expressed as 4 element array for r, g, b, and alpha. Defaults to full white [255, 255, 255, 255] |
| backgroundOverlayColor | The color of the background overlay, expressed as 4 element array for r, g, b, and alpha. Defaults to [0, 0, 0, 136] |
| text | Specifies the text to overlay. This is specified as a closure that returns a string. The delegate for the closure is ApplicationVariant object so you can use properties of the variant to build the string. Separate lines using \n. Default is { "$flavorName $buildType.name\n$versionName" } |

License
--------

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

