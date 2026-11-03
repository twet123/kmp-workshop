package org.example.air_workshop.model

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    RSD("RSD", "дин.", "Serbian Dinar"),
    PLN("PLN", "zł", "Polish Zloty"),
    CZK("CZK", "Kč", "Czech Koruna"),
    HUF("HUF", "Ft", "Hungarian Forint"),
    RON("RON", "lei", "Romanian Leu"),
    BGN("BGN", "лв", "Bulgarian Lev"),
    HRK("HRK", "kn", "Croatian Kuna"),
    CHF("CHF", "CHF", "Swiss Franc"),
    JPY("JPY", "¥", "Japanese Yen"),
    CNY("CNY", "¥", "Chinese Yuan"),
    TRY("TRY", "₺", "Turkish Lira"),
    GEL("GEL", "₾", "Georgian Lari"),
    AZN("AZN", "₼", "Azerbaijani Manat"),
    KZT("KZT", "₸", "Kazakhstani Tenge"),
    UAH("UAH", "₴", "Ukrainian Hryvnia"),
    MDL("MDL", "L", "Moldovan Leu"),
    BAM("BAM", "KM", "Bosnian Convertible Mark"),
    MKD("MKD", "ден", "Macedonian Denar"),
    ALL("ALL", "Lek", "Albanian Lek"),
    SEK("SEK", "kr", "Swedish Krona"),
    NOK("NOK", "kr", "Norwegian Krone"),
    DKK("DKK", "kr", "Danish Krone");

    companion object {
        fun fromCode(code: String): Currency =
            entries.find { it.code.uppercase() == code.uppercase() } ?: EUR
    }
}
