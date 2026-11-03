package org.example.air_workshop.platform

/**
 * Platform-specific CSV file exporter.
 * Each platform implements this to share or save the CSV content appropriately.
 */
expect suspend fun shareCsvFile(fileName: String, csvContent: String)
