package ir.persiancalendar.app

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class PersianCalendarInstrumentedTest {
    private fun persianCalendar() = Calendar.getInstance(
        TimeZone.getTimeZone("Asia/Tehran"),
        ULocale("fa_IR@calendar=persian")
    )

    @Test
    fun firstDayOf1405MatchesMarch21_2026() {
        val persian = persianCalendar().apply {
            isLenient = false
            clear()
            set(1405, 0, 1)
        }
        val gregorian = GregorianCalendar(TimeZone.getTimeZone("Asia/Tehran"), Locale.US).apply {
            time = persian.time
        }
        assertEquals(2026, gregorian.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.MARCH, gregorian.get(java.util.Calendar.MONTH))
        assertEquals(21, gregorian.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun leapYearEsfandHasThirtyDaysAndNextYearDoesNot() {
        val leapYear = persianCalendar().apply {
            set(Calendar.YEAR, 1403)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val commonYear = persianCalendar().apply {
            set(Calendar.YEAR, 1404)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(30, leapYear.getActualMaximum(Calendar.DAY_OF_MONTH))
        assertEquals(29, commonYear.getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun dateRoundTripsThroughGregorianCalendar() {
        val original = persianCalendar().apply {
            isLenient = false
            clear()
            set(1405, 8, 2)
        }
        val gregorian = GregorianCalendar(TimeZone.getTimeZone("Asia/Tehran"), Locale.US).apply {
            time = original.time
        }
        val roundTrip = persianCalendar().apply { time = gregorian.time }
        assertEquals(original.get(Calendar.YEAR), roundTrip.get(Calendar.YEAR))
        assertEquals(original.get(Calendar.MONTH), roundTrip.get(Calendar.MONTH))
        assertEquals(original.get(Calendar.DAY_OF_MONTH), roundTrip.get(Calendar.DAY_OF_MONTH))
    }
}
