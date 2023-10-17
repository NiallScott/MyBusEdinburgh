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
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "uk.org.rivernile.android.bustracker.androidcore"

    defaultConfig {
        testInstrumentationRunner = "uk.org.rivernile.android.bustracker.core.CoreTestRunner"
        consumerProguardFiles += file("proguard-consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }

        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    flavorDimensions += "city"

    productFlavors {
        create("edinburgh") {
            dimension = "city"

            // URLs
            buildConfigField("String", "API_BASE_URL", "\"http://edinb.us/api/\"")
            buildConfigField("String", "TRACKER_BASE_URL", "\"http://ws.mybustracker.co.uk/\"")

            // API keys
            buildConfigField("String", "API_KEY", "\"${getApiKey("edinburgh")}\"")

            // Other
            buildConfigField("String", "SCHEMA_NAME", "\"MBE_10\"")
            buildConfigField("String", "API_APP_NAME", "\"MBE\"")
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
    // Our code module
    api(project(":core"))
    implementation(project(":core:app-properties-android"))
    implementation(project(":core:connectivity-android"))
    implementation(project(":core:http-logging-android"))
    implementation(project(":core:location-android"))
    implementation(project(":core:logging-android"))
    implementation(project(":database:busstop-db-android"))
    implementation(project(":database:settings-db-android"))

    // Kotlin
    implementation(libs.coroutines.android)

    // City implementations
    "edinburghApi"(project(":edinburgh"))

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    api(libs.hilt.work)
    kapt(libs.hilt.androidx.compiler)

    // AndroidX
    implementation(libs.androidx.appcompat)

    // WorkManager
    api(libs.androidx.work)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    // Play Services
    api(libs.play.services.location)

    // Test dependencies
    androidTestImplementation(project(":testutils"))
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.work.test)
    androidTestImplementation(libs.hilt.test)
    kaptAndroidTest(libs.hilt.android.compiler)

    testImplementation(project(":testutils"))
    testImplementation(libs.androidx.arch.core.test)
}

fun getApiKey(city: String): String {
    // Populate the build config with API keys which is provided as a project property so that
    // ApiKey.java can pick them up.
    return project.findProperty("mybus.${city}.apiKey") as? String ?: "undefined"
}