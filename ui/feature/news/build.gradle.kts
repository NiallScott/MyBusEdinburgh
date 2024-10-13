/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 *
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "uk.org.rivernile.android.bustracker.ui.news"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "city"

    productFlavors {
        create("edinburgh") {
            dimension = "city"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    "edinburghImplementation"(project(":core:alphanumcomparator"))
    implementation(project(":core:coroutines-android"))
    implementation(project(":core:logging"))
    implementation(project(":core:services-android"))
    "edinburghImplementation"(project(":cities:edinburgh:edinburgh-service-updates-android"))
    implementation(project(":ui:ui-core"))

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.viewmodel.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.material.compose)

    // Date/time
    implementation(libs.kotlin.datetime)

    // Test dependencies
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlin.test.junit)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.turbine)
}