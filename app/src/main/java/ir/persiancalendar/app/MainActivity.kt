package ir.persiancalendar.app

import android.os.Bundle
import android.icu.util.PersianCalendar
import java.util.Locale
import java.util.TimeZone
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
private val WeekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

private fun faNumber(value: Int): String =
    value.toString().map { if (it in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[it - '0'] else it }.joinToString("")

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
        PersianCalendar(TimeZone.getTimeZone("Asia/Tehran"), Locale("fa", "IR")).apply {
            timeInMillis = System.currentTimeMillis()
        }
    }
    val year = today.get(PersianCalendar.YEAR)
    val month = today.get(PersianCalendar.MONTH)
    val day = today.get(PersianCalendar.DAY_OF_MONTH)
    val firstDay = remember(year, month) {
        (today.clone() as PersianCalendar).apply { set(PersianCalendar.DAY_OF_MONTH, 1) }
            .get(PersianCalendar.DAY_OF_WEEK)
    }
    val monthLength = today.getActualMaximum(PersianCalendar.DAY_OF_MONTH)
    val offset = firstDay % 7
    val cells = (0 until offset).map { 0 } + (1..monthLength).toList()

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("گاه‌شمار هوشمند", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("تقویم فارسی و زمان ایران", color = Muted, fontSize = 12.sp)
            }
            Box(Modifier.size(44.dp).background(Purple.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CalendarMonth, null, tint = Purple)
            }
        }
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("امروز", color = Muted, fontSize = 12.sp)
                Text(faNumber(day) + " " + MonthNames[month] + " " + faNumber(year), color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("بر اساس ساعت رسمی ایران", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(MonthNames[month] + " " + faNumber(year), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("امروز", color = Purple, fontSize = 13.sp, modifier = Modifier.clickable { })
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            WeekDays.forEach { name ->
                Text(name, Modifier.weight(1f), textAlign = TextAlign.Center, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth(), userScrollEnabled = false) {
            items(cells) { value ->
                Box(
                    Modifier.fillMaxWidth().padding(3.dp).aspectRatio(1f).then(
                        if (value == day) Modifier.background(Purple, CircleShape) else Modifier
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (value != 0) Text(faNumber(value), color = if (value == day) Color.White else Ink, fontSize = 14.sp, fontWeight = if (value == day) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("رویدادها و یادآورها", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Text("هنوز رویدادی ثبت نشده است.", Modifier.padding(18.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp)
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
