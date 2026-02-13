/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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
    namespace = "uk.org.rivernile.android.bustracker.core.edinburgh"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "BUSTRACKER_API_KEY",
            "\"${project.findProperty("mybus.edinburgh.bustracker.apiKey") ?: "undefined"}\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(project(":cities:edinburgh:edinburgh-core"))
    implementation(project(":core:http-core-android"))
    implementation(project(":endpoint:tracker-endpoint"))

    // Testing dependencies
    androidTestImplementation(libs.androidx.test.runner)
}
