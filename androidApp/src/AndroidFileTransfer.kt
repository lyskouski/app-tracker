package com.tercad.zwyka

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.tercad.zwyka.core.files.FileTransfer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Files through the Storage Access Framework.
 *
 * Created in `onCreate`: activity results are registered before the activity starts.
 */
class AndroidFileTransfer(activity: ComponentActivity) : FileTransfer {
    private val resolver = activity.contentResolver
    private var pending: CompletableDeferred<Uri?>? = null

    private val createDocument = activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { pending?.complete(it) }

    private val openDocument = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { pending?.complete(it) }

    override suspend fun save(fileName: String, content: String): Boolean {
        val uri = pick(createDocument, fileName) ?: return false
        withContext(Dispatchers.IO) {
            val stream = resolver.openOutputStream(uri, "wt") ?: error("Unable to write $uri")
            stream.use { it.write(content.encodeToByteArray()) }
        }
        return true
    }

    override suspend fun open(extension: String): String? {
        // Providers do not know the extension's MIME type: any file is offered.
        val uri = pick(openDocument, arrayOf("*/*")) ?: return null
        return withContext(Dispatchers.IO) {
            val stream = resolver.openInputStream(uri) ?: error("Unable to read $uri")
            stream.use { it.readBytes().decodeToString() }
        }
    }

    private suspend fun <I> pick(launcher: ActivityResultLauncher<I>, input: I): Uri? {
        pending?.cancel()
        val result = CompletableDeferred<Uri?>()
        pending = result
        launcher.launch(input)
        return result.await()
    }
}
