package com.tercad.zwyka

import com.tercad.zwyka.core.files.FileTransfer
import kotlin.js.Promise
import kotlinx.coroutines.await

/*
 * Files through a browser download and a file input.
 */
class WebFileTransfer : FileTransfer {
    override suspend fun save(fileName: String, content: String): Boolean {
        downloadFile(fileName, content)
        return true
    }

    override suspend fun open(extensions: List<String>): ByteArray? {
        // The bytes come as a string of char codes 0..255: Kotlin/Wasm has no direct ArrayBuffer access.
        val content = pickFile(extensions.joinToString(",") { ".$it" }).await<JsString?>()?.toString() ?: return null
        return ByteArray(content.length) { content[it].code.toByte() }
    }
}

private fun downloadFile(fileName: String, content: String): Unit = js(
    """{
        const url = URL.createObjectURL(new Blob([content], { type: "application/sql" }));
        const link = document.createElement("a");
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(url), 0);
    }""",
)

// Resolves with null when the dialog is closed without a file.
private fun pickFile(accept: String): Promise<JsString?> = js(
    """new Promise((resolve, reject) => {
        const input = document.createElement("input");
        input.type = "file";
        input.accept = accept;
        input.addEventListener("cancel", () => resolve(null));
        input.addEventListener("change", () => {
            const file = input.files && input.files[0];
            if (!file) return resolve(null);
            file.arrayBuffer().then((buffer) => {
                const bytes = new Uint8Array(buffer);
                let text = "";
                for (let i = 0; i < bytes.length; i += 8192) {
                    text += String.fromCharCode.apply(null, bytes.subarray(i, i + 8192));
                }
                resolve(text);
            }, reject);
        });
        input.click();
    })""",
)
