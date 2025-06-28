/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.androidx.baselineprofile)
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
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")

            productFlavors {
                getByName("edinburgh") {
                    signingConfig = signingConfigs.getByName("edinburgh")
                }
            }
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            signingConfig = signingConfigs.getByName("globalDebug")
        }

        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            proguardFiles += file("benchmark-rules.pro")
            signingConfig = signingConfigs.getByName("globalDebug")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
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

baselineProfile {
    automaticGenerationDuringBuild = true
    saveInSrc = false
}

dependencies {

    "edinburghImplementation"(project(":cities:edinburgh:edinburgh-android"))
    implementation(project(":core:alerts-android"))
    implementation(project(":core:busstops-android"))
    implementation(project(":core:config"))
    implementation(project(":core:connectivity-android"))
    implementation(project(":core:coroutines-android"))
    implementation(project(":core:busstop-db-updater-android"))
    implementation(project(":core:favourites"))
    implementation(project(":core:feature"))
    implementation(project(":core:livetimes-android"))
    implementation(project(":core:location-android"))
    implementation(project(":core:logging-android"))
    implementation(project(":core:permission-android"))
    implementation(project(":core:preferences-android"))
    implementation(project(":core:services"))
    implementation(project(":core:services-android"))
    implementation(project(":core:servicepoints"))
    implementation(project(":core:servicestops"))
    implementation(project(":core:time-android"))
    implementation(project(":core:twitter"))
    implementation(project(":database:busstop-db-android"))
    implementation(project(":endpoint:tracker-endpoint"))
    implementation(project(":endpoint:internal-api-endpoint"))
    implementation(project(":ui:feature:about"))
    implementation(project(":ui:text-formatting"))
    implementation(project(":ui:ui-core"))
    implementation(project(":ui:widget:contentview"))
    implementation(project(":ui:widget:expandcollapseindicator"))

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    debugImplementation(libs.androidx.fragment.testing.manifest)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.livedata)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.profileinstaller)

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

    // Image loading
    implementation(libs.picasso)

    // Test dependencies
    androidTestImplementation(testFixtures(project(":core:feature")))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.kotlin.test.junit)

    testImplementation(testFixtures(project(":core:time")))
    testImplementation(testFixtures(project(":database:busstop-db-core")))
    testImplementation(project(":testutils"))
    testImplementation(libs.androidx.arch.core.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)

    baselineProfile(project(":macrobenchmark:app-baselineprofile"))
}