package ir.persiancalendar.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IranCalendarEventsTest {
    @Test
    fun year1405NewYearHolidaysArePresent() {
        assertTrue(IranCalendarEvents.isHoliday(1405, 1, 1))
        assertTrue(IranCalendarEvents.isHoliday(1405, 1, 13))
    }

    @Test
    fun year1406OfficialHolidaysArePresent() {
        assertTrue(IranCalendarEvents.isHoliday(1406, 1, 1))
        assertTrue(IranCalendarEvents.isHoliday(1406, 3, 25))
        assertTrue(IranCalendarEvents.isHoliday(1406, 12, 29))
    }

    @Test
    fun ordinaryDatesAreNotMarkedAsHoliday() {
        assertFalse(IranCalendarEvents.isHoliday(1406, 1, 5))
        assertFalse(IranCalendarEvents.isHoliday(1405, 2, 2))
    }

    @Test
    fun unsupportedYearsReturnNoOfficialEvents() {
        assertEquals(emptyList<IranCalendarOccasion>(), IranCalendarEvents.forDate(1407, 1, 1))
    }
}
