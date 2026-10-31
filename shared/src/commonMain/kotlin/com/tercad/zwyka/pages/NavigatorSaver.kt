// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.runtime.saveable.Saver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorSaver

/*
 * Keeps the opened screens in memory, only their key goes into the saved state of the window.
 *
 * The default saver of Voyager serializes the screens on Android when the app goes to the background,
 * but they hold the services, so the app crashed by the Home button. The screens are restored when the window
 * is recreated in the same process (e.g. rotated); after the process is killed the app starts from the home page.
 */
@OptIn(ExperimentalVoyagerApi::class, InternalVoyagerApi::class)
fun inMemoryNavigatorSaver(): NavigatorSaver<String> =
    NavigatorSaver { _, key, stateHolder, disposeBehavior, parent ->
        Saver(
            save = { navigator -> key.also { savedScreens[it] = navigator.items } },
            restore = { saved ->
                savedScreens.remove(saved)?.let { Navigator(it, key, stateHolder, disposeBehavior, parent) }
            },
        )
    }

private val savedScreens = mutableMapOf<String, List<Screen>>()
