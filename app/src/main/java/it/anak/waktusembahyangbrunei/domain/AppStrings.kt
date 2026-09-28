package it.anak.waktusembahyangbrunei.domain

import it.anak.waktusembahyangbrunei.data.PrayerRepository

/** Malay/English label maps for prayer keys and UI strings. */
object AppStrings {

    fun prayerName(key: String, lang: String): String =
        if (lang == PrayerRepository.LANG_ENGLISH) {
            when (key) {
                "imsak" -> "Imsak"
                "suboh" -> "Fajr"
                "syuruk" -> "Sunrise"
                "doha" -> "Duha"
                "zohor" -> "Dhuhr"
                "asar" -> "Asr"
                "maghrib" -> "Maghrib"
                "isyak" -> "Isha"
                else -> key.replaceFirstChar { it.uppercase() }
            }
        } else {
            when (key) {
                "imsak" -> "Imsak"
                "suboh" -> "Suboh"
                "syuruk" -> "Syuruk"
                "doha" -> "Doha"
                "zohor" -> "Zohor"
                "asar" -> "Asar"
                "maghrib" -> "Maghrib"
                "isyak" -> "Isyak"
                else -> key.replaceFirstChar { it.uppercase() }
            }
        }

    fun districtName(district: String): String =
        when (district) {
            "brunei-muara" -> "Brunei-Muara"
            "tutong" -> "Tutong"
            "belait" -> "Belait"
            "temburong" -> "Temburong"
            else -> district
        }

    fun settings(lang: String) = if (lang == PrayerRepository.LANG_ENGLISH) "Settings" else "Tetapan"

    fun refresh(lang: String) = if (lang == PrayerRepository.LANG_ENGLISH) "Refresh" else "Muat semula"

    fun district(lang: String) = if (lang == PrayerRepository.LANG_ENGLISH) "District" else "Daerah"

    fun language(lang: String) = if (lang == PrayerRepository.LANG_ENGLISH) "Language" else "Bahasa"

    fun nextIn(name: String, duration: String, lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "$name in $duration" else "$name dalam $duration"

    fun inDuration(duration: String, lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "in $duration" else "dalam $duration"

    fun tomorrow(lang: String) = if (lang == PrayerRepository.LANG_ENGLISH) "tomorrow" else "esok"

    fun offline(lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "Offline data" else "Data luar talian"

    fun loading(lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "Loading…" else "Memuatkan…"

    fun unavailable(lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "No data — open app to refresh" else "Tiada data"

    fun tapToRetry(lang: String) =
        if (lang == PrayerRepository.LANG_ENGLISH) "Tap to retry" else "Ketik untuk cuba semula"
}
