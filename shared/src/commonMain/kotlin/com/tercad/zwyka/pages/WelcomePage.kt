// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.components.LanguageField
import com.tercad.zwyka.components.PolicyLinks
import com.tercad.zwyka.defines.GuideSection
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.usageGuide
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.accept_and_continue
import com.tercad.zwyka.resources.usage_guide
import com.tercad.zwyka.resources.welcome_agreement
import com.tercad.zwyka.resources.welcome_intro
import com.tercad.zwyka.resources.welcome_title
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.widgets.BottomBar
import com.tercad.zwyka.widgets.BottomBarBackButton
import com.tercad.zwyka.widgets.BottomBarButton
import com.tercad.zwyka.widgets.Heading
import com.tercad.zwyka.widgets.screenTitle

/*
 * Shown on the first run ([isFirstRun]) until the terms are accepted, then the home page is shown (see `App`).
 * Otherwise it is the usage guide opened from [InfoPage], with the "Back" button.
 */
class WelcomePage(
    private val settingsStore: AppSettingsStore,
    private val isFirstRun: Boolean = true,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val title = stringResource(if (isFirstRun) Res.string.welcome_title else Res.string.usage_guide)
        val textColor = currentColorScheme().onSurface

        Scaffold(
            modifier = Modifier.screenTitle(title),
            containerColor = currentColorScheme().surface,
            bottomBar = {
                BottomBar {
                    if (isFirstRun) {
                        BottomBarButton(
                            icon = Icons.Filled.Check,
                            text = stringResource(Res.string.accept_and_continue),
                            onClick = {
                                scope.launch {
                                    settingsStore.update(settingsStore.current.copy(isTermsAccepted = true))
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    } else {
                        BottomBarBackButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                    }
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
                if (isFirstRun) {
                    Text(stringResource(Res.string.welcome_intro), color = textColor)
                    LanguageField(settingsStore)
                    Text(stringResource(Res.string.welcome_agreement), color = textColor)
                    PolicyLinks()
                    Heading(stringResource(Res.string.usage_guide))
                }
                usageGuide.forEach { GuideSectionView(it) }
            }
        }
    }
}

/*
 * Title
 * Long press   Habit: opens its details with streaks and goals
 */
@Composable
private fun GuideSectionView(section: GuideSection) {
    val title = section.titleArg
        ?.let { stringResource(section.title, stringResource(it)) }
        ?: stringResource(section.title)
    Heading(
        text = title,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
    )
    // Rows touch each other, so the stripes of the odd ones read as a table.
    Column(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
        section.actions.forEachIndexed { index, action ->
            Row(
                // Read out as one sentence: the gesture and its outcome.
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index % 2 == 0) Modifier.background(currentColorScheme().surfaceVariant) else Modifier)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = action.keys ?: action.gesture?.let { stringResource(it) }.orEmpty(),
                    modifier = Modifier.weight(0.4f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = currentColorScheme().primary,
                )
                Text(
                    text = stringResource(action.description),
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentColorScheme().onSurface,
                )
            }
        }
    }
}
