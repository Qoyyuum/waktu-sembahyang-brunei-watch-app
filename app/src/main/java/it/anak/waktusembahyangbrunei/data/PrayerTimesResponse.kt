package it.anak.waktusembahyangbrunei.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimesResponse(
    val status: String = "",
    val code: Int = 0,
    val meta: Meta = Meta(),
    val data: PrayerData = PrayerData(),
)

@Serializable
data class Meta(
    val district: String = "",
    val timezone: String? = null,
    @SerialName("offset_minutes") val offsetMinutes: Int? = null,
    @SerialName("generated_at") val generatedAt: String? = null,
)

@Serializable
data class PrayerData(
    val day: String = "",
    val date: String = "",
    @SerialName("date_iso") val dateIso: String = "",
    @SerialName("date_malay") val dateMalay: String = "",
    val hijri: String = "",
    val timings: Timings = Timings(),
    val schedule: List<ScheduleEntry> = emptyList(),
)

@Serializable
data class Timings(
    val imsak: String = "",
    val suboh: String = "",
    val syuruk: String = "",
    val doha: String = "",
    val zohor: String = "",
    val asar: String = "",
    val maghrib: String = "",
    val isyak: String = "",
)

@Serializable
data class ScheduleEntry(
    val key: String,
    val name: String,
    val time: String,
    @SerialName("time_24") val time24: String? = null,
    @SerialName("time_12") val time12: String? = null,
    val type: String? = null,
)
