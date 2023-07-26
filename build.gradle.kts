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

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed, possibly in Gradle 8.1
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.appdistribution) apply false
    base
}

subprojects {

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    plugins.withType<BasePlugin>().configureEach {
        extensions.configure<BaseExtension> {
            compileSdkVersion(libs.versions.compile.sdk.version.get().toInt())
            buildToolsVersion(libs.versions.build.tools.version.get())

            defaultConfig {
                minSdk = libs.versions.min.sdk.version.get().toInt()
                targetSdk = libs.versions.target.sdk.version.get().toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}