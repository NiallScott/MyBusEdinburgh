/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "uk.org.rivernile.android.bustracker.androidcore"

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

    packaging {
        resources {
            excludes += setOf(
                "META-INF/*.md")
        }
    }
}

dependencies {

    implementation(project(":core:alerts-android"))
    implementation(project(":core:app-properties-android"))
    implementation(project(":core:busstop-db-updater-android"))
    implementation(project(":core:connectivity-android"))
    api(project(":core:coroutines"))
    implementation(project(":core:http-core"))
    implementation(project(":core:http-core-android"))
    implementation(project(":core:location-android"))
    implementation(project(":core:logging-android"))
    implementation(project(":core:permission-android"))
    api(project(":core:preferences-android"))
    implementation(project(":database:busstop-db-android"))
    implementation(project(":database:settings-db-android"))
    implementation(project(":endpoint:internal-api-endpoint"))
    implementation(project(":endpoint:tracker-endpoint"))
    implementation(project(":ui:text-formatting"))

    // FIXME: these dependencies are listed here for now. This will be sorted when androidcore is
    // removed from the project.
    api(project(":core:alerts"))
    api(project(":core:app-properties"))
    api(project(":core:busstop-db-updater"))
    api(project(":core:busstops"))
    api(project(":core:config"))
    api(project(":core:connectivity"))
    api(project(":core:coroutines"))
    api(project(":core:favourites"))
    api(project(":core:feature"))
    api(project(":core:http-core"))
    api(project(":core:http-file-downloader"))
    api(project(":core:livetimes"))
    api(project(":core:location"))
    api(project(":core:logging"))
    api(project(":core:preferences"))
    api(project(":core:services"))
    api(project(":core:servicepoints"))
    api(project(":core:servicestops"))
    api(project(":core:time"))
    api(project(":core:twitter"))
    api(project(":database:busstop-db-core"))
    api(project(":database:settings-db-core"))
    api(project(":endpoint:internal-api-endpoint"))
    api(project(":endpoint:tracker-endpoint"))
}