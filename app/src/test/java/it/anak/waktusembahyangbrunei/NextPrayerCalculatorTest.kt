package it.anak.waktusembahyangbrunei

import it.anak.waktusembahyangbrunei.data.ScheduleEntry
import it.anak.waktusembahyangbrunei.domain.NextPrayerCalculator
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextPrayerCalculatorTest {

    private val schedule =
        listOf(
            ScheduleEntry("imsak", "Imsak", "04:41"),
            ScheduleEntry("suboh", "Suboh", "04:51"),
            ScheduleEntry("syuruk", "Syuruk", "06:08"),
            ScheduleEntry("doha", "Doha", "06:31"),
            ScheduleEntry("zohor", "Zohor", "12:12"),
            ScheduleEntry("asar", "Asar", "15:22"),
            ScheduleEntry("maghrib", "Maghrib", "18:14"),
            ScheduleEntry("isyak", "Isyak", "19:23"),
        )

    @Test
    fun `next prayer before imsak is imsak`() {
        val now = LocalDateTime.of(2026, 9, 28, 4, 0)
        val next = NextPrayerCalculator.next(schedule, now)
        assertEquals("imsak", next?.key)
        assertFalse(next?.isTomorrow ?: true)
        assertEquals(41L, next?.until?.toMinutes())
    }

    @Test
    fun `next prayer midday`() {
        val now = LocalDateTime.of(2026, 9, 28, 13, 0)
        val next = NextPrayerCalculator.next(schedule, now)
        assertEquals("asar", next?.key)
        assertEquals(LocalTime.of(15, 22), next?.time)
    }

    @Test
    fun `entry at exactly now counts as passed`() {
        val now = LocalDateTime.of(2026, 9, 28, 12, 12)
        val next = NextPrayerCalculator.next(schedule, now)
        assertEquals("asar", next?.key)
    }

    @Test
    fun `after isyak rolls over to next day imsak`() {
        val now = LocalDateTime.of(2026, 9, 28, 20, 0)
        val next = NextPrayerCalculator.next(schedule, now)
        assertEquals("imsak", next?.key)
        assertTrue(next?.isTomorrow ?: false)
        // Fallback uses today's imsak time.
        assertEquals(LocalTime.of(4, 41), next?.time)
        assertEquals(8L * 60 + 41, next?.until?.toMinutes())
    }

    @Test
    fun `after isyak uses fetched tomorrow imsak when available`() {
        val now = LocalDateTime.of(2026, 9, 28, 20, 0)
        val next = NextPrayerCalculator.next(schedule, now, tomorrowImsak = LocalTime.of(4, 42))
        assertEquals("imsak", next?.key)
        assertEquals(LocalTime.of(4, 42), next?.time)
        assertEquals(8L * 60 + 42, next?.until?.toMinutes())
    }

    @Test
    fun `empty schedule returns null`() {
        val now = LocalDateTime.of(2026, 9, 28, 20, 0)
        val next = NextPrayerCalculator.next(emptyList(), now)
        assertEquals(null, next)
    }

    @Test
    fun `formatDuration renders hours and minutes`() {
        val now = LocalDateTime.of(2026, 9, 28, 16, 0)
        val next = NextPrayerCalculator.next(schedule, now)!!
        assertEquals("2h 14m", NextPrayerCalculator.formatDuration(next.until))
    }
}
