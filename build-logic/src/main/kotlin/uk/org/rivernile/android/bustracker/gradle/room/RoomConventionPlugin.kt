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

package uk.org.rivernile.android.bustracker.gradle.room

import com.android.build.api.dsl.CommonExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import uk.org.rivernile.android.bustracker.gradle.ksp.applyKspPlugin
import uk.org.rivernile.android.bustracker.gradle.versioncatalog.versionCatalog

/**
 * This [Plugin] applies Room conventions to any module which applies it.
 *
 * @author Niall Scott
 */
public class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyConventionPlugin()
    }

    private fun Project.applyConventionPlugin() {
        applyKspPlugin()

        extensions.configure<CommonExtension> {
            sourceSets {
                getByName("androidTest")
                    .assets
                    .directories += "$projectDir/schemas"
            }
        }

        extensions.configure<KspExtension> {
            /*
             * This is used to export the Room schema out to a JSON file in the module's "schemas"
             * directory. We want to do this so that we can compare schema versions after upgrades.
             * It's also possible for us to perform automated testing using the JSON files to test
             * database migrations.
             */
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        val libs = versionCatalog

        dependencies.add("implementation", libs.findLibrary("androidx.room.core").get())
        dependencies.add("ksp", libs.findLibrary("androidx.room.compiler").get())
    }
}
