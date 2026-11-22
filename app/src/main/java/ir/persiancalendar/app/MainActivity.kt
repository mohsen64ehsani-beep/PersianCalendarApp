package ir.persiancalendar.app

import android.os.Bundle
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.icu.util.GregorianCalendar
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
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var themeIndex by remember { mutableIntStateOf(context.getSharedPreferences("daynegar", 0).getInt("theme", 0).coerceIn(0, 4)) }
    val accent = listOf(Purple, Color(0xFF087E9B), Color(0xFF16845B), Color(0xFFB65C83), Color(0xFFB58A35))[themeIndex]
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
                    0 -> CalendarHome(accent)
                    1 -> DateConverter(accent)
                    2 -> IranClockPage()
                    else -> SettingsPage(themeIndex, { themeIndex = it; context.getSharedPreferences("daynegar", 0).edit().putInt("theme", it).apply() }, accent)
                }
            }
        }
    }
}

@Composable
private fun CalendarHome(accent: Color) {
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
    val eventKey = "$visibleYear-$visibleMonth-$selectedDay"
    val prefs = remember { context.getSharedPreferences("daynegar", 0) }
    var events by remember(eventKey) {
        mutableStateOf(prefs.getStringSet("events_$eventKey", emptySet())?.toList()?.sorted() ?: emptyList())
    }
    var showAddEvent by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }

    val selectedGregorian = remember(visibleYear, visibleMonth, selectedDay) {
        runCatching {
            val selected = persianCalendar().apply {
                isLenient = false
                set(Calendar.YEAR, visibleYear)
                set(Calendar.MONTH, visibleMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay.coerceIn(1, monthLength))
            }
            SimpleDateFormat("EEEE، d MMMM yyyy", Locale("fa", "IR")).format(selected.time)
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
                Text("تقویم روزنگار", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("تقویم فارسی و زمان ایران", color = Muted, fontSize = 13.sp)
            }
            Box(Modifier.size(46.dp).background(Purple.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CalendarMonth, null, tint = accent)
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 17.dp, vertical = 14.dp)) {
                Text(if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) "امروز" else "تاریخ انتخاب‌شده", color = Muted, fontSize = 13.sp)
                Text(faNumber(selectedDay) + " " + MonthNames[visibleMonth] + " " + faNumber(visibleYear), color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text(selectedGregorian, color = Muted, fontSize = 12.sp)
                if (visibleYear == todayYear && visibleMonth == todayMonth && selectedDay == todayDay) {
                    Text("امروز · ساعت رسمی ایران", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
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
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEachIndexed { index, label ->
                Text(label, Modifier.weight(1f), textAlign = TextAlign.Center,
                    color = if (index == 6) Color(0xFFD45D68) else Muted,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth(), userScrollEnabled = false) {
            items(cells) { value ->
                val isSelected = value != 0 && value == selectedDay
                val isToday = value != 0 && visibleYear == todayYear && visibleMonth == todayMonth && value == todayDay
                Box(
                    Modifier.fillMaxWidth().padding(2.dp).aspectRatio(0.94f).then(
                        if (isSelected) Modifier.background(accent, CircleShape) else Modifier
                    ).clickable(enabled = value != 0) { selectedDay = value },
                    contentAlignment = Alignment.Center
                ) {
                    if (value != 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(faNumber(value),
                                color = when { isSelected -> Color.White; isToday -> accent; else -> Ink },
                                fontSize = 17.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium)
                            if (isToday && !isSelected) {
                                Box(Modifier.padding(top = 1.dp).size(4.dp).background(accent, CircleShape))
                            }
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("رویدادها و یادآورها", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { eventTitle = ""; showAddEvent = true }) { Text("افزودن رویداد", color = accent) }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
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
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it.take(80) },
                        label = { Text("عنوان رویداد") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val title = eventTitle.trim()
                        if (title.isNotEmpty()) {
                            events = (events + title).distinct()
                            prefs.edit().putStringSet("events_$eventKey", events.toSet()).apply()
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
    var jy by remember { mutableStateOf("۱۴۰۵") }; var jm by remember { mutableStateOf("۱") }; var jd by remember { mutableStateOf("۱") }
    var gy by remember { mutableStateOf("۲۰۲۶") }; var gm by remember { mutableStateOf("۳") }; var gd by remember { mutableStateOf("۲۱") }
    var result by remember { mutableStateOf("تاریخ را وارد کنید.") }
    fun num(s: String): Int? = s.map { if (it in '۰'..'۹') ('0'.code + it.code - '۰'.code).toChar() else it }.joinToString("").toIntOrNull()
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("تبدیل تاریخ", color=Ink, fontSize=24.sp, fontWeight=FontWeight.Bold)
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("شمسی به میلادی", color=accent, fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    DateField("سال",jy,{jy=it},Modifier.weight(1.2f)); DateField("ماه",jm,{jm=it},Modifier.weight(1f)); DateField("روز",jd,{jd=it},Modifier.weight(1f))
                }
                Button(onClick={
                    val y=num(jy); val m=num(jm); val d=num(jd)
                    result=if(y!=null && m!=null && m in 1..12 && d!=null && d in 1..31) runCatching {
                        val c=persianCalendar(); c.isLenient=false; c.clear(); c.set(y,m-1,d)
                        c.timeInMillis // Force strict calendar validation before formatting.
                        SimpleDateFormat("yyyy/MM/dd",Locale.US).format(c.time)
                    }.getOrElse{"تاریخ معتبر نیست."} else "سال، ماه و روز را بررسی کنید."
                }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("تبدیل به میلادی")}
            }
        }
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("میلادی به شمسی", color=accent, fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    DateField("سال",gy,{gy=it},Modifier.weight(1.2f)); DateField("ماه",gm,{gm=it},Modifier.weight(1f)); DateField("روز",gd,{gd=it},Modifier.weight(1f))
                }
                Button(onClick={
                    val y=num(gy); val m=num(gm); val d=num(gd)
                    result=if(y!=null && m!=null && m in 1..12 && d!=null && d in 1..31) runCatching {
                        val c=GregorianCalendar(); c.isLenient=false; c.clear(); c.set(y,m-1,d); c.timeInMillis
                        val p=persianCalendar(); p.timeInMillis=c.timeInMillis
                        faNumber(p.get(Calendar.YEAR))+"/"+faNumber(p.get(Calendar.MONTH)+1)+"/"+faNumber(p.get(Calendar.DAY_OF_MONTH))
                    }.getOrElse{"تاریخ معتبر نیست."} else "سال، ماه و روز را بررسی کنید."
                }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("تبدیل به شمسی")}
            }
        }
        Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)), shape=RoundedCornerShape(18.dp)){Text(result,Modifier.fillMaxWidth().padding(16.dp),color=Ink,textAlign=TextAlign.Center,fontSize=17.sp)}
    }
}
@Composable
private fun DateField(label:String,value:String,onValue:(String)->Unit,modifier:Modifier=Modifier){
    OutlinedTextField(value=value,onValueChange={onValue(it.filter{ch->ch.isDigit() || ch in '۰'..'۹'}.take(4))},label={Text(label)},modifier=modifier,singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
}
@Composable
private fun IranClockPage(){
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}
    LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();kotlinx.coroutines.delay(1000)}}
    val time=remember(now){SimpleDateFormat("HH:mm:ss",Locale("fa","IR")).apply{timeZone=java.util.TimeZone.getTimeZone("Asia/Tehran")}.format(java.util.Date(now))}
    val date=remember(now){val c=persianCalendar().apply{timeInMillis=now};faNumber(c.get(Calendar.DAY_OF_MONTH))+" "+MonthNames[c.get(Calendar.MONTH)]+" "+faNumber(c.get(Calendar.YEAR))}
    Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
        Text("ساعت رسمی ایران",color=Muted,fontSize=16.sp); Spacer(Modifier.height(12.dp))
        Text(time,color=Purple,fontSize=46.sp,fontWeight=FontWeight.Bold); Text(date,color=Ink,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp)); Text("منطقه زمانی Asia/Tehran",color=Muted,fontSize=12.sp)
        Text("محاسبه اوقات شرعی و اذان در حال توسعه است.",color=Muted,fontSize=12.sp)
    }
}
@Composable
private fun SettingsPage(themeIndex:Int,onTheme:(Int)->Unit,accent:Color){
    val palette=listOf(Color(0xFF7255C7),Color(0xFF087E9B),Color(0xFF16845B),Color(0xFFB65C83),Color(0xFFB58A35))
    val names=listOf("بنفش سلطنتی","آبی اقیانوسی","سبز زمردی","صورتی رزگلد","مشکی و طلایی")
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("تنظیمات",color=Ink,fontSize=24.sp,fontWeight=FontWeight.Bold)
        Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(20.dp)){
            Column(Modifier.padding(14.dp)){Text("انتخاب تم رنگی",color=Muted); names.forEachIndexed{index,name->
                Row(Modifier.fillMaxWidth().clickable{onTheme(index)}.padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){
                    Box(Modifier.size(28.dp).background(palette[index],CircleShape)); Text(name,Modifier.weight(1f).padding(start=12.dp),color=Ink)
                    RadioButton(selected=themeIndex==index,onClick={onTheme(index)},colors=RadioButtonDefaults.colors(selectedColor=accent))
                }
            }}
        }
        Text("تقویم روزنگار",color=Ink,fontWeight=FontWeight.SemiBold); Text("نسخه ۰.۴.۰",color=Muted,fontSize=12.sp)
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
