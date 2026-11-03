package org.example.air_workshop.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess

actual suspend fun shareCsvFile(fileName: String, csvContent: String) {
    // Run on EDT for Swing components (Compose Desktop's Main dispatcher is the Swing EDT)
    withContext(Dispatchers.Main) {
        val fileChooser = JFileChooser(System.getProperty("user.home"))
        fileChooser.selectedFile = File(fileName)
        fileChooser.fileFilter = FileNameExtensionFilter("CSV Files", "csv")

        val result = fileChooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            // Ensure .csv extension
            val targetFile = if (selectedFile.extension.lowercase() == "csv") {
                selectedFile
            } else {
                File(selectedFile.absolutePath + ".csv")
            }

            FileOutputStream(targetFile).use { it.write(csvContent.toByteArray()) }

            // Try to open the file with the default application
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desktop.open(targetFile)
                    } catch (e: Exception) {
                        // Ignore if opening fails
                    }
                }
            }
        }
    }
}
