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
    fun year1406ContainsTwentyEightOfficialHolidayEntries() {
        val count = (1..12).sumOf { month -> (1..31).count { day -> IranCalendarEvents.isHoliday(1406, month, day) } }
        assertEquals(28, count)
    }

    @Test
    fun year1406IncludesImportantNonHolidayOccasions() {
        assertFalse(IranCalendarEvents.forDate(1406, 1, 25).isEmpty())
        assertFalse(IranCalendarEvents.forDate(1406, 9, 30).isEmpty())
    }

    @Test
    fun unsupportedYearsReturnNoOfficialEvents() {
        assertEquals(emptyList<IranCalendarOccasion>(), IranCalendarEvents.forDate(1407, 1, 1))
    }
    @Test
    fun year1405IncludesProfessionalAndStudentOccasions() {
        assertTrue(IranCalendarEvents.forDate(1405, 7, 24).any { it.title.contains("پرستار") })
        assertTrue(IranCalendarEvents.forDate(1405, 8, 13).any { it.title.contains("دانش‌آموز") })
        assertTrue(IranCalendarEvents.forDate(1405, 12, 29).any { it.title.contains("ملی شدن صنعت نفت") })
    }

    @Test
    fun year1406IncludesNewNationalCommemorations() {
        assertTrue(IranCalendarEvents.forDate(1406, 8, 21).any { it.title.contains("هوافضا") })
        assertTrue(IranCalendarEvents.forDate(1406, 9, 9).any { it.title.contains("جزایر سه‌گانه") })
        assertTrue(IranCalendarEvents.forDate(1406, 7, 14).any { it.title.contains("پرستار") })
        assertTrue(IranCalendarEvents.forDate(1406, 8, 13).any { it.title.contains("دانش‌آموز") })
    }

    @Test
    fun monthListingIncludesAdditionalOccasions() {
        assertTrue(IranCalendarEvents.forMonth(1405, 8).any { it.second.title.contains("دانش‌آموز") })
        assertTrue(IranCalendarEvents.forMonth(1406, 9).any { it.second.title.contains("جزایر سه‌گانه") })
    }

    @Test
    fun overlappingCuratedListsDoNotShowDuplicateOccasions() {
        val events = IranCalendarEvents.forDate(1405, 4, 7)
        assertEquals(events.map { it.title }.distinct().size, events.size)
        assertEquals(1, events.count { it.title.contains("قوه قضاییه") })
    }

    @Test
    fun monthListingDoesNotDuplicateSameOccasion() {
        val events = IranCalendarEvents.forMonth(1405, 6)
        val keys = events.map { it.first to it.second.title }
        assertEquals(keys.distinct().size, keys.size)
    }

}
