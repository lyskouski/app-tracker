// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.components.PolicyLinks
import com.tercad.zwyka.components.ProjectLinks
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.info
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.widgets.BottomBar
import com.tercad.zwyka.widgets.BottomBarBackButton
import com.tercad.zwyka.widgets.Heading
import com.tercad.zwyka.widgets.screenTitle

class InfoPage(
    private val settingsStore: AppSettingsStore,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val title = stringResource(Res.string.info)

        Scaffold(
            modifier = Modifier.screenTitle(title),
            containerColor = currentColorScheme().surface,
            bottomBar = {
                BottomBar {
                    BottomBarBackButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Heading(title)
                PolicyLinks(onGuideClick = { navigator.push(WelcomePage(settingsStore, isFirstRun = false)) })
                HorizontalDivider()
                ProjectLinks()
            }
        }
    }
}
