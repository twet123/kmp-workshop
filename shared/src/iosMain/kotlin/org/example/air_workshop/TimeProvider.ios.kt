package org.example.air_workshop

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentEpochMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}