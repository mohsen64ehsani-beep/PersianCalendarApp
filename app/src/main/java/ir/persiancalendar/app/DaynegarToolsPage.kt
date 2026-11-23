package ir.persiancalendar.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.IslamicCalendar
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.icu.util.TimeZone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone as JavaTimeZone

private data class ClockCity(val title: String, val zone: String)
private val clockCities = listOf(
    ClockCity("تهران", "Asia/Tehran"), ClockCity("برلین", "Europe/Berlin"),
    ClockCity("لندن", "Europe/London"), ClockCity("نیویورک", "America/New_York"),
    ClockCity("توکیو", "Asia/Tokyo"), ClockCity("دبی", "Asia/Dubai"),
    ClockCity("استانبول", "Europe/Istanbul"), ClockCity("سیدنی", "Australia/Sydney")
)

@Composable
fun DaynegarToolsPage(accent: Color) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("daynegar_tools", Context.MODE_PRIVATE) }
    var query by remember { mutableStateOf("") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val todayKey = remember(now) { Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"), ULocale("fa_IR@calendar=persian")).apply { timeInMillis = now }.let { "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH) + 1}-${it.get(Calendar.DAY_OF_MONTH)}" } }
    var note by remember(todayKey) { mutableStateOf(prefs.getString("note_$todayKey", "") ?: "") }
    var feedback by remember { mutableStateOf("") }
    var compassAzimuth by remember { mutableFloatStateOf(0f) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    DisposableEffect(sensorManager) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                compassAzimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(30_000) } }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            val root = JSONObject()
            listOf("daynegar", "daynegar_prayer", "daynegar_reminders", "daynegar_tools").forEach { name ->
                val source = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                val values = JSONObject()
                source.all.forEach { (key, value) ->
                    when (value) {
                        is String -> values.put(key, value)
                        is Int -> values.put(key, value)
                        is Long -> values.put(key, value)
                        is Float -> values.put(key, value.toDouble())
                        is Boolean -> values.put(key, value)
                        is Set<*> -> values.put(key, org.json.JSONArray(value.toList()))
                    }
                }
                root.put(name, values)
            }
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
            feedback = "نسخه پشتیبان با موفقیت ذخیره شد."
        }.onFailure { feedback = "ذخیره پشتیبان ناموفق بود: ${it.message}" }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("فایل قابل خواندن نیست")
            val root = JSONObject(json)
            listOf("daynegar", "daynegar_prayer", "daynegar_reminders", "daynegar_tools").forEach { name ->
                if (root.has(name)) {
                    val values = root.getJSONObject(name)
                    val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
                    values.keys().forEach { key ->
                        val value = values.get(key)
                        when (value) {
                            is String -> editor.putString(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is Boolean -> editor.putBoolean(key, value)
                            is Double -> editor.putFloat(key, value.toFloat())
                            is org.json.JSONArray -> editor.putStringSet(key, (0 until value.length()).map { value.getString(it) }.toSet())
                        }
                    }
                    editor.apply()
                }
            }
            note = context.getSharedPreferences("daynegar_tools", Context.MODE_PRIVATE).getString("note_$todayKey", "") ?: ""
            feedback = "بازیابی اطلاعات انجام شد؛ برای اعمال همه تنظیمات، برنامه را یک‌بار بازنشانی کن."
        }.onFailure { feedback = "فایل پشتیبان معتبر نیست: ${it.message}" }
    }

    val todayHijri = remember(now) {
        runCatching {
            val hijri = IslamicCalendar(TimeZone.getTimeZone("Asia/Tehran")).apply { timeInMillis = now }
            "${hijri.get(IslamicCalendar.DAY_OF_MONTH)} / ${hijri.get(IslamicCalendar.MONTH) + 1} / ${hijri.get(IslamicCalendar.YEAR)}"
        }.getOrDefault("—")
    }
    val events = remember(query) {
        (1405..1406).flatMap { year ->
            (1..12).flatMap { month -> (1..31).flatMap { day ->
                IranCalendarEvents.forDate(year, month, day).map { Triple(year, month, day) to it }
            } }
        }.filter { query.isBlank() || it.second.title.contains(query.trim(), ignoreCase = true) }
            .take(60)
    }
    val worldTimes = remember(now) {
        clockCities.map { city ->
            city.title to SimpleDateFormat("HH:mm", Locale("fa", "IR")).apply {
                timeZone = JavaTimeZone.getTimeZone(city.zone)
            }.format(Date(now))
        }
    }
    val qiblaBearing = remember {
        val lat1 = Math.toRadians(35.6892); val lon1 = Math.toRadians(51.3890)
        val lat2 = Math.toRadians(21.4225); val lon2 = Math.toRadians(39.8262)
        val y = kotlin.math.sin(lon2 - lon1)
        val x = kotlin.math.cos(lat1) * kotlin.math.tan(lat2) - kotlin.math.sin(lat1) * kotlin.math.cos(lon2 - lon1)
        ((Math.toDegrees(kotlin.math.atan2(y, x)) + 360) % 360).toInt()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF203747)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF203747), accent.copy(alpha = .86f), Color(0xFF426B70))),
                    RoundedCornerShape(24.dp)
                ).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(48.dp).background(Color.White.copy(alpha = .14f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("ابزارهای روزنگار", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("یادداشت، جست‌وجوی مناسبت، ساعت شهرها و ابزارهای روزانه", color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                }
            }
        }
        ToolCard(accent, "یادداشت امروز", "یادداشت کوتاه روزانه؛ به‌صورت خودکار روی گوشی ذخیره می‌شود.", Icons.Default.EditNote) {
            OutlinedTextField(value = note, onValueChange = { note = it.take(2000); prefs.edit().putString("note_$todayKey", it.take(2000)).apply() },
                modifier = Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("امروز چه چیزی را می‌خواهی به خاطر بسپاری؟") })
            Text("${note.length} / ۲۰۰۰ نویسه", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
        ToolCard(accent, "تقویم قمری", "تاریخ قمری امروز بر اساس محاسبه تقویم اسلامی دستگاه", Icons.Default.Brightness3) {
            Text(todayHijri, color = accent, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("ممکن است با اعلام رؤیت هلال در ایران یک روز اختلاف داشته باشد.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        ToolCard(accent, "جهت قبله", "زاویه محاسباتی از شمال حقیقی برای شهر تهران", Icons.Default.Explore) {
            Text("${qiblaBearing}°", color = accent, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("برای جهت دقیق در محل خود، موقعیت جغرافیایی و قطب‌نمای گوشی باید در نسخه بعدی فعال شود.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        ToolCard(accent, "ساعت شهرهای جهان", "زمان هم‌زمان در چند منطقه زمانی", Icons.Default.Public) {
            worldTimes.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (city, time) ->
                        Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), RoundedCornerShape(13.dp)).padding(12.dp)) {
                            Text(city, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            Text(time, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        ToolCard(accent, "جست‌وجوی مناسبت‌ها", "جست‌وجو در مناسبت‌های ثبت‌شده سال‌های ۱۴۰۵ و ۱۴۰۶", Icons.Default.Search) {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("مثلاً نوروز، حافظ، فطر…") })
            if (query.isNotBlank()) {
                if (events.isEmpty()) Text("مناسبتی پیدا نشد.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                events.forEach { (date, occasion) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${date.third}/${date.second}/${date.first}", color = accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(occasion.title, Modifier.weight(1f).padding(start = 10.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        if (occasion.isHoliday) Text("تعطیل", color = Color(0xFFD34D5C), fontSize = 10.sp)
                    }
                }
            } else Text("برای دیدن نتایج، نام مناسبت را وارد کن.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        ToolCard(accent, "پشتیبان‌گیری و بازیابی", "ذخیره تنظیمات، یادداشت‌ها، رویدادها و یادآورها در یک فایل JSON", Icons.Default.Backup) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { backupLauncher.launch("daynegar-backup.json") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("تهیه پشتیبان") }
                OutlinedButton(onClick = { restoreLauncher.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.weight(1f)) { Text("بازیابی") }
            }
            if (feedback.isNotBlank()) Text(feedback, color = accent, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ToolCard(accent: Color, title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(accent.copy(alpha = .11f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent) }
                Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
            content()
        }
    }
}
