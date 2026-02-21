/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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
    id("mybus.android-library")
    id("mybus.hilt-convention")
}

android {
    namespace = "uk.org.rivernile.android.bustracker.ui.favouritestops.remove"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

kotlin {
    explicitApi()
}

dependencies {

    implementation(project(":core:core-domain-android"))
    implementation(project(":core:coroutines-android"))
    implementation(project(":core:favourites-android"))
    implementation(project(":ui:ui-core"))

    // AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.viewmodel)

    // Material Design
    implementation(libs.material)

    // Test dependencies
    androidTestImplementation(libs.androidx.test.runner)

    testImplementation(testFixtures(project(":core:favourites")))
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
}
