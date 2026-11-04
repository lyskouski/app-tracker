package com.tercad.zwyka

import com.tercad.zwyka.core.files.FileTransfer
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

/*
 * Files through the native file dialog.
 */
class DesktopFileTransfer(private val owner: Frame) : FileTransfer {
    override suspend fun save(fileName: String, content: String): Boolean {
        val file = choose(FileDialog.SAVE, fileName) ?: return false
        withContext(Dispatchers.IO) { file.writeText(content) }
        return true
    }

    override suspend fun open(extensions: List<String>): ByteArray? {
        val pattern = extensions.joinToString(";") { "*.$it" }
        val file = choose(FileDialog.LOAD, pattern) { name -> extensions.any { name.endsWith(".$it", ignoreCase = true) } }
            ?: return null
        return withContext(Dispatchers.IO) { file.readBytes() }
    }

    private suspend fun choose(
        mode: Int,
        fileName: String,
        accept: (String) -> Boolean = { true },
    ): File? = withContext(Dispatchers.Swing) {
        val dialog = FileDialog(owner, null, mode).apply {
            file = fileName
            setFilenameFilter { _, name -> accept(name) }
            isVisible = true
        }
        dialog.file?.let { File(dialog.directory, it) }
    }
}
