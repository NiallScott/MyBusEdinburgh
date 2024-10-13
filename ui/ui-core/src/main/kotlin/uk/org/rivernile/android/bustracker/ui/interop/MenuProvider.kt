/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.interop

import android.content.Context
import android.content.ContextWrapper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Provides a [MenuHost] that can be used by Composables hosted in a
 * [androidx.activity.ComponentActivity].
 *
 * This implementation was largely borrowed from `BackHandler.kt` in the androidx activity-compose
 * artifact.
 *
 * @author Niall Scott
 */
public object LocalMenuHost {

    private val LocalMenuHost = compositionLocalOf<MenuHost?> { null }

    /**
     * Returns current composition local for the [MenuHost] or `null` if one has not been provided
     * nor is one available by looking at the [LocalContext].
     */
    public val current: MenuHost?
        @Composable
        get() = LocalMenuHost.current
            ?: findOwner<MenuHost>(LocalContext.current)

    /**
     * Associates a [LocalMenuHost] key to a value in a call to CompositionLocalProvider.
     */
    public infix fun provides(menuHost: MenuHost): ProvidedValue<MenuHost?> {
        return LocalMenuHost.provides(menuHost)
    }
}

/**
 * An effect for providing menu items to a [MenuHost].
 *
 * Calling this in your composable adds a [androidx.core.view.MenuProvider] to the [MenuHost] and
 * calls your supplied lambdas as per the [androidx.core.view.MenuProvider] interface.
 *
 * @param onCreateMenu See [androidx.core.view.MenuProvider.onCreateMenu].
 * @param onPrepareMenu See [androidx.core.view.MenuProvider.onPrepareMenu].
 * @param onMenuItemSelected See [androidx.core.view.MenuProvider.onMenuItemSelected].
 * @param onMenuClosed See [androidx.core.view.MenuProvider.onMenuClosed].
 */
@Composable
public fun MenuProvider(
    onCreateMenu: (Menu, MenuInflater) -> Unit,
    onPrepareMenu: ((Menu) -> Unit)? = null,
    onMenuItemSelected: (MenuItem) -> Boolean,
    onMenuClosed: ((Menu) -> Unit)? = null
) {
    val currentOnCreateMenu by rememberUpdatedState(onCreateMenu)
    val currentOnPrepareMenu by rememberUpdatedState(onPrepareMenu)
    val currentOnMenuItemSelected by rememberUpdatedState(onMenuItemSelected)
    val currentOnMenuClosed by rememberUpdatedState(onMenuClosed)

    val menuProvider = remember {
        object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                currentOnCreateMenu(menu, menuInflater)
            }

            override fun onPrepareMenu(menu: Menu) {
                currentOnPrepareMenu?.invoke(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem) =
                currentOnMenuItemSelected(menuItem)

            override fun onMenuClosed(menu: Menu) {
                currentOnMenuClosed?.invoke(menu)
            }
        }
    }

    val menuHost = checkNotNull(LocalMenuHost.current) {
        "No MenuHost was provided via LocalMenuHost."
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    SideEffect {
        menuHost.invalidateMenu()
    }

    DisposableEffect(lifecycleOwner, menuHost) {
        menuHost.addMenuProvider(menuProvider, lifecycleOwner)

        onDispose {
            menuHost.removeMenuProvider(menuProvider)
        }
    }
}

/**
 * Given a [Context], find an instance of [T] by iterating through [ContextWrapper]s until fully
 * unwrapped.
 *
 * @param context The [Context] to find [T] within.
 * @return The first [Context] which is part of [T] or `null` if [T] could not be found once fully
 * unwrapped.
 */
private inline fun <reified T> findOwner(context: Context): T? {
    var innerContext = context

    while (innerContext is ContextWrapper) {
        if (innerContext is T) {
            return innerContext
        }

        innerContext = innerContext.baseContext
    }

    return null
}