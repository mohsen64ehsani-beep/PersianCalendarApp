package ir.persiancalendar.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.icu.util.Calendar
import android.icu.util.ULocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DaynegarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val now = System.currentTimeMillis()
            val persian = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"), ULocale("fa_IR@calendar=persian")).apply { timeInMillis = now }
            val date = "${persian.get(Calendar.DAY_OF_MONTH)} ${listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")[persian.get(Calendar.MONTH)]} ${persian.get(Calendar.YEAR)}"
            val clock = SimpleDateFormat("HH:mm", Locale("fa", "IR")).apply { timeZone = TimeZone.getTimeZone("Asia/Tehran") }.format(Date(now))
            val views = RemoteViews(context.packageName, R.layout.widget_daynegar).apply {
                setTextViewText(R.id.widget_date, date)
                setTextViewText(R.id.widget_clock, clock)
                setTextViewText(R.id.widget_caption, "تقویم شمسی · ساعت ایران")
                val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (launch != null) setOnClickPendingIntent(R.id.widget_root, android.app.PendingIntent.getActivity(context, 701, launch, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            }
            manager.updateAppWidget(id, views)
        }
    }
}
