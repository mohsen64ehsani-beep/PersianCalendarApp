package ir.persiancalendar.app

import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

internal data class PlannerTask(
    val id: Long,
    val title: String,
    val dueDate: String,
    val completed: Boolean = false
)

internal fun encodePlannerTasks(tasks: List<PlannerTask>): String = JSONArray().apply {
    tasks.forEach { task ->
        put(JSONObject().apply {
            put("id", task.id)
            put("title", task.title)
            put("dueDate", task.dueDate)
            put("completed", task.completed)
        })
    }
}.toString()

internal fun decodePlannerTasks(raw: String?): List<PlannerTask> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val title = item.optString("title").trim()
            if (title.isEmpty()) return@mapNotNull null
            PlannerTask(
                id = item.optLong("id", index.toLong()),
                title = title.take(100),
                dueDate = item.optString("dueDate").take(10),
                completed = item.optBoolean("completed", false)
            )
        }.distinctBy { it.id }
    }.getOrDefault(emptyList())
}

private fun currentPersianDateKey(): String {
    val calendar = Calendar.getInstance(
        android.icu.util.TimeZone.getTimeZone("Asia/Tehran"),
        ULocale("fa_IR@calendar=persian")
    )
    return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
}

@Composable
fun PersonalPlannerCard(accent: Color) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("daynegar_tools", 0) }
    var tasks by remember {
        mutableStateOf(decodePlannerTasks(prefs.getString("personal_tasks_v1", null)))
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(currentPersianDateKey()) }

    fun save(updated: List<PlannerTask>) {
        tasks = updated
        prefs.edit().putString("personal_tasks_v1", encodePlannerTasks(updated)).apply()
    }

    val pendingCount = tasks.count { !it.completed }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = accent)
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("برنامه‌ریز شخصی", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        if (pendingCount == 0) "همه کارها انجام شده‌اند" else "$pendingCount کار باقی‌مانده",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
                FilledTonalButton(
                    onClick = { title = ""; dueDate = currentPersianDateKey(); showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("کار جدید", fontSize = 11.sp)
                }
            }
            if (tasks.isEmpty()) {
                Text(
                    "کارها و هدف‌های روزانه‌ات را اینجا ثبت کن؛ اطلاعات فقط روی همین دستگاه ذخیره می‌شود.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            } else {
                tasks.sortedWith(compareBy<PlannerTask> { it.completed }.thenBy { it.dueDate }.thenBy { it.id })
                    .forEach { task ->
                        Row(
                            Modifier.fillMaxWidth().background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                                RoundedCornerShape(14.dp)
                            ).padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = { checked ->
                                    save(tasks.map { if (it.id == task.id) it.copy(completed = checked) else it })
                                }
                            )
                            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                                Text(
                                    task.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (task.dueDate.isNotBlank()) Text(
                                    "موعد: ${task.dueDate}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(onClick = { save(tasks.filterNot { it.id == task.id }) }) {
                                Icon(Icons.Default.DeleteOutline, "حذف کار", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("افزودن کار جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(100) },
                        label = { Text("عنوان کار") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it.filter { ch -> ch.isDigit() || ch == '/' }.take(10) },
                        label = { Text("تاریخ شمسی (سال/ماه/روز)") },
                        supportingText = { Text("برای نمونه: ۱۴۰۵/۰۹/۰۳") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleanTitle = title.trim()
                    val dateValid = dueDate.isBlank() || Regex("\\d{3,4}/\\d{1,2}/\\d{1,2}").matches(
                        dueDate.map { ch -> if (ch in '۰'..'۹') ('0'.code + ch.code - '۰'.code).toChar() else ch }.joinToString("")
                    )
                    if (cleanTitle.isNotEmpty() && dateValid) {
                        save(tasks + PlannerTask(System.currentTimeMillis(), cleanTitle, dueDate.trim()))
                        showAddDialog = false
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("انصراف") } }
        )
    }
}
