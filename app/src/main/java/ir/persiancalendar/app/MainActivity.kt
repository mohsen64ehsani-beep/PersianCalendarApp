package ir.persiancalendar.app

import android.os.Bundle
import android.os.Build
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import io.github.abdulwaheds.prayertimeplus.CalculationMethod
import io.github.abdulwaheds.prayertimeplus.Coordinates
import io.github.abdulwaheds.prayertimeplus.DateComponents
import io.github.abdulwaheds.prayertimeplus.Madhab
import io.github.abdulwaheds.prayertimeplus.PrayerTimes
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import android.icu.util.Calendar
import android.icu.util.ULocale
import java.util.GregorianCalendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(2100); showSplash = false }
            if (showSplash) DaynegarSplash() else PersianCalendarApp()
        }
    }
}


@Composable
private fun DaynegarSplash() {
    var progressTarget by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(progressTarget, animationSpec = tween(1700), label = "splashProgress")
    val emblemScale = remember { androidx.compose.animation.core.Animatable(.88f) }
    LaunchedEffect(Unit) {
        progressTarget = 1f
        emblemScale.animateTo(1f, animation = tween(850))
    }
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF101B2D), Color(0xFF243C55), Color(0xFF172B3C)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.align(Alignment.TopEnd).padding(top = 70.dp, end = 8.dp).size(210.dp)
            .background(Brush.radialGradient(listOf(Color(0xFFB99A67).copy(alpha = .20f), Color.Transparent)), CircleShape))
        Box(Modifier.align(Alignment.BottomStart).padding(bottom = 55.dp, start = 8.dp).size(250.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF6FA6A0).copy(alpha = .18f), Color.Transparent)), CircleShape))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Box(
                Modifier.size(112.dp).graphicsLayer { scaleX = emblemScale.value; scaleY = emblemScale.value }
                    .background(Brush.linearGradient(listOf(Color(0xFFD9C08B), Color(0xFF9B7948))), RoundedCornerShape(34.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF172B3C), modifier = Modifier.size(58.dp))
            }
            Text("روزنگار", color = Color(0xFFFFF8E9), fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            Text("تقویمِ لحظه‌های زندگی", color = Color(0xFFD9E2E7), fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.width(176.dp).height(4.dp),
                color = Color(0xFFD9C08B),
                trackColor = Color.White.copy(alpha = .16f)
            )
            Text("در حال آماده‌سازی روز شما", color = Color.White.copy(alpha = .66f), fontSize = 11.sp)
        }
    }
}
private data class DayPalette(val ink: Color, val muted: Color, val canvas: Color, val surface: Color)
private val LocalDayPalette = androidx.compose.runtime.staticCompositionLocalOf {
    DayPalette(Color(0xFF202033), Color(0xFF77778A), Color(0xFFF6F4FA), Color.White)
}
private val Ink: Color @Composable get() = LocalDayPalette.current.ink
private val Muted: Color @Composable get() = LocalDayPalette.current.muted
private val CanvasColor: Color @Composable get() = LocalDayPalette.current.canvas
private val Purple = Color(0xFF7255C7)
private val MonthNames = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
private val PersianDigits = "۰۱۲۳۴۵۶۷۸۹"

private fun faNumber(value: Int): String =
    value.toString().map { if (it in '0'..'9') PersianDigits[it - '0'] else it }.joinToString("")

private fun persianCalendar(): Calendar =
    Calendar.getInstance(android.icu.util.TimeZone.getTimeZone("Asia/Tehran"), ULocale("fa_IR@calendar=persian"))

@Composable
private fun PersianCalendarApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var calendarVisit by remember { mutableIntStateOf(0) }
    val preferences = remember { context.getSharedPreferences("daynegar", 0) }
    var themeIndex by remember { mutableIntStateOf(preferences.getInt("theme", 0).coerceIn(0, 4)) }
    var darkMode by remember { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
    if (!preferences.getBoolean("font_default_migrated_v2", false)) {
        preferences.edit().putInt("font_choice", 1).putBoolean("font_default_migrated_v2", true).apply()
    }
    var fontChoice by remember { mutableIntStateOf(preferences.getInt("font_choice", 1).coerceIn(0, 3)) }
    val accent = listOf(Purple, Color(0xFF087E9B), Color(0xFF16845B), Color(0xFFB65C83), Color(0xFFB58A35))[themeIndex]
    val palette = if (darkMode) DayPalette(Color(0xFFF1EFF8), Color(0xFFB8B4C7), Color(0xFF15141B), Color(0xFF24222D))
        else DayPalette(Color(0xFF202033), Color(0xFF77778A), Color(0xFFF6F4FA), Color.White)
    val colors = if (darkMode) darkColorScheme(primary = accent, secondary = accent, tertiary = accent,
        background = palette.canvas, surface = palette.surface, onBackground = palette.ink, onSurface = palette.ink)
        else lightColorScheme(primary = accent, secondary = accent, tertiary = accent,
        background = palette.canvas, surface = palette.surface, onBackground = palette.ink, onSurface = palette.ink)
    val selectedFont = when (fontChoice) {
        1 -> androidx.compose.ui.text.font.FontFamily(
            androidx.compose.ui.text.font.Font(R.font.vazirmatn_regular, FontWeight.Normal),
            androidx.compose.ui.text.font.Font(R.font.vazirmatn_bold, FontWeight.Bold)
        )
        2 -> androidx.compose.ui.text.font.FontFamily(
            androidx.compose.ui.text.font.Font(R.font.estedad_regular, FontWeight.Normal),
            androidx.compose.ui.text.font.Font(R.font.estedad_bold, FontWeight.Bold)
        )
        3 -> androidx.compose.ui.text.font.FontFamily(
            androidx.compose.ui.text.font.Font(R.font.sahel_regular, FontWeight.Normal),
            androidx.compose.ui.text.font.Font(R.font.sahel_bold, FontWeight.Bold)
        )
        else -> androidx.compose.ui.text.font.FontFamily.SansSerif
    }
    CompositionLocalProvider(LocalDayPalette provides palette, androidx.compose.material3.LocalContentColor provides palette.ink) {
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = selectedFont)) {
        Scaffold(
            containerColor = CanvasColor,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(selectedTab == 0, { selectedTab = 0; calendarVisit++ }, icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("تقویم") })
                    NavigationBarItem(selectedTab == 1, { selectedTab = 1 }, icon = { Icon(Icons.Default.CompareArrows, null) }, label = { Text("تبدیل تاریخ") })
                    NavigationBarItem(selectedTab == 2, { selectedTab = 2 }, icon = { Icon(Icons.Default.WbSunny, null) }, label = { Text("اوقات شرعی") })
                    NavigationBarItem(selectedTab == 3, { selectedTab = 3 }, icon = { Icon(Icons.Default.Build, null) }, label = { Text("امکانات") })
                    NavigationBarItem(selectedTab == 4, { selectedTab = 4 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("تنظیمات") })
                }
            }
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = CanvasColor) {
                when (selectedTab) {
                    0 -> CalendarHome(accent, calendarVisit)
                    1 -> DateConverter(accent)
                    2 -> PrayerTimesPage(accent)
                    3 -> DaynegarToolsPage(accent)
                    else -> SettingsPage(themeIndex, { themeIndex = it; preferences.edit().putInt("theme", it).apply() }, darkMode, { darkMode = it; preferences.edit().putBoolean("dark_mode", it).apply() }, fontChoice, { fontChoice = it; preferences.edit().putInt("font_choice", it).apply() }, accent)
                }
            }
        }
        }
    }
    }
}


private data class HafezGhazal(val title: String, val verses: String)
private val HafezGhazals = listOf(
    HafezGhazal(
        "الا یا ایها الساقی",
        "الا یا ایها الساقی ادر کأساً و ناولها\nکه عشق آسان نمود اول ولی افتاد مشکل‌ها\n\nبه بوی نافه‌ای کآخر صبا زان طره بگشاید\nز تاب جعد مشکینش چه خون افتاد در دل‌ها\n\nمرا در منزل جانان چه امن عیش چون هر دم\nجرس فریاد می‌دارد که بربندید محمل‌ها\n\nبه می سجاده رنگین کن گرت پیر مغان گوید\nکه سالک بی‌خبر نبود ز راه و رسم منزل‌ها\n\nشب تاریک و بیم موج و گردابی چنین هایل\nکجا دانند حال ما سبکباران ساحل‌ها\n\nهمه کارم ز خودکامی به بدنامی کشید آخر\nنهان کی ماند آن رازی کز او سازند محفل‌ها\n\nحضوری گر همی‌خواهی از او غایب مشو حافظ\nمتی ما تلق من تهوی دع الدنیا و اهملها"
    ),
    HafezGhazal(
        "اگر آن ترک شیرازی",
        "اگر آن ترک شیرازی به دست آرد دل ما را\nبه خال هندویش بخشم سمرقند و بخارا را\n\nبده ساقی می باقی که در جنت نخواهی یافت\nکنار آب رکن‌آباد و گلگشت مصلا را\n\nفغان کاین لولیان شوخ شیرین‌کار شهرآشوب\nچنان بردند صبر از دل که ترکان خوان یغما را\n\nز عشق ناتمام ما جمال یار مستغنی است\nبه آب و رنگ و خال و خط چه حاجت روی زیبا را\n\nمن از آن حسن روزافزون که یوسف داشت دانستم\nکه عشق از پرده عصمت برون آرد زلیخا را\n\nاگر دشنام فرمایی و گر نفرین دعا گویم\nجواب تلخ می‌زیبد لب لعل شکرخا را\n\nنصیحت گوش کن جانا که از جان دوست‌تر دارند\nجوانان سعادتمند پند پیر دانا را\n\nحدیث از مطرب و می گو و راز دهر کمتر جو\nکه کس نگشود و نگشاید به حکمت این معما را"
    ),
    HafezGhazal(
        "دوش دیدم که ملائک در میخانه زدند",
        "دوش دیدم که ملائک در میخانه زدند\nگل آدم بسرشتند و به پیمانه زدند\n\nساکنان حرم ستر و عفاف ملکوت\nبا من راه‌نشین باده مستانه زدند\n\nآسمان بار امانت نتوانست کشید\nقرعه فال به نام من دیوانه زدند\n\nجنگ هفتاد و دو ملت همه را عذر بنه\nچون ندیدند حقیقت ره افسانه زدند\n\nشکر ایزد که میان من و او صلح افتاد\nصوفیان رقص‌کنان ساغر شکرانه زدند\n\nآتش آن نیست که از شعله او خندد شمع\nآتش آن است که در خرمن پروانه زدند\n\nکس چو حافظ نگشاد از رخ اندیشه نقاب\nتا سر زلف سخن را به قلم شانه زدند"
    )
)

@Composable
private fun HafezGhazalCard(accent: Color, visitToken: Int) {
    val ghazal = remember(visitToken) { HafezGhazals[Math.floorMod(visitToken, HafezGhazals.size)] }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(accent.copy(alpha = .10f), MaterialTheme.colorScheme.surface, Color(0xFFB99A67).copy(alpha = .08f)))
            ).padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("غزل امروز حافظ", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.AutoAwesome, null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Text(ghazal.title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(ghazal.verses, color = Ink, fontSize = 13.sp, lineHeight = 25.sp, textAlign = TextAlign.Center)
            HorizontalDivider(color = Muted.copy(alpha = .18f))
            Text("حافظ شیرازی", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CalendarHome(accent: Color, visitToken: Int) {
    val context = LocalContext.current
    var todayTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1_000)
            todayTick = System.currentTimeMillis()
        }
    }
    val today = remember(todayTick) {
        persianCalendar().apply { timeInMillis = todayTick }
    }
    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)
    val iranClock = remember(todayTick) {
        SimpleDateFormat("HH:mm:ss", Locale("fa", "IR")).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Tehran")
        }.format(java.util.Date(todayTick))
    }

    var visibleYear by remember { mutableIntStateOf(todayYear) }
    var visibleMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedDay by remember { mutableIntStateOf(todayDay) }
    var calendarMode by remember { mutableStateOf("ماه") }

    val monthCalendar = remember(visibleYear, visibleMonth) {
        persianCalendar().apply {
            set(Calendar.YEAR, visibleYear)
            set(Calendar.MONTH, visibleMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDay = monthCalendar.get(Calendar.DAY_OF_WEEK)
    val monthLength = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val offset = firstDay % 7 // ICU: Sunday=1; Persian calendar grid starts Saturday.
    val cells = (0 until offset).map { 0 } + (1..monthLength).toList()
    val eventKey = "$visibleYear-$visibleMonth-$selectedDay"
    val prefs = remember { context.getSharedPreferences("daynegar", 0) }
    var events by remember(eventKey) {
        mutableStateOf<List<String>>(
            prefs.getStringSet("events_$eventKey", emptySet<String>())?.toList()?.sorted() ?: emptyList()
        )
    }
    val officialEvents = remember(visibleYear, visibleMonth, selectedDay) {
        IranCalendarEvents.forDate(visibleYear, visibleMonth + 1, selectedDay)
    }
    val monthOccasions = remember(visibleYear, visibleMonth) {
        IranCalendarEvents.forMonth(visibleYear, visibleMonth + 1)
    }
    var showAddEvent by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("09:00") }
    var pendingReminder by remember { mutableStateOf<Pair<Triple<Int, String, Long>, Boolean>?>(null) }
    var repeatDaily by remember { mutableStateOf(false) }
    val reminderPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val pending = pendingReminder
        if (granted && pending != null) EventReminderScheduler.schedule(context, pending.first.first, pending.first.second, pending.first.third, repeatDaily = pending.second)
        pendingReminder = null
    }

    val selectedGregorian = remember(visibleYear, visibleMonth, selectedDay) {
        runCatching {
            val selected = persianCalendar().apply {
                isLenient = false
                set(Calendar.YEAR, visibleYear)
                set(Calendar.MONTH, visibleMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay.coerceIn(1, monthLength))
            }
            SimpleDateFormat("EEEE، d MMMM yyyy", Locale("fa", "IR")).apply { timeZone = java.util.TimeZone.getTimeZone("Asia/Tehran") }.format(selected.time)
        }.getOrDefault("")
    }

    fun moveMonth(amount: Int) {
        val moved = persianCalendar().apply {
            set(Calendar.YEAR, visibleYear)
            set(Calendar.MONTH, visibleMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, amount)
        }
        visibleYear = moved.get(Calendar.YEAR)
        visibleMonth = moved.get(Calendar.MONTH)
        selectedDay = 1
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("روزنگار", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                Text("تقویم هوشمند فارسی", color = Muted, fontSize = 12.sp)
            }
            Box(Modifier.size(48.dp).background(
                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.68f))), RoundedCornerShape(16.dp)
            ), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = 238.dp).clip(RoundedCornerShape(28.dp))) {
                Image(
                    painter = painterResource(R.drawable.hafezieh_background),
                    contentDescription = "نمایی از حافظیه شیراز",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    Modifier.matchParentSize().background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF101820).copy(alpha = .24f), Color(0xFF101820).copy(alpha = .48f), Color(0xFF101820).copy(alpha = .88f))
                        )
                    )
                )
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = .15f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(27.dp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) "امروزِ شما" else "روز انتخاب‌شده",
                                color = Color.White.copy(alpha = .82f), fontSize = 12.sp, textAlign = TextAlign.End)
                            Text(faNumber(selectedDay) + " " + MonthNames[visibleMonth] + " " + faNumber(visibleYear),
                                color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
                        }
                    }
                    Text(selectedGregorian, modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = .88f), fontSize = 12.sp, textAlign = TextAlign.End)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("ساعت رسمی ایران", color = Color.White.copy(alpha = .76f), fontSize = 10.sp)
                            Text(iranClock, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("منطقه زمانی", color = Color.White.copy(alpha = .76f), fontSize = 10.sp)
                            Text("Asia / Tehran", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    val selectedOccasions = IranCalendarEvents.forDate(visibleYear, visibleMonth + 1, selectedDay)
                    if (selectedOccasions.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = .30f))
                        selectedOccasions.take(3).forEach { item ->
                            Text("✦  " + item.title, modifier = Modifier.fillMaxWidth(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
                        }
                    } else {
                        Text("برای این روز مناسبتی ثبت نشده است.", modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = .78f), fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HafezGhazalCard(accent, visitToken)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("ماه", "هفته", "سال").forEach { mode ->
                val selected = calendarMode == mode
                Box(Modifier.weight(1f).background(if (selected) accent else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { calendarMode = mode }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(mode, color = if (selected) Color.White else Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        when (calendarMode) {
            "سال" -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { visibleYear--; selectedDay = 1 }) { Icon(Icons.Default.ChevronRight, "سال قبل", tint = accent) }
                    Text(faNumber(visibleYear), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { visibleYear++; selectedDay = 1 }) { Icon(Icons.Default.ChevronLeft, "سال بعد", tint = accent) }
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonthNames.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowMonths.forEachIndexed { colIndex, monthName ->
                                    val monthIndex = rowIndex * 3 + colIndex
                                    val active = monthIndex == visibleMonth
                                    Box(Modifier.weight(1f).background(if (active) accent.copy(alpha = .12f) else CanvasColor, RoundedCornerShape(14.dp))
                                        .clickable { visibleMonth = monthIndex; selectedDay = 1; calendarMode = "ماه" }
                                        .padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                                        Text(monthName, color = if (active) accent else Ink, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "هفته" -> {
                val selectedCalendar = persianCalendar().apply {
                    set(Calendar.YEAR, visibleYear); set(Calendar.MONTH, visibleMonth); set(Calendar.DAY_OF_MONTH, selectedDay)
                }
                val weekdayOffset = selectedCalendar.get(Calendar.DAY_OF_WEEK) % 7
                val weekStart = (selectedCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -weekdayOffset) }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(MonthNames[visibleMonth] + " " + faNumber(visibleYear), color = Ink, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { index ->
                                val dayCal = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, index) }
                                val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
                                val dayMonth = dayCal.get(Calendar.MONTH)
                                val dayYear = dayCal.get(Calendar.YEAR)
                                val isChosen = dayYear == visibleYear && dayMonth == visibleMonth && dayNum == selectedDay
                                val isFriday = index == 6
                                Column(Modifier.weight(1f).background(if (isChosen) accent else CanvasColor, RoundedCornerShape(12.dp))
                                    .clickable { visibleYear = dayYear; visibleMonth = dayMonth; selectedDay = dayNum }
                                    .padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(listOf("ش","ی","د","س","چ","پ","ج")[index], color = if (isChosen) Color.White.copy(alpha=.8f) else if (isFriday) Color(0xFFD13F50) else Muted, fontSize = 10.sp)
                                    Text(faNumber(dayNum), color = if (isChosen) Color.White else if (isFriday || IranCalendarEvents.isHoliday(dayYear, dayMonth + 1, dayNum)) Color(0xFFD13F50) else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                        Text("برای دیدن جزئیات هر روز، روی آن بزنید.", color = Muted, fontSize = 11.sp)
                    }
                }
            }
            else -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { moveMonth(-1) }) { Icon(Icons.Default.ChevronRight, "ماه قبل", tint = accent, modifier = Modifier.size(30.dp)) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(MonthNames[visibleMonth] + " " + faNumber(visibleYear), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("تقویم ماهانه", color = Muted, fontSize = 10.sp)
                    }
                    IconButton(onClick = { moveMonth(1) }) { Icon(Icons.Default.ChevronLeft, "ماه بعد", tint = accent, modifier = Modifier.size(30.dp)) }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEachIndexed { index, label ->
                        Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, color = if (index == 6) Color(0xFFD45D68) else Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(6.dp))
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEachIndexed { columnIndex, value ->
                            val isSelected = value != 0 && value == selectedDay
                            val isToday = value != 0 && visibleYear == todayYear && visibleMonth == todayMonth && value == todayDay
                            val isHoliday = value != 0 && IranCalendarEvents.isHoliday(visibleYear, visibleMonth + 1, value)
                            val isFriday = columnIndex == 6
                            val redDay = isFriday || isHoliday
                            Box(Modifier.weight(1f).padding(2.dp).aspectRatio(0.94f).then(if (isSelected) Modifier.background(if (redDay) Color(0xFFD13F50) else accent, CircleShape) else Modifier)
                                .clickable(enabled = value != 0) { selectedDay = value }, contentAlignment = Alignment.Center) {
                                if (value != 0) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(faNumber(value), color = when { isSelected -> Color.White; isToday -> accent; redDay -> Color(0xFFD13F50); else -> Ink },
                                            fontSize = 17.sp, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium)
                                        if ((isToday || redDay) && !isSelected) Box(Modifier.padding(top = 1.dp).size(4.dp).background(if (redDay) Color(0xFFD13F50) else accent, CircleShape))
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        if (calendarMode == "ماه") {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("روزهای مهم این ماه", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("مناسبت‌های رسمی، فرهنگی و ملی", color = Muted, fontSize = 11.sp)
                }
                Text(faNumber(monthOccasions.size) + " مناسبت", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                if (monthOccasions.isEmpty()) {
                    Text("برای این ماه هنوز مناسبت ثبت نشده است.", Modifier.fillMaxWidth().padding(18.dp), color = Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
                } else {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                        monthOccasions.forEachIndexed { index, entry ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(38.dp).background(if (entry.second.isHoliday) Color(0xFFFFE8EA) else accent.copy(alpha = .10f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text(faNumber(entry.first), color = if (entry.second.isHoliday) Color(0xFFC43F50) else accent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                                Column(Modifier.weight(1f).padding(start = 11.dp)) {
                                    Text(entry.second.title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(if (entry.second.isHoliday) "تعطیل رسمی" else "مناسبت", color = if (entry.second.isHoliday) Color(0xFFC43F50) else Muted, fontSize = 10.sp)
                                }
                                Text("›", color = Muted, fontSize = 22.sp)
                            }
                            if (index != monthOccasions.lastIndex) HorizontalDivider(color = Muted.copy(alpha = .12f))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
                visibleYear = todayYear
                visibleMonth = todayMonth
                selectedDay = todayDay
            }) { Text("بازگشت به امروز", color = accent, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(4.dp))
        if (officialEvents.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("مناسبت‌های رسمی", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    officialEvents.forEach { occasion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("•", color = if (occasion.isHoliday) Color(0xFFD45D68) else accent, fontSize = 20.sp)
                            Text(occasion.title, Modifier.padding(start = 8.dp), color = Ink, fontSize = 13.sp)
                            if (occasion.isHoliday) {
                                Spacer(Modifier.weight(1f))
                                Text("تعطیل", color = Color(0xFFD45D68), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("رویدادها و یادآورها", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { eventTitle = ""; reminderTime = "09:00"; repeatDaily = false; showAddEvent = true }) { Text("افزودن رویداد", color = accent) }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            if (events.isEmpty()) {
                Text("برای این روز رویدادی ثبت نشده است.", Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp)
            } else {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
                    events.forEach { event ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("•", color = accent, fontSize = 20.sp)
                            Text(event, Modifier.weight(1f).padding(start = 8.dp), color = Ink, fontSize = 14.sp)
                            Text("حذف", color = Color(0xFFD45D68), fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    events = events - event
                                    prefs.edit().putStringSet("events_$eventKey", events.toSet()).apply()
                                    EventReminderScheduler.cancel(context, (eventKey + "-" + event).hashCode())
                                }.padding(6.dp))
                        }
                    }
                }
            }
        }
        if (showAddEvent) {
            AlertDialog(
                onDismissRequest = { showAddEvent = false },
                title = { Text("رویداد جدید") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it.take(80) },
                            label = { Text("عنوان رویداد") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reminderTime,
                            onValueChange = { reminderTime = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5) },
                            label = { Text("ساعت یادآوری (مثلاً 09:30)") },
                            singleLine = true
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = repeatDaily, onCheckedChange = { repeatDaily = it })
                            Text("تکرار روزانه یادآور", color = Ink, fontSize = 12.sp)
                        }
                        Text("می‌توانی یادآور را برای هر روز تکرار کنی؛ اعلان‌ها باید در گوشی مجاز باشند.", color = Muted, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val title = eventTitle.trim()
                        if (title.isNotEmpty()) {
                            events = (events + title).distinct()
                            prefs.edit().putStringSet("events_$eventKey", events.toSet()).apply()
                            val parts = reminderTime.split(":")
                            val hour = parts.getOrNull(0)?.toIntOrNull()
                            val minute = parts.getOrNull(1)?.toIntOrNull()
                            if (hour != null && hour in 0..23 && minute != null && minute in 0..59) {
                                val alarmDate = persianCalendar().apply {
                                    set(Calendar.YEAR, visibleYear); set(Calendar.MONTH, visibleMonth); set(Calendar.DAY_OF_MONTH, selectedDay)
                                    set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                if (alarmDate > System.currentTimeMillis()) {
                                    val reminderId = (eventKey + "-" + title).hashCode()
                                    if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        pendingReminder = Triple(reminderId, title, alarmDate) to repeatDaily
                                        reminderPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        EventReminderScheduler.schedule(context, reminderId, title, alarmDate, repeatDaily = repeatDaily)
                                    }
                                }
                            }
                            showAddEvent = false
                        }
                    }) { Text("ذخیره") }
                },
                dismissButton = { TextButton(onClick = { showAddEvent = false }) { Text("انصراف") } }
            )
        }
    }
}


@Composable
private fun DateConverter(accent: Color) {
    val initialMillis = remember { System.currentTimeMillis() }
    val initialPersian = remember { persianCalendar().apply { timeInMillis = initialMillis } }
    val initialGregorian = remember {
        GregorianCalendar(java.util.TimeZone.getTimeZone("Asia/Tehran"), Locale.US).apply { timeInMillis = initialMillis }
    }
    var direction by remember { mutableIntStateOf(0) }
    var jy by remember { mutableStateOf(faNumber(initialPersian.get(Calendar.YEAR))) }
    var jm by remember { mutableIntStateOf(initialPersian.get(Calendar.MONTH) + 1) }
    var jd by remember { mutableStateOf(faNumber(initialPersian.get(Calendar.DAY_OF_MONTH))) }
    var gy by remember { mutableStateOf(initialGregorian.get(Calendar.YEAR).toString()) }
    var gm by remember { mutableIntStateOf(initialGregorian.get(Calendar.MONTH) + 1) }
    var gd by remember { mutableStateOf(initialGregorian.get(Calendar.DAY_OF_MONTH).toString()) }
    var result by remember { mutableStateOf<String?>(null) }
    var resultDate by remember { mutableStateOf("") }
    var resultGregorian by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    fun num(value: String): Int? = value.map {
        if (it in '۰'..'۹') ('0'.code + it.code - '۰'.code).toChar() else it
    }.joinToString("").toIntOrNull()
    fun showResult(millis: Long) {
        val p = persianCalendar().apply { timeInMillis = millis }
        val g = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("fa", "IR")).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Tehran")
        }.format(java.util.Date(millis))
        resultDate = faNumber(p.get(Calendar.DAY_OF_MONTH)) + " " + MonthNames[p.get(Calendar.MONTH)] + " " + faNumber(p.get(Calendar.YEAR))
        resultGregorian = g
        val occasions = IranCalendarEvents.forDate(p.get(Calendar.YEAR), p.get(Calendar.MONTH) + 1, p.get(Calendar.DAY_OF_MONTH))
        result = if (occasions.isEmpty()) "برای این تاریخ مناسبتی در فهرست ثبت‌شده برنامه پیدا نشد."
            else occasions.joinToString("  •  ") { it.title } + if (occasions.any { it.isHoliday }) "  ·  تعطیل رسمی" else ""
        error = ""
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("تبدیل تاریخ", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("یک تاریخ را انتخاب کن؛ معادل دقیق آن را در تقویم دیگر ببین.", color = Muted, fontSize = 12.sp)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3547)),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Box(Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF1D3547), Color(0xFF31566A), Color(0xFF426D70))),
                RoundedCornerShape(28.dp)
            ).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("DATE CONVERTER", color = Color.White.copy(alpha = .65f), fontSize = 9.sp, letterSpacing = 2.sp)
                            Text("از هر تقویم به تقویم دیگر", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = .13f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CompareArrows, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                    Text("تبدیل سریع، همراه با مناسبت‌های همان روز", color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            listOf("شمسی به میلادی", "میلادی به شمسی").forEachIndexed { index, label ->
                Box(
                    Modifier.weight(1f).background(if (direction == index) accent else Color.Transparent, RoundedCornerShape(14.dp))
                        .clickable { direction = index; error = ""; result = null }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (direction == index) Color.White else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CalendarMonth, null, tint = accent)
                    }
                    Column(Modifier.padding(start = 10.dp)) {
                        Text(if (direction == 0) "تاریخ هجری شمسی" else "تاریخ میلادی", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(if (direction == 0) "سال، ماه و روز شمسی را وارد کن" else "سال، ماه و روز میلادی را وارد کن", color = Muted, fontSize = 11.sp)
                    }
                }
                if (direction == 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateField("سال", jy, { jy = it }, Modifier.weight(1.1f))
                        PersianMonthMenu(jm, { jm = it }, accent, Modifier.weight(1.2f))
                        DateField("روز", jd, { jd = it }, Modifier.weight(.8f))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateField("سال", gy, { gy = it }, Modifier.weight(1.1f))
                        GregorianMonthMenu(gm, { gm = it }, accent, Modifier.weight(1.2f))
                        DateField("روز", gd, { gd = it }, Modifier.weight(.8f))
                    }
                }
                Button(
                    onClick = {
                        if (direction == 0) {
                            val y = num(jy); val d = num(jd)
                            if (y != null && y in 1..3000 && d != null && d in 1..31) {
                                runCatching {
                                    val c = persianCalendar().apply { isLenient = false; clear(); set(y, jm - 1, d) }
                                    c.timeInMillis
                                    showResult(c.timeInMillis)
                                }.onFailure { error = "تاریخ شمسی واردشده معتبر نیست." }
                            } else error = "سال و روز را بررسی کن."
                        } else {
                            val y = num(gy); val d = num(gd)
                            if (y != null && y in 1..9999 && d != null && d in 1..31) {
                                runCatching {
                                    val c = GregorianCalendar(java.util.TimeZone.getTimeZone("Asia/Tehran"), Locale.US)
                                        .apply { isLenient = false; clear(); set(y, gm - 1, d) }
                                    c.timeInMillis
                                    showResult(c.timeInMillis)
                                }.onFailure { error = "تاریخ میلادی واردشده معتبر نیست." }
                            } else error = "سال و روز را بررسی کن."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("محاسبه و نمایش نتیجه", fontWeight = FontWeight.Bold)
                }
            }
        }
        if (error.isNotEmpty()) {
            Text(error, color = Color(0xFFD13F50), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = if (result == null) accent.copy(alpha = .09f) else MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = accent)
                    Text("نتیجه تبدیل", Modifier.padding(start = 8.dp), color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                if (result == null) {
                    Text("پس از محاسبه، تاریخ معادل و رویدادهای ثبت‌شده همان روز در این بخش نمایش داده می‌شود.", color = Muted, fontSize = 12.sp)
                } else {
                    Text(resultDate, color = accent, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text(resultGregorian, color = Muted, fontSize = 12.sp)
                    HorizontalDivider(color = Muted.copy(alpha = .18f))
                    Text(result.orEmpty(), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PersianMonthMenu(selected: Int, onSelect: (Int) -> Unit, accent: Color, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text(MonthNames[selected - 1], maxLines = 1)
            Text("⌄", modifier = Modifier.padding(start = 3.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            MonthNames.forEachIndexed { index, month ->
                DropdownMenuItem(text = { Text(month) }, onClick = { onSelect(index + 1); expanded = false })
            }
        }
    }
}
private val GregorianMonthNames = listOf("ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر")
@Composable
private fun GregorianMonthMenu(selected: Int, onSelect: (Int) -> Unit, accent: Color, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text(GregorianMonthNames[selected - 1], maxLines = 1)
            Text("⌄", modifier = Modifier.padding(start = 3.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GregorianMonthNames.forEachIndexed { index, month ->
                DropdownMenuItem(text = { Text(month) }, onClick = { onSelect(index + 1); expanded = false })
            }
        }
    }
}
@Composable
private fun DateField(label:String,value:String,onValue:(String)->Unit,modifier:Modifier=Modifier){
    OutlinedTextField(value=value,onValueChange={onValue(it.filter{ch->ch.isDigit() || ch in '۰'..'۹'}.take(4))},label={Text(label)},modifier=modifier,singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
}
private data class PrayerMoment(val title: String, val atMillis: Long)

private fun calculatePrayerMoments(city: IranianCity, methodIndex: Int, dateCalendar: GregorianCalendar): List<PrayerMoment> {
    val date = DateComponents(dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH) + 1, dateCalendar.get(Calendar.DAY_OF_MONTH))
    val coordinates = Coordinates(latitude = city.latitude, longitude = city.longitude, altitude = 0.0)
    val params = when (methodIndex) {
        1 -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters()
        2 -> CalculationMethod.KARACHI.parameters()
        else -> CalculationMethod.OTHER.parameters().copy(
            fajrAngle = 17.7,
            maghribIsInterval = false,
            maghribValue = 4.5,
            ishaIsInterval = false,
            ishaValue = 14.0,
            madhab = Madhab.SHAFI
        )
    }
    val times = PrayerTimes(
        coordinates = coordinates,
        dateComponents = date,
        calculationParameters = params,
        utcOffset = ZoneOffset.ofHoursMinutes(3, 30),
        countryCode = "IR",
        cityName = city.name
    )
    return listOf(
        "اذان صبح" to times.fajr,
        "اذان ظهر" to times.dhuhr,
        "اذان عصر" to times.asr,
        "اذان مغرب" to times.maghrib,
        "اذان عشا" to times.isha
    ).mapNotNull { (label, time) -> time?.let { PrayerMoment(label, it.toInstant().toEpochMilli()) } }
}

private data class IranianCity(val name: String, val latitude: Double, val longitude: Double)
private val IranianCities = listOf(
    IranianCity("تهران", 35.6892, 51.3890),
    IranianCity("مشهد", 36.2605, 59.6168),
    IranianCity("اصفهان", 32.6546, 51.6680),
    IranianCity("شیراز", 29.5918, 52.5837),
    IranianCity("تبریز", 38.0962, 46.2738),
    IranianCity("اهواز", 31.3183, 48.6706),
    IranianCity("قم", 34.6416, 50.8746),
    IranianCity("کرمان", 30.2839, 57.0834),
    IranianCity("رشت", 37.2808, 49.5832),
    IranianCity("یزد", 31.8974, 54.3569)
)

@Composable
private fun PrayerTimesPage(accent: Color) {
    val context = LocalContext.current
    val prayerPrefs = remember { context.getSharedPreferences("daynegar_prayer", 0) }
    var cityIndex by remember { mutableIntStateOf(prayerPrefs.getInt("city_index", 0).coerceIn(0, IranianCities.lastIndex)) }
    var methodIndex by remember { mutableIntStateOf(prayerPrefs.getInt("method_index", 0).coerceIn(0, 2)) }
    var cityMenu by remember { mutableStateOf(false) }
    var methodMenu by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var adhanPreviewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            adhanPreviewPlayer?.release()
            adhanPreviewPlayer = null
        }
    }
    LaunchedEffect(Unit) {
        while (true) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(60_000) }
    }
    val city = IranianCities[cityIndex]
    val methodNames = listOf("روش رسمی تهران", "مسلمانان جهان", "کراچی")
    val prayerMoments = remember(now, cityIndex, methodIndex) {
        runCatching {
            val calendar = GregorianCalendar(java.util.TimeZone.getTimeZone("Asia/Tehran")).apply { timeInMillis = now }
            calculatePrayerMoments(city, methodIndex, calendar)
        }.getOrElse { emptyList() }
    }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale("fa", "IR")) }
    val prayerData = prayerMoments.map { moment ->
        moment.title to formatter.format(java.time.Instant.ofEpochMilli(moment.atMillis).atOffset(ZoneOffset.ofHoursMinutes(3, 30)))
    }
    var pendingPrayerReminders by remember { mutableStateOf<List<PrayerMoment>>(emptyList()) }
    var scheduledPrayerReminderCount by remember { mutableIntStateOf(0) }
    val prayerNotificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingPrayerReminders.forEach { reminder ->
                EventReminderScheduler.schedule(context, reminder.atMillis.hashCode(), reminder.title, reminder.atMillis, playAdhan = true)
            }
            scheduledPrayerReminderCount = pendingPrayerReminders.size
        } else {
            scheduledPrayerReminderCount = 0
        }
        pendingPrayerReminders = emptyList()
    }
    fun schedulePrayerWeek() {
        val schedules = mutableListOf<PrayerMoment>()
        val zone = java.util.TimeZone.getTimeZone("Asia/Tehran")
        repeat(7) { offset ->
            val day = GregorianCalendar(zone).apply { timeInMillis = now; add(Calendar.DAY_OF_MONTH, offset) }
            schedules += calculatePrayerMoments(city, methodIndex, day).filter { it.atMillis > now }
                .map { it.copy(title = it.title + " · " + city.name) }
        }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingPrayerReminders = schedules
            prayerNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            schedules.forEach { reminder ->
                EventReminderScheduler.schedule(context, reminder.atMillis.hashCode(), reminder.title, reminder.atMillis, playAdhan = true)
            }
            scheduledPrayerReminderCount = schedules.size
        }
    }
    val dateLabel = remember(now) {
        val c = persianCalendar().apply { timeInMillis = now }
        faNumber(c.get(Calendar.DAY_OF_MONTH)) + " " + MonthNames[c.get(Calendar.MONTH)] + " " + faNumber(c.get(Calendar.YEAR))
    }
    val nextPrayer = prayerMoments.firstOrNull { it.atMillis > now }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("اوقات شرعی", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text(dateLabel + " · محاسبه بر پایه موقعیت شهر انتخابی", color = Muted, fontSize = 12.sp)
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = accent)) {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent, accent.copy(alpha = .78f), Color(0xFF263653))), RoundedCornerShape(28.dp)).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("اوقات شرعی امروز", color = Color.White.copy(alpha = .76f), fontSize = 11.sp)
                            Text(city.name, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Icon(Icons.Default.WbSunny, null, tint = Color.White, modifier = Modifier.size(34.dp))
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = .22f))
                    Text(if (nextPrayer != null) "نماز بعدی · " + nextPrayer.title else "برنامه امروز به پایان رسیده است", color = Color.White.copy(alpha = .82f), fontSize = 12.sp)
                    Text(nextPrayer?.let { formatter.format(java.time.Instant.ofEpochMilli(it.atMillis).atOffset(ZoneOffset.ofHoursMinutes(3, 30))) } ?: "—", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("منطقه زمانی رسمی ایران · UTC+03:30", color = Color.White.copy(alpha = .7f), fontSize = 10.sp)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("برنامه نمازهای امروز", Modifier.weight(1f), color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("به وقت محلی", color = Muted, fontSize = 10.sp)
                }
                if (prayerData.isEmpty()) Text("برای این موقعیت امکان محاسبه زمان‌ها وجود ندارد.", color = Muted, modifier = Modifier.padding(12.dp))
                else prayerData.forEachIndexed { index, pair ->
                    val upcoming = prayerMoments.getOrNull(index)?.atMillis?.let { it > now } == true && prayerMoments.take(index).none { it.atMillis > now }
                    Row(Modifier.fillMaxWidth().background(if (upcoming) accent.copy(alpha = .09f) else Color.Transparent, RoundedCornerShape(15.dp)).padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).background(if (upcoming) accent.copy(alpha = .16f) else CanvasColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WbSunny, null, tint = if (upcoming) accent else Muted, modifier = Modifier.size(18.dp))
                        }
                        Text(pair.first, Modifier.weight(1f).padding(start = 11.dp), color = Ink, fontWeight = if (upcoming) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                        Text(pair.second, color = if (upcoming) accent else Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تنظیمات محاسبه", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("شهر و روش محاسبه را انتخاب کن تا زمان‌ها دقیق‌تر نمایش داده شوند.", color = Muted, fontSize = 11.sp)
                Text("شهر محل سکونت", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { cityMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                        Text(city.name, modifier = Modifier.weight(1f), textAlign = TextAlign.Start); Text("⌄")
                    }
                    DropdownMenu(expanded = cityMenu, onDismissRequest = { cityMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                        IranianCities.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = { cityIndex = index; prayerPrefs.edit().putInt("city_index", index).apply(); cityMenu = false })
                        }
                    }
                }
                Text("روش محاسبه", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { methodMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                        Text(methodNames[methodIndex], modifier = Modifier.weight(1f), textAlign = TextAlign.Start); Text("⌄")
                    }
                    DropdownMenu(expanded = methodMenu, onDismissRequest = { methodMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                        methodNames.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { methodIndex = index; prayerPrefs.edit().putInt("method_index", index).apply(); methodMenu = false })
                        }
                    }
                }
            }
        }
        Button(onClick = { schedulePrayerWeek() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
            Text("فعال‌سازی اعلان و اذان ۷ روز آینده", fontWeight = FontWeight.Bold)
        }
        if (scheduledPrayerReminderCount > 0) Text("${faNumber(scheduledPrayerReminderCount)} اعلان برای شهر ${city.name} تنظیم شد.", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = {
            val current = adhanPreviewPlayer
            if (current?.isPlaying == true) { current.stop(); current.release(); adhanPreviewPlayer = null }
            else {
                val player = MediaPlayer.create(context, R.raw.adhan)
                if (player != null) {
                    adhanPreviewPlayer = player
                    player.setOnCompletionListener { it.release(); adhanPreviewPlayer = null }
                    player.start()
                }
            }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
            Text(if (adhanPreviewPlayer?.isPlaying == true) "توقف پخش اذان" else "شنیدن نمونه صدای اذان")
        }
        Card(colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .08f)), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("درباره محاسبه", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("روش تهران: فجر ۱۷٫۷°، مغرب ۴٫۵° و عشا ۱۴° زیر افق. اختلاف جزئی با جدول‌های محلی ممکن است به‌دلیل مختصات و گردکردن رخ دهد.", color = Muted, fontSize = 11.sp)
                Text("صدای اذان در کانال اعلان مستقل Android پخش می‌شود و از تنظیمات اعلان گوشی قابل کنترل است.", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SettingsPage(
    themeIndex: Int, onTheme: (Int) -> Unit, darkMode: Boolean, onDarkMode: (Boolean) -> Unit,
    fontChoice: Int, onFont: (Int) -> Unit, accent: Color
) {
    val palette = listOf(Color(0xFF7255C7), Color(0xFF087E9B), Color(0xFF16845B), Color(0xFFB65C83), Color(0xFFB58A35))
    val names = listOf("بنفش سلطنتی", "آبی اقیانوسی", "سبز زمردی", "صورتی رزگلد", "کهربایی")
    val fontNames = listOf("فونت سیستم", "وزیرمتن", "استعداد", "ساحل")
    val fontSamples = listOf("نمونه نمایش متن فارسی", "روزنگار؛ همراه هر روز", "تقویم هوشمند زندگی", "لحظه‌ها را به خاطر بسپار")
    val surface = MaterialTheme.colorScheme.surface
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("تنظیمات روزنگار", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text("تجربه تقویم را مطابق سلیقه و نیازت شخصی‌سازی کن.", color = Muted, fontSize = 12.sp)
        }
        Card(colors = CardDefaults.cardColors(containerColor = accent), shape = RoundedCornerShape(25.dp)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(50.dp).background(Color.White.copy(alpha = .16f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(27.dp))
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("ظاهر دلخواه شما", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text("رنگ، حالت شب و قلم برنامه", color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(23.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("پالت رنگی برنامه", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("با انتخاب هر رنگ، اجزای اصلی برنامه هماهنگ تغییر می‌کنند.", color = Muted, fontSize = 11.sp)
                names.forEachIndexed { index, name ->
                    Row(Modifier.fillMaxWidth().clickable { onTheme(index) }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).background(Brush.linearGradient(listOf(palette[index], palette[index].copy(alpha = .55f))), RoundedCornerShape(12.dp)))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(if (themeIndex == index) "انتخاب‌شده" else "برای اعمال لمس کن", color = Muted, fontSize = 10.sp)
                        }
                        RadioButton(selected = themeIndex == index, onClick = { onTheme(index) }, colors = RadioButtonDefaults.colors(selectedColor = accent))
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(23.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("حالت نمایش", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(accent.copy(alpha = .10f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.WbSunny, null, tint = accent) }
                    Column(Modifier.weight(1f).padding(start = 11.dp)) {
                        Text("حالت تاریک", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("زمینه تیره برای استفاده در شب", color = Muted, fontSize = 10.sp)
                    }
                    Switch(checked = darkMode, onCheckedChange = onDarkMode)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(23.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("فونت و خوانایی", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("سه فونت فارسی همراه برنامه؛ بدون نیاز به اینترنت.", color = Muted, fontSize = 11.sp)
                fontNames.forEachIndexed { index, label ->
                    Column(Modifier.fillMaxWidth().background(if (fontChoice == index) accent.copy(alpha = .08f) else CanvasColor, RoundedCornerShape(15.dp))
                        .clickable { onFont(index) }.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            RadioButton(selected = fontChoice == index, onClick = { onFont(index) }, colors = RadioButtonDefaults.colors(selectedColor = accent))
                        }
                        Text(fontSamples[index], color = if (fontChoice == index) accent else Muted, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(23.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("درباره روزنگار", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = accent)
                    Text("روزنگار · نسخه ۱٫۲٫۰", Modifier.padding(start = 9.dp), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Text("تقویم شمسی، مناسبت‌های ایران، تبدیل تاریخ، رویدادها و اوقات شرعی در یک همراه روزانه.", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

