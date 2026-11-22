package ir.persiancalendar.app

/**
 * Official public holidays for Solar Hijri year 1405.
 * Source: final official Iranian calendar, prepared by the Calendar Center
 * of the Institute of Geophysics, University of Tehran.
 *
 * Lunar dates may be adjusted by official moon-sighting announcements.
 */
data class IranCalendarOccasion(
    val title: String,
    val isHoliday: Boolean
)

object IranCalendarEvents {
    private val year1405 = mapOf(
        (1 to 1) to listOf(IranCalendarOccasion("عید سعید فطر و آغاز نوروز", true)),
        (1 to 2) to listOf(IranCalendarOccasion("تعطیل عید سعید فطر و عید نوروز", true)),
        (1 to 3) to listOf(IranCalendarOccasion("عید نوروز", true)),
        (1 to 4) to listOf(IranCalendarOccasion("عید نوروز", true)),
        (1 to 6) to listOf(IranCalendarOccasion("زادروز زرتشت پیامبر", false)),
        (1 to 12) to listOf(IranCalendarOccasion("روز جمهوری اسلامی ایران", true)),
        (1 to 13) to listOf(IranCalendarOccasion("روز طبیعت", true)),
        (1 to 17) to listOf(IranCalendarOccasion("روز فرهنگ پهلوانی و ورزش زورخانه‌ای", false)),
        (1 to 18) to listOf(IranCalendarOccasion("روز سلامتی", false)),
        (1 to 20) to listOf(IranCalendarOccasion("روز ملی فناوری هسته‌ای و روز هنر انقلاب اسلامی", false)),
        (1 to 25) to listOf(
            IranCalendarOccasion("شهادت امام جعفر صادق (ع)", true),
            IranCalendarOccasion("روز بزرگداشت عطار نیشابوری", false)
        ),
        (1 to 29) to listOf(IranCalendarOccasion("روز ارتش جمهوری اسلامی و نیروی زمینی", false)),
        (1 to 30) to listOf(IranCalendarOccasion("ولادت حضرت معصومه (س) و روز دختران", false)),
        (2 to 1) to listOf(IranCalendarOccasion("روز بزرگداشت سعدی و روز نثر فارسی", false)),
        (2 to 10) to listOf(IranCalendarOccasion("روز ملی خلیج فارس", false)),
        (2 to 12) to listOf(IranCalendarOccasion("روز معلم", false)),
        (2 to 25) to listOf(IranCalendarOccasion("روز پاسداشت زبان فارسی و بزرگداشت فردوسی", false)),
        (3 to 3) to listOf(
            IranCalendarOccasion("شهادت امام محمد باقر (ع)", false),
            IranCalendarOccasion("سالروز آزادسازی خرمشهر", false)
        ),
        (3 to 5) to listOf(IranCalendarOccasion("روز عرفه", false)),
        (3 to 6) to listOf(IranCalendarOccasion("عید سعید قربان", true)),
        (3 to 14) to listOf(
            IranCalendarOccasion("عید سعید غدیر خم", true),
            IranCalendarOccasion("رحلت امام خمینی (ره)", true)
        ),
        (3 to 15) to listOf(IranCalendarOccasion("قیام ۱۵ خرداد", true)),
        (3 to 23) to listOf(IranCalendarOccasion("روز شهدای اقتدار و اتحاد ملی ایران", false)),
        (4 to 3) to listOf(IranCalendarOccasion("تاسوعای حسینی", true)),
        (4 to 4) to listOf(IranCalendarOccasion("عاشورای حسینی", true)),
        (5 to 13) to listOf(IranCalendarOccasion("اربعین حسینی", true)),
        (5 to 21) to listOf(
            IranCalendarOccasion("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", true)
        ),
        (5 to 22) to listOf(IranCalendarOccasion("شهادت امام رضا (ع)", true)),
        (5 to 30) to listOf(IranCalendarOccasion("شهادت امام حسن عسکری (ع)", true)),
        (6 to 8) to listOf(IranCalendarOccasion("میلاد پیامبر اکرم (ص) و امام جعفر صادق (ع)", true)),
        (8 to 22) to listOf(IranCalendarOccasion("شهادت حضرت فاطمه زهرا (س)", true)),
        (10 to 2) to listOf(IranCalendarOccasion("ولادت امام علی (ع) و روز پدر", true)),
        (10 to 16) to listOf(IranCalendarOccasion("مبعث پیامبر اکرم (ص)", true)),
        (11 to 4) to listOf(IranCalendarOccasion("ولادت حضرت قائم (عج) و نیمه شعبان", true)),
        (11 to 22) to listOf(IranCalendarOccasion("پیروزی انقلاب اسلامی ایران", true)),
        (12 to 9) to listOf(IranCalendarOccasion("شهادت امام علی (ع)", true)),
        (12 to 19) to listOf(IranCalendarOccasion("عید سعید فطر", true)),
        (12 to 20) to listOf(IranCalendarOccasion("تعطیل به مناسبت عید سعید فطر", true)),
        (12 to 29) to listOf(IranCalendarOccasion("روز ملی شدن صنعت نفت ایران", true))
    )

    fun forDate(year: Int, month: Int, day: Int): List<IranCalendarOccasion> =
        if (year == 1405) year1405[month to day].orEmpty() else emptyList()

    fun isHoliday(year: Int, month: Int, day: Int): Boolean =
        forDate(year, month, day).any { it.isHoliday }
}
