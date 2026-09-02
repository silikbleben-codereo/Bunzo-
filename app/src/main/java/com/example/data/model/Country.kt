package com.example.data.model

data class Country(
    val code: String, // ISO country code
    val nameAr: String,
    val dialCode: String,
    val flagEmoji: String,
    val exampleNumber: String,
    val phoneLength: Int
) {
    companion object {
        val SYRIA = Country(
            code = "SY",
            nameAr = "سوريا",
            dialCode = "+963",
            flagEmoji = "🇸🇾",
            exampleNumber = "9XXXXXXXX",
            phoneLength = 9
        )

        val SUPPORTED_COUNTRIES = listOf(
            SYRIA,
            Country("LB", "لبنان", "+961", "🇱🇧", "70XXXXXX", 8),
            Country("JO", "الأردن", "+962", "🇯🇴", "79XXXXXXX", 9),
            Country("IQ", "العراق", "+964", "🇮🇶", "7XXXXXXXXX", 10),
            Country("AE", "الإمارات", "+971", "🇦🇪", "50XXXXXXX", 9),
            Country("SA", "السعودية", "+966", "🇸🇦", "5XXXXXXXX", 9),
            Country("EG", "مصر", "+20", "🇪🇬", "10XXXXXXXX", 10),
            Country("KW", "الكويت", "+965", "🇰🇼", "9XXXXXXX", 8),
            Country("QA", "قطر", "+974", "🇶🇦", "5XXXXXXX", 8),
            Country("OM", "عُمان", "+968", "🇴🇲", "9XXXXXXX", 8),
            Country("BH", "البحرين", "+973", "🇧🇭", "3XXXXXXX", 8),
            Country("TR", "تركيا", "+90", "🇹🇷", "5XXXXXXXXX", 10),
            Country("DE", "ألمانيا", "+49", "🇩🇪", "15XXXXXXXX", 10),
            Country("SE", "السويد", "+46", "🇸🇪", "70XXXXXXX", 9)
        )
    }
}
