package org.example.air_workshop.ui.components

import org.example.air_workshop.model.Currency
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Multiplatform replacement for JVM-only `"%.Nf".format(this)`.
 * Rounds half away from zero and pads the fraction with zeros.
 */
fun Double.toFixed(decimals: Int): String {
    require(decimals >= 0) { "decimals must be non-negative" }
    if (isNaN() || isInfinite()) return toString()

    var factor = 1L
    repeat(decimals) { factor *= 10 }

    val scaled = (abs(this) * factor).roundToLong()
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    val integerPart = scaled / factor
    if (decimals == 0) return "$sign$integerPart"

    val fractionPart = (scaled % factor).toString().padStart(decimals, '0')
    return "$sign$integerPart.$fractionPart"
}

fun formatCurrency(amount: Double, currencyCode: String, decimals: Int = 2): String =
    formatCurrency(amount, Currency.fromCode(currencyCode), decimals)

fun formatCurrency(amount: Double, currency: Currency, decimals: Int = 2): String =
    "${currency.symbol}${amount.toFixed(decimals)}"
