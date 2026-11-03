package org.example.air_workshop.platform

import android.content.ClipData
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.air_workshop.database.requireAndroidContext
import java.io.File

/**
 * Writes the CSV into the app cache and opens the system share sheet for it.
 * The file is exposed through the FileProvider declared in the app manifest
 * (authority `${applicationId}.fileprovider`, `exports/` cache path).
 */
actual suspend fun shareCsvFile(fileName: String, csvContent: String) {
    val context = requireAndroidContext()

    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        File(dir, fileName).apply { writeText(csvContent, Charsets.UTF_8) }
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, fileName)
        clipData = ClipData.newRawUri(fileName, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Started from the application context, so the chooser needs its own task
    val chooser = Intent.createChooser(sendIntent, "Export $fileName").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
