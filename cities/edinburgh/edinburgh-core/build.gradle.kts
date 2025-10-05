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

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `java-test-fixtures`
}

dependencies {

    implementation(project(":core:alphanumcomparator"))
    implementation(project(":core:config"))
    implementation(project(":core:connectivity"))
    implementation(project(":core:http-core"))
    implementation(project(":core:livetimes"))
    implementation(project(":core:logging"))
    implementation(project(":core:services"))
    implementation(project(":core:time"))
    implementation(project(":endpoint:tracker-endpoint"))

    // Dagger 2
    implementation(libs.dagger.core)
    ksp(libs.dagger.compiler)

    // Retrofit
    implementation(libs.retrofit)

    // Okhttp
    implementation(libs.okhttp)

    // (De-)serialisation
    implementation(libs.kotlin.serialization.json)

    // Edinburgh APIs
    api(libs.edinburgh.bus.tracker.api)

    // Tests
    testImplementation(project(":testutils"))
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)

    testFixturesImplementation(project(":core:services"))
}
