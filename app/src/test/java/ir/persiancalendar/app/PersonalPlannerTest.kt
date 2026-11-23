package ir.persiancalendar.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalPlannerTest {
    @Test
    fun tasksRoundTripThroughJson() {
        val source = listOf(
            PlannerTask(101L, "خرید کتاب", "۱۴۰۵/۰۹/۰۳", false),
            PlannerTask(102L, "تماس با خانواده", "", true)
        )
        assertEquals(source, decodePlannerTasks(encodePlannerTasks(source)))
    }

    @Test
    fun malformedJsonReturnsEmptyList() {
        assertTrue(decodePlannerTasks("{broken").isEmpty())
        assertTrue(decodePlannerTasks(null).isEmpty())
    }

    @Test
    fun blankTitlesAreIgnoredAndTitlesAreTrimmed() {
        val tasks = decodePlannerTasks("""[{"id":1,"title":"  ","dueDate":"","completed":false},{"id":2,"title":"  کار مهم  ","dueDate":"","completed":false}]""")
        assertEquals(1, tasks.size)
        assertEquals("کار مهم", tasks.single().title)
    }

    @Test
    fun duplicateIdsAreCollapsed() {
        val tasks = listOf(
            PlannerTask(7L, "اول", "", false),
            PlannerTask(7L, "دوم", "", true)
        )
        assertEquals(1, decodePlannerTasks(encodePlannerTasks(tasks)).size)
    }

    @Test
    fun completedStateIsPreserved() {
        val decoded = decodePlannerTasks(encodePlannerTasks(listOf(PlannerTask(9L, "تمام", "", true))))
        assertTrue(decoded.single().completed)
        assertFalse(decoded.single().title.isBlank())
    }
}
