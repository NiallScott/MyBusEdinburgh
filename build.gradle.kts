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

import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
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

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    plugins.withType<BasePlugin>().configureEach {
        extensions.configure<BaseExtension> {
            compileSdkVersion(libs.versions.android.sdk.compile.get().toInt())
            buildToolsVersion(libs.versions.android.build.tools.get())

            defaultConfig {
                minSdk = libs.versions.android.sdk.min.get().toInt()
                targetSdk = libs.versions.android.sdk.target.get().toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            signingConfigs {
                create("globalDebug") {
                    storeFile = file(project.findProperty("mybus.keystore.debug.file")
                        ?.toString()
                        ?: "/dev/null")
                    storePassword = project.findProperty("mybus.keystore.debug.storePassword")
                        ?.toString()
                        ?: "not_set"
                    keyAlias = project.findProperty("mybus.keystore.debug.keyAlias")
                        ?.toString()
                        ?: "not_set"
                    keyPassword = project.findProperty("mybus.keystore.debug.keyPassword")
                        ?.toString()
                        ?: "not_set"
                }
            }

            testOptions {
                @Suppress("UnstableApiUsage")
                managedDevices {
                    devices {
                        maybeCreate<ManagedVirtualDevice>("pixel2api27").apply {
                            device = "Pixel 2"
                            apiLevel = 27
                            systemImageSource = "aosp" // No ATD.
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api28").apply {
                            device = "Pixel 2"
                            apiLevel = 28
                            systemImageSource = "aosp" // No ATD.
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api29").apply {
                            device = "Pixel 2"
                            apiLevel = 29
                            systemImageSource = "aosp" // No ATD.
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api30").apply {
                            device = "Pixel 2"
                            apiLevel = 30
                            systemImageSource = "aosp-atd"
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api31").apply {
                            device = "Pixel 2"
                            apiLevel = 31
                            systemImageSource = "aosp-atd"
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api33").apply {
                            device = "Pixel 2"
                            apiLevel = 33
                            systemImageSource = "aosp-atd"
                        }

                        maybeCreate<ManagedVirtualDevice>("pixel2api34").apply {
                            device = "Pixel 2"
                            apiLevel = 34
                            systemImageSource = "aosp" // No ATD - yet.
                        }
                    }

                    groups {
                        maybeCreate("allApis").apply {
                            targetDevices += devices["pixel2api27"]
                            targetDevices += devices["pixel2api28"]
                            targetDevices += devices["pixel2api29"]
                            targetDevices += devices["pixel2api30"]
                            targetDevices += devices["pixel2api31"]
                            targetDevices += devices["pixel2api33"]
                            targetDevices += devices["pixel2api34"]
                        }
                    }
                }
            }
        }
    }
}