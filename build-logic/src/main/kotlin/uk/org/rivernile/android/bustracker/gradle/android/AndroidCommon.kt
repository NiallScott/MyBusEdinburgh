/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.gradle.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maybeCreate
import uk.org.rivernile.android.bustracker.gradle.java.myBusJavaVersion
import uk.org.rivernile.android.bustracker.gradle.versioncatalog.versionCatalog

internal fun CommonExtension.configureDefaults(
    project: Project,
    versionCatalog: VersionCatalog = project.versionCatalog
) {
    compileSdk {
        version = release(
            versionCatalog
                .findVersion("android.sdk.compile")
                .get()
                .displayName
                .toInt()
        )
    }

    buildToolsVersion = versionCatalog.findVersion("android.build.tools").get().displayName

    defaultConfig.apply {
        minSdk {
            version = release(
                versionCatalog
                    .findVersion("android.sdk.min")
                    .get()
                    .displayName
                    .toInt()
            )
        }
    }

    compileOptions.apply {
        sourceCompatibility = myBusJavaVersion
        targetCompatibility = myBusJavaVersion
    }

    signingConfigs {
        create("globalDebug") {
            storeFile = project.file(project.findProperty("mybus.keystore.debug.file")
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

    testOptions.apply {
        @Suppress("UnstableApiUsage")
        managedDevices {
            allDevices {
                maybeCreate<ManagedVirtualDevice>("pixel3api28").apply {
                    device = "Pixel 3"
                    sdkVersion = 28
                    systemImageSource = "aosp" // No ATD.
                }

                maybeCreate<ManagedVirtualDevice>("pixel4api29").apply {
                    device = "Pixel 4"
                    sdkVersion = 29
                    systemImageSource = "aosp" // No ATD.
                }

                maybeCreate<ManagedVirtualDevice>("pixel5api30").apply {
                    device = "Pixel 5"
                    sdkVersion = 30
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel6api31").apply {
                    device = "Pixel 6"
                    sdkVersion = 31
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel7api33").apply {
                    device = "Pixel 7"
                    sdkVersion = 33
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel8api34").apply {
                    device = "Pixel 8"
                    sdkVersion = 34
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel9api35").apply {
                    device = "Pixel 9"
                    sdkVersion = 35
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel10api36").apply {
                    device = "Pixel 10"
                    sdkVersion = 36
                    systemImageSource = "aosp-atd"
                }

                maybeCreate<ManagedVirtualDevice>("pixel10api37").apply {
                    // TODO: replace with Pixel 11 when device definition becomes available.
                    device = "Pixel 10"
                    sdkVersion = 37
                    // TODO: replace with ATD image when it becomes available.
                    systemImageSource = "google_apis"
                }
            }

            groups {
                maybeCreate("allApis").apply {
                    targetDevices += allDevices["pixel3api28"]
                    targetDevices += allDevices["pixel4api29"]
                    targetDevices += allDevices["pixel5api30"]
                    targetDevices += allDevices["pixel6api31"]
                    targetDevices += allDevices["pixel7api33"]
                    targetDevices += allDevices["pixel8api34"]
                    targetDevices += allDevices["pixel9api35"]
                    targetDevices += allDevices["pixel10api36"]
                    targetDevices += allDevices["pixel10api37"]
                }
            }
        }
    }
}

internal val Project.hasAndroidTests: Boolean get() =
    findProperty("mybus.hasAndroidTest")?.toString()?.toBoolean() ?: false
