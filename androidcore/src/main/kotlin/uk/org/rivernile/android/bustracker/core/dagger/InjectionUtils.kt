/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.dagger

import android.app.backup.BackupAgent
import dagger.android.HasAndroidInjector

/**
 * Inject an instance (or subclass of) [BackupAgent].
 *
 * [BackupAgent] has a documented special case whereby it may be created under a different
 * [android.app.Application] instance than what we expect, thus not allowing us to perform
 * injection. Therefore, unlike other injection methods that try-and-otherwise-fail, this
 * injection occurs as a best-try.
 *
 * @param backupAgent The [BackupAgent] to inject.
 * @author Niall Scott
 */
fun inject(backupAgent: BackupAgent) {
    val application = backupAgent.applicationContext

    (application as? HasAndroidInjector)
            ?.let { inject(backupAgent, it) }
}

/**
 * Given a `target`, inject it with the [HasAndroidInjector].
 *
 * @param target The target to inject.
 * @param hasAndroidInjector The class which performs the injection.
 */
private fun inject(target: Any, hasAndroidInjector: HasAndroidInjector) {
    hasAndroidInjector.androidInjector()
            .inject(target)
}