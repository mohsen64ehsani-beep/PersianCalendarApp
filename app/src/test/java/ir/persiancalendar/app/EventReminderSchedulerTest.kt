package ir.persiancalendar.app

import org.junit.Assert.assertEquals
import org.junit.Test

class EventReminderSchedulerTest {
    private val oneDay = 24L * 60L * 60L * 1000L

    @Test
    fun nextDailyTriggerKeepsOriginalTime() {
        val original = 1_800_000_000_000L
        assertEquals(original + oneDay, EventReminderScheduler.nextDailyTrigger(original, original + 60_000L))
    }

    @Test
    fun missedDailyRemindersAdvanceToNextFutureOccurrence() {
        val original = 1_800_000_000_000L
        val now = original + oneDay * 3 + 1_000L
        assertEquals(original + oneDay * 4, EventReminderScheduler.nextDailyTrigger(original, now))
    }

    @Test
    fun triggerExactlyAtNowAdvancesOneMoreDay() {
        val original = 1_800_000_000_000L
        assertEquals(original + oneDay * 2, EventReminderScheduler.nextDailyTrigger(original, original + oneDay))
    }
}
