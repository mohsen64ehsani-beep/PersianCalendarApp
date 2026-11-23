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
        (1 to 14) to listOf(IranCalendarOccasion("شهادت امام جعفر صادق (ع)", true)),
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

    // Year 1406 event dates are curated from the published annual listing.
    // Lunar occasions must be checked against the final official calendar before release.
    // Reference listing: https://timestamp.ir/events-list/1406
    private val year1406 = mapOf(
        (1 to 1) to listOf(IranCalendarOccasion("عید نوروز و آغاز سال نو", true)),
        (1 to 2) to listOf(IranCalendarOccasion("تعطیلات عید نوروز", true)),
        (1 to 3) to listOf(IranCalendarOccasion("تعطیلات عید نوروز", true)),
        (1 to 4) to listOf(IranCalendarOccasion("تعطیلات عید نوروز", true)),
        (1 to 6) to listOf(IranCalendarOccasion("زادروز زرتشت (روز امید)", false)),
        (1 to 12) to listOf(IranCalendarOccasion("روز جمهوری اسلامی ایران", true)),
        (1 to 13) to listOf(IranCalendarOccasion("روز طبیعت", true)),
        (1 to 14) to listOf(IranCalendarOccasion("شهادت امام جعفر صادق (ع)", true)),
        (1 to 19) to listOf(IranCalendarOccasion("جشن فروردینگان", false)),
        (1 to 20) to listOf(IranCalendarOccasion("روز ملی فناوری هسته‌ای", false)),
        (1 to 25) to listOf(IranCalendarOccasion("روز بزرگداشت عطار نیشابوری", false)),
        (1 to 29) to listOf(IranCalendarOccasion("روز ارتش جمهوری اسلامی ایران", false)),
        (2 to 1) to listOf(IranCalendarOccasion("روز بزرگداشت سعدی", false)),
        (2 to 3) to listOf(IranCalendarOccasion("روز معمار و بزرگداشت شیخ بهایی", false)),
        (2 to 9) to listOf(IranCalendarOccasion("روز شوراها", false)),
        (2 to 10) to listOf(IranCalendarOccasion("روز ملی خلیج فارس", false)),
        (2 to 12) to listOf(IranCalendarOccasion("روز معلم", false)),
        (2 to 15) to listOf(IranCalendarOccasion("روز شیراز", false)),
        (2 to 17) to listOf(IranCalendarOccasion("شهادت امام محمد تقی (ع)", false)),
        (2 to 18) to listOf(IranCalendarOccasion("سالروز ازدواج حضرت علی (ع) و حضرت فاطمه (س)", false)),
        (2 to 23) to listOf(IranCalendarOccasion("روز بهداشت و سلامت ایران", false)),
        (2 to 24) to listOf(IranCalendarOccasion("شهادت امام محمد باقر (ع)", true)),
        (2 to 25) to listOf(IranCalendarOccasion("روز بزرگداشت فردوسی", false)),
        (2 to 26) to listOf(IranCalendarOccasion("روز عرفه", false)),
        (2 to 27) to listOf(IranCalendarOccasion("عید سعید قربان", true)),
        (3 to 1) to listOf(IranCalendarOccasion("بزرگداشت ملاصدرا", false)),
        (3 to 3) to listOf(IranCalendarOccasion("سالروز آزادسازی خرمشهر", false)),
        (3 to 4) to listOf(IranCalendarOccasion("عید سعید غدیر خم", true)),
        (3 to 6) to listOf(IranCalendarOccasion("ولادت امام موسی کاظم (ع)", false)),
        (3 to 10) to listOf(IranCalendarOccasion("روز مباهله", false)),
        (3 to 14) to listOf(IranCalendarOccasion("رحلت امام خمینی (ره)", true)),
        (3 to 15) to listOf(IranCalendarOccasion("قیام ۱۵ خرداد", true)),
        (3 to 24) to listOf(IranCalendarOccasion("تاسوعای حسینی", true)),
        (3 to 25) to listOf(IranCalendarOccasion("عاشورای حسینی", true)),
        (3 to 27) to listOf(IranCalendarOccasion("شهادت امام زین‌العابدین (ع)", false)),
        (4 to 1) to listOf(IranCalendarOccasion("روز اصناف", false)),
        (4 to 7) to listOf(IranCalendarOccasion("روز قوه قضاییه", false)),
        (4 to 10) to listOf(IranCalendarOccasion("روز صنعت و معدن و بزرگداشت صائب تبریزی", false)),
        (4 to 13) to listOf(IranCalendarOccasion("جشن تیرگان", false)),
        (4 to 14) to listOf(IranCalendarOccasion("روز قلم", false)),
        (4 to 22) to listOf(IranCalendarOccasion("روز فناوری اطلاعات", false)),
        (4 to 25) to listOf(IranCalendarOccasion("روز بهزیستی و تأمین اجتماعی", false)),
        (5 to 3) to listOf(IranCalendarOccasion("اربعین حسینی", true)),
        (5 to 11) to listOf(IranCalendarOccasion("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", true)),
        (5 to 12) to listOf(IranCalendarOccasion("شهادت امام رضا (ع)", true)),
        (5 to 13) to listOf(IranCalendarOccasion("روز دریانوردی", false)),
        (5 to 17) to listOf(IranCalendarOccasion("روز خبرنگار", false)),
        (5 to 20) to listOf(IranCalendarOccasion("شهادت امام حسن عسکری (ع)", true)),
        (5 to 21) to listOf(IranCalendarOccasion("آغاز امامت حضرت ولی‌عصر (عج)", false)),
        (5 to 29) to listOf(IranCalendarOccasion("ولادت پیامبر اکرم (ص) و امام جعفر صادق (ع)", true)),
        (6 to 1) to listOf(IranCalendarOccasion("روز پزشک", false)),
        (6 to 5) to listOf(IranCalendarOccasion("روز داروساز", false)),
        (6 to 10) to listOf(IranCalendarOccasion("روز بانکداری اسلامی", false)),
        (6 to 11) to listOf(IranCalendarOccasion("روز پرستار", false)),
        (6 to 12) to listOf(IranCalendarOccasion("روز مددکار اجتماعی", false)),
        (6 to 15) to listOf(IranCalendarOccasion("ولادت حضرت عبدالعظیم حسنی (ع)", false)),
        (6 to 19) to listOf(IranCalendarOccasion("ولادت امام حسن عسکری (ع)", false)),
        (6 to 21) to listOf(IranCalendarOccasion("روز سینمای ایران", false)),
        (6 to 27) to listOf(IranCalendarOccasion("روز شعر و ادب فارسی و بزرگداشت شهریار", false)),
        (6 to 31) to listOf(IranCalendarOccasion("آغاز هفته دفاع مقدس", false)),
        (7 to 4) to listOf(IranCalendarOccasion("روز گرامیداشت سربازان وطن", false)),
        (7 to 5) to listOf(IranCalendarOccasion("سالروز شکست حصر آبادان", false)),
        (7 to 7) to listOf(IranCalendarOccasion("روز آتش‌نشانی و ایمنی", false)),
        (7 to 8) to listOf(IranCalendarOccasion("روز بزرگداشت مولوی", false)),
        (7 to 10) to listOf(IranCalendarOccasion("جشن مهرگان", false)),
        (7 to 13) to listOf(IranCalendarOccasion("روز نیروی انتظامی", false)),
        (7 to 14) to listOf(IranCalendarOccasion("ولادت حضرت زینب (س) و روز پرستار", false)),
        (7 to 16) to listOf(IranCalendarOccasion("روز ملی کودک", false)),
        (7 to 20) to listOf(IranCalendarOccasion("روز بزرگداشت حافظ", false)),
        (7 to 26) to listOf(IranCalendarOccasion("روز تربیت بدنی و ورزش", false)),
        (8 to 7) to listOf(IranCalendarOccasion("روز بزرگداشت کوروش بزرگ", false)),
        (8 to 8) to listOf(IranCalendarOccasion("روز نوجوان", false)),
        (8 to 12) to listOf(IranCalendarOccasion("شهادت حضرت فاطمه زهرا (س)", true)),
        (8 to 13) to listOf(IranCalendarOccasion("روز دانش‌آموز و روز مبارزه با استکبار", false)),
        (8 to 14) to listOf(IranCalendarOccasion("روز فرهنگ عمومی", false)),
        (8 to 24) to listOf(IranCalendarOccasion("روز کتاب و کتابخوانی", false)),
        (8 to 29) to listOf(IranCalendarOccasion("ولادت حضرت فاطمه زهرا (س) و روز مادر", false)),
        (9 to 1) to listOf(IranCalendarOccasion("روز اصفهان", false)),
        (9 to 5) to listOf(IranCalendarOccasion("روز بسیج مستضعفان", false)),
        (9 to 9) to listOf(IranCalendarOccasion("ولادت امام محمد باقر (ع)", false)),
        (9 to 11) to listOf(IranCalendarOccasion("شهادت امام هادی (ع)", false)),
        (9 to 15) to listOf(IranCalendarOccasion("روز حسابدار", false)),
        (9 to 16) to listOf(IranCalendarOccasion("روز دانشجو", false)),
        (9 to 18) to listOf(IranCalendarOccasion("ولادت امام محمد تقی (ع)", false)),
        (9 to 21) to listOf(IranCalendarOccasion("ولادت امام علی (ع) و روز پدر", true)),
        (9 to 23) to listOf(IranCalendarOccasion("وفات حضرت زینب (س)", false)),
        (9 to 25) to listOf(IranCalendarOccasion("روز پژوهش و فناوری", false)),
        (9 to 30) to listOf(IranCalendarOccasion("شب یلدا", false)),
        (10 to 3) to listOf(IranCalendarOccasion("شهادت امام موسی کاظم (ع)", false)),
        (10 to 4) to listOf(IranCalendarOccasion("روز بزرگداشت رودکی", false)),
        (10 to 5) to listOf(IranCalendarOccasion("مبعث پیامبر اکرم (ص)", true)),
        (10 to 8) to listOf(IranCalendarOccasion("روز ملی موسیقی ایرانی", false)),
        (10 to 11) to listOf(IranCalendarOccasion("ولادت امام حسین (ع) و روز پاسدار", false)),
        (10 to 12) to listOf(IranCalendarOccasion("ولادت حضرت ابوالفضل (ع) و روز جانباز", false)),
        (10 to 13) to listOf(IranCalendarOccasion("سالروز شهادت سردار سلیمانی", false)),
        (10 to 19) to listOf(IranCalendarOccasion("ولادت حضرت علی‌اکبر (ع) و روز جوان", false)),
        (10 to 23) to listOf(IranCalendarOccasion("ولادت حضرت قائم (عج) و نیمه شعبان", true)),
        (10 to 29) to listOf(IranCalendarOccasion("روز هوای پاک", false)),
        (11 to 10) to listOf(IranCalendarOccasion("جشن سده", false)),
        (11 to 19) to listOf(IranCalendarOccasion("روز نیروی هوایی", false)),
        (11 to 22) to listOf(IranCalendarOccasion("پیروزی انقلاب اسلامی ایران", true)),
        (11 to 28) to listOf(IranCalendarOccasion("شهادت امام علی (ع)", true)),
        (11 to 29) to listOf(IranCalendarOccasion("جشن سپندارمذگان", false)),
        (12 to 5) to listOf(IranCalendarOccasion("روز مهندس", false)),
        (12 to 8) to listOf(IranCalendarOccasion("عید سعید فطر", true)),
        (12 to 9) to listOf(IranCalendarOccasion("تعطیل به مناسبت عید سعید فطر", true)),
        (12 to 15) to listOf(IranCalendarOccasion("روز درختکاری", false)),
        (12 to 29) to listOf(IranCalendarOccasion("روز ملی شدن صنعت نفت ایران", true))
    )

    fun forDate(year: Int, month: Int, day: Int): List<IranCalendarOccasion> =
        when (year) {
            1405 -> year1405[month to day].orEmpty()
            1406 -> year1406[month to day].orEmpty()
            else -> emptyList()
        }

    fun isHoliday(year: Int, month: Int, day: Int): Boolean =
        forDate(year, month, day).any { it.isHoliday }
}
