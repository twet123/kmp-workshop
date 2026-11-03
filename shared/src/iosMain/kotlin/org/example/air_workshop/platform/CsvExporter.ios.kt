package org.example.air_workshop.platform

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

@OptIn(BetaInteropApi::class)
actual suspend fun shareCsvFile(fileName: String, csvContent: String) {
    // Write to a temporary file in the documents directory
    val fileManager = NSFileManager.defaultManager
    val documentUrl = fileManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    ).firstOrNull() as? NSURL ?: return

    val fileUrl = documentUrl.URLByAppendingPathComponent(fileName) ?: return

    val data = NSString.create(string = csvContent).dataUsingEncoding(NSUTF8StringEncoding) ?: return
    data.writeToURL(fileUrl, true)

    // Create activity view controller to share the file
    val activityItems = listOf(fileUrl)
    val activityVC = UIActivityViewController(activityItems, null)

    // Present from the topmost view controller
    val keyWindow = UIApplication.sharedApplication.windows
        .filterIsInstance<UIWindow>()
        .firstOrNull { it.keyWindow }
    keyWindow?.rootViewController?.presentViewController(activityVC, true, null)
}
