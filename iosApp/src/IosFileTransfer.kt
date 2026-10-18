package com.tercad.zwyka

import com.tercad.zwyka.core.files.FileTransfer
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

/*
 * Files through UIDocumentPickerViewController.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class IosFileTransfer : FileTransfer {
    // The picker keeps its delegate weakly.
    private var delegate: PickerDelegate? = null

    override suspend fun save(fileName: String, content: String): Boolean {
        val file = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
        val written = NSString.create(string = content)
            .writeToURL(file, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        if (!written) error("Unable to write $fileName")
        return pick(UIDocumentPickerViewController(forExportingURLs = listOf(file), asCopy = true)) != null
    }

    override suspend fun open(extension: String): String? {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData), asCopy = true)
        val file = pick(picker) ?: return null
        return NSString.stringWithContentsOfURL(file, encoding = NSUTF8StringEncoding, error = null)
            ?: error("Unable to read ${file.lastPathComponent}")
    }

    private suspend fun pick(picker: UIDocumentPickerViewController): NSURL? =
        suspendCancellableCoroutine { continuation ->
            val pickerDelegate = PickerDelegate { file ->
                delegate = null
                if (continuation.isActive) continuation.resume(file)
            }
            delegate = pickerDelegate
            picker.delegate = pickerDelegate
            val presenter = topViewController() ?: error("No view controller to present the file picker")
            presenter.presentViewController(picker, animated = true, completion = null)
            continuation.invokeOnCancellation { picker.dismissViewControllerAnimated(true, completion = null) }
        }

    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}

private class PickerDelegate(
    private val onResult: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}
