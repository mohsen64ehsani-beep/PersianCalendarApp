package ir.persiancalendar.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object EventReminderScheduler {
    private const val CHANNEL_ID = "daynegar_reminders"
    private const val PREFS = "daynegar_reminders"

    fun schedule(context: Context, id: Int, title: String, triggerAtMillis: Long, playAdhan: Boolean = false, repeatDaily: Boolean = false) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("reminder_id", id)
            putExtra("reminder_title", title)
            putExtra("play_adhan", playAdhan)
            putExtra("repeat_daily", repeatDaily)
        }
        val pending = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        val soundFlag = if (playAdhan) 1 else 0
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("reminder_$id", "$triggerAtMillis|$soundFlag|${if (repeatDaily) 1 else 0}|$title").apply()
    }

    fun cancel(context: Context, id: Int) {
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
            pending.cancel()
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("reminder_$id").apply()
    }

    fun restoreAfterBoot(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("reminder_") && value is String) {
                val split = value.split("|", limit = 4)
                val id = key.removePrefix("reminder_").toIntOrNull() ?: return@forEach
                val time = split.firstOrNull()?.toLongOrNull() ?: return@forEach
                val modern = split.size >= 4
                val playAdhan = split.getOrNull(1) == "1"
                val repeatDaily = modern && split.getOrNull(2) == "1"
                val title = if (modern) split.getOrNull(3) else split.getOrNull(2) ?: split.getOrNull(1)
                if (time > System.currentTimeMillis()) schedule(context, id, title ?: "یادآور", time, playAdhan, repeatDaily)
                else prefs.edit().remove(key).apply()
            }
        }
    }

    internal fun show(context: Context, id: Int, title: String, playAdhan: Boolean, repeatDaily: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (playAdhan) "daynegar_adhan" else CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (playAdhan) "اذان و اوقات شرعی" else "یادآورهای روزنگار",
                NotificationManager.IMPORTANCE_HIGH
            )
            if (playAdhan) {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.adhan}")
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                channel.setSound(soundUri, attributes)
            }
            manager.createNotificationChannel(channel)
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(if (playAdhan) "وقت نماز" else "یادآور روزنگار")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .apply {
                if (playAdhan && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.adhan}"))
                }
            }
            .build()
        if (Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
        if (repeatDaily) schedule(context, id, title, System.currentTimeMillis() + 24L * 60L * 60L * 1000L, playAdhan, true)
        else context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("reminder_$id").apply()
    }
}

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        EventReminderScheduler.show(
            context,
            intent.getIntExtra("reminder_id", 0),
            intent.getStringExtra("reminder_title") ?: "یادآور",
            intent.getBooleanExtra("play_adhan", false),
            intent.getBooleanExtra("repeat_daily", false)
        )
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            EventReminderScheduler.restoreAfterBoot(context)
        }
    }
}
