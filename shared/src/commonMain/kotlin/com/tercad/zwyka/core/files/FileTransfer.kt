// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.files

import androidx.compose.runtime.staticCompositionLocalOf

/*
 * Platform file dialogs (Android, iOS, Desktop, Web).
 */
interface FileTransfer {
    /** Lets the user store [content] as [fileName]. @return false when cancelled. */
    suspend fun save(fileName: String, content: String): Boolean

    /** Lets the user pick a file, preferably with [extension]. @return its text, or null when cancelled. */
    suspend fun open(extension: String): String?
}

object NoFileTransfer : FileTransfer {
    override suspend fun save(fileName: String, content: String) = false

    override suspend fun open(extension: String): String? = null
}

val LocalFileTransfer = staticCompositionLocalOf<FileTransfer> { NoFileTransfer }
