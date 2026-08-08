#!/bin/bash
# Remove roborazzi plugin
sed -i '/alias(libs.plugins.roborazzi)/d' app/build.gradle.kts

# Remove testOptions
sed -i '/testOptions { unitTests { isIncludeAndroidResources = true } }/d' app/build.gradle.kts

# Remove test dependencies
sed -i '/testImplementation/d' app/build.gradle.kts
sed -i '/androidTestImplementation/d' app/build.gradle.kts
sed -i '/debugImplementation/d' app/build.gradle.kts

