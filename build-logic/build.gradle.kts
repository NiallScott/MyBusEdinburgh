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

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins.register("mybus.android-application") {
        id = "mybus.android-application"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.android" +
            ".AndroidApplicationPlugin"
    }

    plugins.register("mybus.android-compose") {
        id = "mybus.android-compose"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.compose" +
            ".AndroidComposePlugin"
    }

    plugins.register("mybus.android-library") {
        id = "mybus.android-library"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.android" +
            ".AndroidLibraryPlugin"
    }

    plugins.register("mybus.android-test") {
        id = "mybus.android-test"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.android.AndroidTestPlugin"
    }

    plugins.register("mybus.dagger-convention") {
        id = "mybus.dagger-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.dagger" +
            ".DaggerConventionPlugin"
    }

    plugins.register("mybus.hilt-convention") {
        id = "mybus.hilt-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.dagger" +
            ".HiltConventionPlugin"
    }

    plugins.register("mybus.java-convention") {
        id = "mybus.java-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.java.JavaConventionPlugin"
    }

    plugins.register("mybus.kotlin-convention") {
        id = "mybus.kotlin-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.kotlin" +
            ".KotlinConventionPlugin"
    }

    plugins.register("mybus.room-convention") {
        id = "mybus.room-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.room" +
            ".RoomConventionPlugin"
    }

    plugins.register("mybus.test-convention") {
        id = "mybus.test-convention"
        implementationClass = "uk.org.rivernile.android.bustracker.gradle.test.TestConventionPlugin"
    }
}

dependencies {

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:${libs.versions.kotlin.get()}")
    compileOnly("com.android.tools.build:gradle-api:${libs.versions.android.gradle.plugin.get()}")
    compileOnly(plugin(libs.plugins.ksp))
}

private fun plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
