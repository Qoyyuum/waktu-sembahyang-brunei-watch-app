package it.anak.waktusembahyangbrunei.domain

import it.anak.waktusembahyangbrunei.data.ScheduleEntry
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

data class NextPrayer(
    val key: String,
    val time: LocalTime,
    val at: LocalDateTime,
    val isTomorrow: Boolean,
    val until: Duration,
)

object NextPrayerCalculator {

    /**
     * Finds the next upcoming schedule entry after [now]. Entries at or before the current minute
     * count as passed. When nothing remains today, the next prayer is tomorrow's Imsak — uses
     * [tomorrowImsak] when available, otherwise today's Imsak as an approximation.
     */
    fun next(
        schedule: List<ScheduleEntry>,
        now: LocalDateTime,
        tomorrowImsak: LocalTime? = null,
    ): NextPrayer? {
        val sorted =
            schedule
                .mapNotNull { entry -> parseTime(entry.time)?.let { entry.key to it } }
                .sortedBy { it.second }

        val upcoming = sorted.firstOrNull { (_, time) -> time > now.toLocalTime() }
        if (upcoming != null) {
            val at = LocalDateTime.of(now.toLocalDate(), upcoming.second)
            return NextPrayer(
                key = upcoming.first,
                time = upcoming.second,
                at = at,
                isTomorrow = false,
                until = Duration.between(now, at),
            )
        }

        val imsak = tomorrowImsak ?: parseTime(schedule.firstOrNull { it.key == "imsak" }?.time)
        if (imsak != null) {
            val at = LocalDateTime.of(now.toLocalDate().plusDays(1), imsak)
            return NextPrayer(
                key = "imsak",
                time = imsak,
                at = at,
                isTomorrow = true,
                until = Duration.between(now, at),
            )
        }
        return null
    }

    fun parseTime(time: String?): LocalTime? =
        time?.let {
            runCatching { LocalTime.parse(it.trim()) }.getOrNull()
        }

    /** "2h 14m" / "45m" style countdown. */
    fun formatDuration(duration: Duration): String {
        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
