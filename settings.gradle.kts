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

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyBusEdinburgh"

include(
    ":app",
    ":core",
    ":core:app-properties",
    ":core:app-properties-android",
    ":core:config",
    ":core:connectivity",
    ":core:connectivity-android",
    ":core:coroutines",
    ":core:favourites",
    ":core:http-core",
    ":core:http-file-downloader",
    ":core:http-logging-android",
    ":core:livetimes",
    ":core:location",
    ":core:location-android",
    ":core:logging",
    ":core:logging-android",
    ":core:time",
    ":core:twitter",
    ":database:settings-db-android",
    ":database:settings-db-core",
    ":edinburgh",
    ":endpoint:internal-api-endpoint",
    ":endpoint:tracker-endpoint",
    ":androidcore",
    ":testutils")