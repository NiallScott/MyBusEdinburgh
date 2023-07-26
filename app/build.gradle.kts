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

import com.android.build.api.variant.ResValue

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.parcelize")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "uk.org.rivernile.edinburghbustracker.android"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        firebaseAppDistribution {
            artifactType = "APK"
            groups = "testers"
        }
    }

    signingConfigs {
        create("globalDebug") {
            storeFile = file(
                project.findProperty("mybus.keystore.debug.file") as String? ?: "/dev/null")
            storePassword =
                project.findProperty("mybus.keystore.debug.storePassword") as String? ?: "not_set"
            keyAlias = project.findProperty("mybus.keystore.debug.keyAlias") as String? ?: "not_set"
            keyPassword = project.findProperty("mybus.keystore.debug.keyPassword") as String?
                ?: "not_set"
        }

        create("edinburgh") {
            storeFile = file(
                project.findProperty("mybus.keystore.release.file") as String? ?: "/dev/null")
            storePassword = project.findProperty("mybus.keystore.release.storePassword") as String?
                ?: "not_set"
            keyAlias = project.findProperty("mybus.keystore.release.keyAlias") as String?
                ?: "not_set"
            keyPassword = project.findProperty("mybus.keystore.release.keyPassword") as String?
                ?: "not_set"
        }
    }

    @Suppress("UnstableApiUsage")
    flavorDimensions += "city"

    productFlavors {
        create("edinburgh") {
            dimension = "city"

            applicationId = "uk.org.rivernile.edinburghbustracker.android"
            versionCode = 18
            versionName = "3.1"
            setProperty("archivesBaseName", "MyBusEdinburgh-$versionName")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            @Suppress("UnstableApiUsage")
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro")

            productFlavors {
                getByName("edinburgh") {
                    signingConfig = signingConfigs.getByName("edinburgh")
                }
            }
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            signingConfig = signingConfigs.getByName("globalDebug")
        }
    }

    @Suppress("UnstableApiUsage")
    buildFeatures {
        viewBinding = true
    }

    @Suppress("UnstableApiUsage")
    useLibrary("android.test.mock")
}

androidComponents {
    onVariants { variant ->
        variant.resValues.put(
            variant.makeResValueKey("string", "package_name"),
            ResValue(variant.applicationId.get(), null))

        variant.manifestPlaceholders.put(
            "googleMapsApiKey",
            project
                .findProperty("mybus.${variant.flavorName}.${variant.buildType}.mapsKey") as? String
                ?: "undefined")
    }
}

dependencies {
    implementation(project(":androidcore"))

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    debugImplementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.livedata)

    // Material Design
    implementation(libs.material)

    // Play Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.maps.ktx)
    implementation(libs.play.services.maps.utils)
    implementation(libs.play.services.maps.utils.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    implementation(libs.fetchutils)

    // Image loading
    implementation(libs.picasso)

    // Test dependencies
    androidTestImplementation(project(":testutils"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.hamcrest)
    androidTestImplementation(libs.mockito.android)

    testImplementation(project(":testutils"))
    testImplementation(libs.androidx.arch.core.test)
}