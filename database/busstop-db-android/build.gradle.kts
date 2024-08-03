/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "uk.org.rivernile.android.bustracker.core.database.busstop"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles += file("proguard-consumer-rules.pro")
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

    flavorDimensions += "city"

    productFlavors {
        create("edinburgh") {
            dimension = "city"
        }
    }

    sourceSets {
        // This adds the generated Room schema files to the instrumentation test assets so that they
        // can be loaded at test time.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/*.md")
        }
    }
}

ksp {
    /*
     * This is used to export the Room schema out to a JSON file in the module's "schemas"
     * directory. We want to do this so that we can compare schema versions after upgrades.
     * It's also possible for us to do automated testing using the JSON files to test database
     * migrations.
     */
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation(project(":core:coroutines"))
    implementation(project(":core:logging"))
    api(project(":database:busstop-db-core"))

    // Kotlin
    implementation(libs.coroutines.android)

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Room (ORM)
    implementation(libs.androidx.room.core)
    ksp(libs.androidx.room.compiler)

    // Okio
    implementation(libs.okio)

    // Test dependencies
    androidTestImplementation(testFixtures(project(":core:logging")))
    androidTestImplementation(project(":database:database-test-android"))
    androidTestImplementation(project(":testutils"))
    androidTestImplementation(libs.androidx.room.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.turbine)

    testImplementation(project(":testutils"))
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
}