package ir.persiancalendar.app

import io.github.abdulwaheds.prayertimeplus.CalculationMethod
import io.github.abdulwaheds.prayertimeplus.Coordinates
import io.github.abdulwaheds.prayertimeplus.DateComponents
import io.github.abdulwaheds.prayertimeplus.Madhab
import io.github.abdulwaheds.prayertimeplus.PrayerTimes
import java.time.ZoneOffset
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesCalculationTest {
    @Test
    fun tehranMethodProducesChronologicalPrayerTimes() {
        val parameters = CalculationMethod.OTHER.parameters().copy(
            fajrAngle = 17.7,
            maghribIsInterval = false,
            maghribValue = 4.5,
            ishaIsInterval = false,
            ishaValue = 14.0,
            madhab = Madhab.SHAFI
        )
        val times = PrayerTimes(
            coordinates = Coordinates(35.6892, 51.3890, 0.0),
            dateComponents = DateComponents(2026, 11, 23),
            calculationParameters = parameters,
            utcOffset = ZoneOffset.ofHoursMinutes(3, 30),
            countryCode = "IR",
            cityName = "Tehran"
        )
        val ordered = listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
        assertTrue(ordered.all { it != null })
        for (index in 0 until ordered.lastIndex) {
            assertNotNull(ordered[index])
            assertNotNull(ordered[index + 1])
            assertTrue(ordered[index]!!.isBefore(ordered[index + 1]!!))
        }
    }
}
