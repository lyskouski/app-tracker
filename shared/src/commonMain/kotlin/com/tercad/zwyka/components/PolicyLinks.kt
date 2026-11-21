// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.doc_locale
import com.tercad.zwyka.resources.privacy_policy
import com.tercad.zwyka.resources.terms_of_use
import com.tercad.zwyka.resources.usage_guide

/** Documents of the repository, by the suffix of the language (`privacy_policy_en.md`). */
private const val DOCS_URL = "https://github.com/lyskouski/app-tracker/blob/main/docs/l10n"

fun privacyPolicyUrl(docLocale: String): String = "$DOCS_URL/privacy_policy_$docLocale.md"

fun termsOfUseUrl(docLocale: String): String = "$DOCS_URL/terms_of_use_$docLocale.md"

/*
 * [?] Usage guide
 * [↗] Privacy Policy
 * [↗] Terms of Use
 *
 * The documents open in the browser, in the language of the application; the guide is shown with [onGuideClick].
 */
@Composable
fun PolicyLinks(
    modifier: Modifier = Modifier,
    onGuideClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val docLocale = stringResource(Res.string.doc_locale)
    Column(modifier = modifier) {
        onGuideClick?.let {
            LinkButton(Icons.AutoMirrored.Filled.HelpOutline, stringResource(Res.string.usage_guide), it)
        }
        LinkButton(Icons.AutoMirrored.Filled.OpenInNew, stringResource(Res.string.privacy_policy)) {
            uriHandler.openUri(privacyPolicyUrl(docLocale))
        }
        LinkButton(Icons.AutoMirrored.Filled.OpenInNew, stringResource(Res.string.terms_of_use)) {
            uriHandler.openUri(termsOfUseUrl(docLocale))
        }
    }
}

@Composable
private fun LinkButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}
