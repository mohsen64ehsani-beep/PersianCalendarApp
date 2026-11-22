package ir.persiancalendar.app

import android.os.Bundle
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

private val Ink = Color(0xFF202033)
private val Muted = Color(0xFF77778A)
private val Purple = Color(0xFF7255C7)
private val CanvasColor = Color(0xFFF6F4FA)
private val MonthNames = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
private val WeekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
private val PersianDigits = "۰۱۲۳۴۵۶۷۸۹"

private fun faNumber(value: Int): String =
    value.toString().map { if (it in '0'..'9') PersianDigits[it - '0'] else it }.joinToString("")

private fun persianCalendar(): Calendar =
    Calendar.getInstance(android.icu.util.TimeZone.getTimeZone("Asia/Tehran"), ULocale("fa_IR@calendar=persian"))

@Composable
private fun PersianCalendarApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
            containerColor = CanvasColor,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(selectedTab == 0, { selectedTab = 0 }, icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("تقویم") })
                    NavigationBarItem(selectedTab == 1, { selectedTab = 1 }, icon = { Icon(Icons.Default.CompareArrows, null) }, label = { Text("تبدیل تاریخ") })
                    NavigationBarItem(selectedTab == 2, { selectedTab = 2 }, icon = { Icon(Icons.Default.WbSunny, null) }, label = { Text("اوقات شرعی") })
                    NavigationBarItem(selectedTab == 3, { selectedTab = 3 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("تنظیمات") })
                }
            }
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = CanvasColor) {
                when (selectedTab) {
                    0 -> CalendarHome()
                    1 -> PlaceholderPage("تبدیل تاریخ", "تبدیل دوطرفه تاریخ شمسی و میلادی")
                    2 -> PlaceholderPage("اوقات شرعی", "انتخاب شهر و محاسبه زمان‌های شرعی")
                    else -> PlaceholderPage("تنظیمات", "پوسته، فونت، اعلان‌ها و پخش اذان")
                }
            }
        }
    }
}

@Composable
private fun CalendarHome() {
    val today = remember {
        persianCalendar().apply { timeInMillis = System.currentTimeMillis() }
    }
    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    var visibleYear by remember { mutableIntStateOf(todayYear) }
    var visibleMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedDay by remember { mutableIntStateOf(todayDay) }

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

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("تقویم روزنگار", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("تقویم فارسی و زمان ایران", color = Muted, fontSize = 13.sp)
            }
            Box(Modifier.size(46.dp).background(Purple.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CalendarMonth, null, tint = Purple)
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 17.dp, vertical = 14.dp)) {
                Text(if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) "امروز" else "تاریخ انتخاب‌شده", color = Muted, fontSize = 13.sp)
                Text(faNumber(selectedDay) + " " + MonthNames[visibleMonth] + " " + faNumber(visibleYear), color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("بر اساس ساعت رسمی ایران", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { moveMonth(-1) }) { Icon(Icons.Default.ChevronRight, "ماه قبل", tint = Purple, modifier = Modifier.size(30.dp)) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(MonthNames[visibleMonth] + " " + faNumber(visibleYear), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("برای جابه‌جایی بین ماه‌ها از فلش‌ها استفاده کنید", color = Muted, fontSize = 10.sp)
            }
            IconButton(onClick = { moveMonth(1) }) { Icon(Icons.Default.ChevronLeft, "ماه بعد", tint = Purple, modifier = Modifier.size(30.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            WeekDays.forEach { name ->
                Text(name.take(1), Modifier.weight(1f), textAlign = TextAlign.Center, color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth(), userScrollEnabled = false) {
            items(cells) { value ->
                val isSelected = value != 0 && value == selectedDay
                Box(
                    Modifier.fillMaxWidth().padding(2.dp).aspectRatio(0.94f).then(
                        if (isSelected) Modifier.background(Purple, CircleShape) else Modifier
                    ).clickable(enabled = value != 0) { selectedDay = value },
                    contentAlignment = Alignment.Center
                ) {
                    if (value != 0) {
                        Text(
                            faNumber(value),
                            color = if (isSelected) Color.White else Ink,
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
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
            }) { Text("بازگشت به امروز", color = Purple, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(4.dp))
        Text("رویدادها و یادآورها", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Text("هنوز رویدادی برای این روز ثبت نشده است.", Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PlaceholderPage(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Text("در حال توسعه", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
