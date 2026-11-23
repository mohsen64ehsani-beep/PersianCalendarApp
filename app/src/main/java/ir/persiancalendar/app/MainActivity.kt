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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PersianCalendarApp() }
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
    val preferences = remember { context.getSharedPreferences("daynegar", 0) }
    var themeIndex by remember { mutableIntStateOf(preferences.getInt("theme", 0).coerceIn(0, 4)) }
    var darkMode by remember { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
    var fontChoice by remember { mutableIntStateOf(preferences.getInt("font_choice", 0).coerceIn(0, 2)) }
    val accent = listOf(Purple, Color(0xFF087E9B), Color(0xFF16845B), Color(0xFFB65C83), Color(0xFFB58A35))[themeIndex]
    val palette = if (darkMode) DayPalette(Color(0xFFF1EFF8), Color(0xFFB8B4C7), Color(0xFF15141B), Color(0xFF24222D))
        else DayPalette(Color(0xFF202033), Color(0xFF77778A), Color(0xFFF6F4FA), Color.White)
    val colors = if (darkMode) darkColorScheme(primary = accent, secondary = accent, tertiary = accent,
        background = palette.canvas, surface = palette.surface, onBackground = palette.ink, onSurface = palette.ink)
        else lightColorScheme(primary = accent, secondary = accent, tertiary = accent,
        background = palette.canvas, surface = palette.surface, onBackground = palette.ink, onSurface = palette.ink)
    val selectedFont = when (fontChoice) { 1 -> androidx.compose.ui.text.font.FontFamily.Serif; 2 -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.SansSerif }
    CompositionLocalProvider(LocalDayPalette provides palette, androidx.compose.material3.LocalContentColor provides palette.ink) {
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = selectedFont)) {
        Scaffold(
            containerColor = CanvasColor,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(selectedTab == 0, { selectedTab = 0 }, icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("تقویم") })
                    NavigationBarItem(selectedTab == 1, { selectedTab = 1 }, icon = { Icon(Icons.Default.CompareArrows, null) }, label = { Text("تبدیل تاریخ") })
                    NavigationBarItem(selectedTab == 2, { selectedTab = 2 }, icon = { Icon(Icons.Default.WbSunny, null) }, label = { Text("اوقات شرعی") })
                    NavigationBarItem(selectedTab == 3, { selectedTab = 3 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("تنظیمات") })
                }
            }
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = CanvasColor) {
                when (selectedTab) {
                    0 -> CalendarHome(accent)
                    1 -> DateConverter(accent)
                    2 -> PrayerTimesPage(accent)
                    else -> SettingsPage(themeIndex, { themeIndex = it; preferences.edit().putInt("theme", it).apply() }, darkMode, { darkMode = it; preferences.edit().putBoolean("dark_mode", it).apply() }, fontChoice, { fontChoice = it; preferences.edit().putInt("font_choice", it).apply() }, accent)
                }
            }
        }
        }
    }
    }
}

@Composable
private fun CalendarHome(accent: Color) {
    val context = LocalContext.current
    var todayTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
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
        SimpleDateFormat("HH:mm", Locale("fa", "IR")).apply {
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
    var showAddEvent by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("09:00") }
    var pendingReminder by remember { mutableStateOf<Triple<Int, String, Long>?>(null) }
    val reminderPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val pending = pendingReminder
        if (granted && pending != null) EventReminderScheduler.schedule(context, pending.first, pending.second, pending.third)
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
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.78f))),
                RoundedCornerShape(26.dp)
            ).padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) "امروز" else "تاریخ انتخاب‌شده",
                        color = Color.White.copy(alpha = .82f), fontSize = 13.sp)
                    Icon(Icons.Default.WbSunny, null, tint = Color.White.copy(alpha = .9f))
                }
                Text(faNumber(selectedDay) + " " + MonthNames[visibleMonth], color = Color.White,
                    fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                Text(faNumber(visibleYear), color = Color.White.copy(alpha = .92f), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(selectedGregorian, color = Color.White.copy(alpha = .82f), fontSize = 12.sp)
                if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) {
                    Text("●  امروز · ساعت ایران  $iranClock", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
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
                                        .clickable { visibleMonth = monthIndex; calendarMode = "ماه" }
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
                                Column(Modifier.weight(1f).background(if (isChosen) accent else CanvasColor, RoundedCornerShape(12.dp))
                                    .clickable { visibleYear = dayYear; visibleMonth = dayMonth; selectedDay = dayNum }
                                    .padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(listOf("ش","ی","د","س","چ","پ","ج")[index], color = if (isChosen) Color.White.copy(alpha=.8f) else Muted, fontSize = 10.sp)
                                    Text(faNumber(dayNum), color = if (isChosen) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        week.forEach { value ->
                            val isSelected = value != 0 && value == selectedDay
                            val isToday = value != 0 && visibleYear == todayYear && visibleMonth == todayMonth && value == todayDay
                            val isHoliday = value != 0 && IranCalendarEvents.isHoliday(visibleYear, visibleMonth + 1, value)
                            Box(Modifier.weight(1f).padding(2.dp).aspectRatio(0.94f).then(if (isSelected) Modifier.background(accent, CircleShape) else Modifier)
                                .clickable(enabled = value != 0) { selectedDay = value }, contentAlignment = Alignment.Center) {
                                if (value != 0) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(faNumber(value), color = when { isSelected -> Color.White; isToday -> accent; isHoliday -> Color(0xFFD45D68); else -> Ink },
                                            fontSize = 17.sp, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium)
                                        if ((isToday || isHoliday) && !isSelected) Box(Modifier.padding(top = 1.dp).size(4.dp).background(if (isToday) accent else Color(0xFFD45D68), CircleShape))
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
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
            TextButton(onClick = { eventTitle = ""; reminderTime = "09:00"; showAddEvent = true }) { Text("افزودن رویداد", color = accent) }
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
                        Text("با ذخیره رویداد، در صورت مجاز بودن اعلان‌ها، یادآوری در ساعت انتخاب‌شده تنظیم می‌شود.", color = Muted, fontSize = 11.sp)
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
                                        pendingReminder = Triple(reminderId, title, alarmDate)
                                        reminderPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        EventReminderScheduler.schedule(context, reminderId, title, alarmDate)
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
    var jy by remember { mutableStateOf(faNumber(initialPersian.get(Calendar.YEAR))) }
    var jm by remember { mutableStateOf(faNumber(initialPersian.get(Calendar.MONTH) + 1)) }
    var jd by remember { mutableStateOf(faNumber(initialPersian.get(Calendar.DAY_OF_MONTH))) }
    var gy by remember { mutableStateOf(initialGregorian.get(Calendar.YEAR).toString()) }
    var gm by remember { mutableStateOf((initialGregorian.get(Calendar.MONTH) + 1).toString()) }
    var gd by remember { mutableStateOf(initialGregorian.get(Calendar.DAY_OF_MONTH).toString()) }
    var result by remember { mutableStateOf("یکی از تبدیل‌ها را انتخاب کنید.") }
    fun num(value: String): Int? = value.map {
        if (it in '۰'..'۹') ('0'.code + it.code - '۰'.code).toChar() else it
    }.joinToString("").toIntOrNull()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("تبدیل تاریخ", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("تبدیل دقیق دوطرفه با تقویم دستگاه و منطقه زمانی ایران", color = Muted, fontSize = 12.sp)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("هجری شمسی به میلادی", color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    DateField("سال", jy, { jy = it }, Modifier.weight(1.2f))
                    DateField("ماه", jm, { jm = it }, Modifier.weight(1f))
                    DateField("روز", jd, { jd = it }, Modifier.weight(1f))
                }
                Button(onClick = {
                    val y = num(jy); val m = num(jm); val d = num(jd)
                    result = if (y != null && y in 1..3000 && m != null && m in 1..12 && d != null && d in 1..31) runCatching {
                        val c = persianCalendar().apply { isLenient = false; clear(); set(y, m - 1, d) }
                        c.timeInMillis
                        SimpleDateFormat("EEEE، yyyy/MM/dd", Locale("fa", "IR"))
                            .apply { timeZone = java.util.TimeZone.getTimeZone("Asia/Tehran") }.format(c.time)
                    }.getOrElse { "تاریخ شمسی معتبر نیست." } else "سال، ماه و روز را بررسی کنید."
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                    Text("تبدیل به میلادی")
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("میلادی به هجری شمسی", color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    DateField("سال", gy, { gy = it }, Modifier.weight(1.2f))
                    DateField("ماه", gm, { gm = it }, Modifier.weight(1f))
                    DateField("روز", gd, { gd = it }, Modifier.weight(1f))
                }
                Button(onClick = {
                    val y = num(gy); val m = num(gm); val d = num(gd)
                    result = if (y != null && y in 1..9999 && m != null && m in 1..12 && d != null && d in 1..31) runCatching {
                        val c = GregorianCalendar(java.util.TimeZone.getTimeZone("Asia/Tehran"), Locale.US)
                            .apply { isLenient = false; clear(); set(y, m - 1, d) }
                        c.timeInMillis
                        val p = persianCalendar().apply { timeInMillis = c.timeInMillis }
                        faNumber(p.get(Calendar.YEAR)) + "/" + faNumber(p.get(Calendar.MONTH) + 1) + "/" + faNumber(p.get(Calendar.DAY_OF_MONTH))
                    }.getOrElse { "تاریخ میلادی معتبر نیست." } else "سال، ماه و روز را بررسی کنید."
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                    Text("تبدیل به شمسی")
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .12f)), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("نتیجه تبدیل", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text(result, color = Ink, textAlign = TextAlign.Center, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("اوقات شرعی", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(dateLabel + " · ساعت رسمی ایران", color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = accent), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text(city.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("زمان‌های محاسبه‌شده برای امروز", color = Color.White.copy(alpha = .82f), fontSize = 12.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("انتخاب شهر", color = Ink, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { cityMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(city.name, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Text("⌄")
                    }
                    DropdownMenu(expanded = cityMenu, onDismissRequest = { cityMenu = false }) {
                        IranianCities.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = { cityIndex = index; prayerPrefs.edit().putInt("city_index", index).apply(); cityMenu = false })
                        }
                    }
                }
                Text("روش محاسبه", color = Ink, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { methodMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(methodNames[methodIndex], modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Text("⌄")
                    }
                    DropdownMenu(expanded = methodMenu, onDismissRequest = { methodMenu = false }) {
                        methodNames.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { methodIndex = index; prayerPrefs.edit().putInt("method_index", index).apply(); methodMenu = false })
                        }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (prayerData.isEmpty()) {
                    Text("برای این موقعیت امکان محاسبه زمان‌ها وجود ندارد.", color = Muted, modifier = Modifier.padding(12.dp))
                } else {
                    prayerData.forEachIndexed { index, pair ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(pair.first, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Medium)
                            Text(pair.second, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        if (index != prayerData.lastIndex) HorizontalDivider(color = Muted.copy(alpha = .16f))
                    }
                }
            }
        }
        Button(onClick = { schedulePrayerWeek() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
            Text("فعال‌سازی اعلان اوقات شرعی ۷ روز آینده")
        }
        if (scheduledPrayerReminderCount > 0) {
            Text("${faNumber(scheduledPrayerReminderCount)} اعلان برای شهر ${city.name} تنظیم شد.", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Button(onClick = {
            val current = adhanPreviewPlayer
            if (current?.isPlaying == true) {
                current.stop()
                current.release()
                adhanPreviewPlayer = null
            } else {
                val player = MediaPlayer.create(context, R.raw.adhan)
                if (player != null) {
                    adhanPreviewPlayer = player
                    player.setOnCompletionListener {
                        it.release()
                        adhanPreviewPlayer = null
                    }
                    player.start()
                }
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)) {
            Text(if (adhanPreviewPlayer?.isPlaying == true) "توقف پخش اذان" else "پخش نمونه صدای اذان")
        }
        Text("برای اعلان‌های نماز، فایل اذان دارای مجوز CC0 از کانال اعلان جداگانه پخش می‌شود. صدای کانال را می‌توان از تنظیمات اعلان Android کنترل کرد.", color = Muted, fontSize = 11.sp)
        Text("روش تهران: فجر ۱۷٫۷°، مغرب ۴٫۵° و عشا ۱۴° زیر افق. اختلاف دقیقه‌ای ممکن است به‌دلیل مختصات و گردکردن با جدول محلی متفاوت باشد.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsPage(
    themeIndex: Int,
    onTheme: (Int) -> Unit,
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    fontChoice: Int,
    onFont: (Int) -> Unit,
    accent: Color
) {
    val palette = listOf(Color(0xFF7255C7), Color(0xFF087E9B), Color(0xFF16845B), Color(0xFFB65C83), Color(0xFFB58A35))
    val names = listOf("بنفش سلطنتی", "آبی اقیانوسی", "سبز زمردی", "صورتی رزگلد", "مشکی و طلایی")
    val surface = MaterialTheme.colorScheme.surface
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("شخصی‌سازی", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text("ظاهر روزنگار را مطابق سلیقه‌ات تنظیم کن.", color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("رنگ‌بندی برنامه", color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                names.forEachIndexed { index, name ->
                    Row(Modifier.fillMaxWidth().clickable { onTheme(index) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).background(palette[index], CircleShape))
                        Text(name, Modifier.weight(1f).padding(start = 12.dp), color = Ink)
                        RadioButton(selected = themeIndex == index, onClick = { onTheme(index) }, colors = RadioButtonDefaults.colors(selectedColor = accent))
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("حالت نمایش", color = Ink, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("حالت تاریک", color = Ink)
                        Text("زمینه تیره برای استفاده در شب", color = Muted, fontSize = 11.sp)
                    }
                    Switch(checked = darkMode, onCheckedChange = onDarkMode)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("فونت رابط کاربری", color = Ink, fontWeight = FontWeight.Bold)
                Text("انتخاب فونت‌های پایه دستگاه؛ بسته فونت‌های فارسی مجاز در مرحله بعد افزوده می‌شود.", color = Muted, fontSize = 11.sp)
                listOf("ساده", "سریف", "تک‌فاصله").forEachIndexed { index, label ->
                    Row(Modifier.fillMaxWidth().clickable { onFont(index) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.weight(1f), color = Ink)
                        RadioButton(selected = fontChoice == index, onClick = { onFont(index) }, colors = RadioButtonDefaults.colors(selectedColor = accent))
                    }
                }
            }
        }
        Text("روزنگار · نسخه ۰٫۹٫۰", color = Muted, fontSize = 11.sp)
    }
}

