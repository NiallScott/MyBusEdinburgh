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
    kotlin("jvm")
    kotlin("kapt")
    id("kotlinx-serialization")
}

dependencies {

    api(project(":core:alerts"))
    api(project(":core:app-properties"))
    api(project(":core:busstops"))
    api(project(":core:config"))
    api(project(":core:connectivity"))
    api(project(":core:coroutines"))
    api(project(":core:favourites"))
    api(project(":core:http-core"))
    api(project(":core:http-file-downloader"))
    api(project(":core:livetimes"))
    api(project(":core:location"))
    api(project(":core:logging"))
    api(project(":core:services"))
    api(project(":core:servicepoints"))
    api(project(":core:servicestops"))
    api(project(":core:time"))
    api(project(":core:twitter"))
    api(project(":database:busstop-db-core"))
    api(project(":database:settings-db-core"))
    api(project(":endpoint:internal-api-endpoint"))
    api(project(":endpoint:tracker-endpoint"))

    // Kotlin
    api(libs.coroutines.core)

    // Dagger 2
    implementation(libs.dagger.core)
    kapt(libs.dagger.compiler)

    // (De-)serialisation
    api(libs.kotlin.serialization.json)

    // Okhttp
    api(libs.okhttp)

    // Retrofit
    api(libs.retrofit)

    // Testing
    testImplementation(project(":testutils"))
}