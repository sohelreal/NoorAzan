package app.noor.prayer

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate

/** Schedules one exact alarm for the next enabled prayer; each fired alarm schedules the following one. */
object Alarms {
    fun scheduleNext(ctx: Context) {
        val app = ctx.applicationContext
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        var pick: Pair<Int, Long>? = null
        val today = LocalDate.now()
        search@ for (d in 0..2) {
            val day = PrayerCalc.day(today.plusDays(d.toLong())) ?: continue
            for (p in 0..4) {
                val ms = day.prayer(p)
                if (ms > now + 500 && Prefs.notify[p]) {
                    pick = p to ms
                    break@search
                }
            }
        }
        val intent = Intent(app, AzanReceiver::class.java)
        if (pick == null) {
            val pi = PendingIntent.getBroadcast(app, 100, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            pi?.let { am.cancel(it) }
            return
        }
        intent.putExtra("idx", pick.first)
        val pi = PendingIntent.getBroadcast(app, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, pick.second, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, pick.second, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, pick.second, pi)
        }
    }
}

object Notifier {
    private fun openApp(ctx: Context): PendingIntent =
        PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

    fun azanText(idx: Int): Pair<String, String> {
        val jumuah = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.FRIDAY
        val name = prayerName(idx, jumuah)
        return name to tr("its_time").replace("%s", name)
    }

    fun postAzan(ctx: Context, idx: Int, channel: String) {
        val (title, text) = azanText(idx)
        val n = Notification.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openApp(ctx))
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .build()
        runCatching { (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(200 + idx, n) }
    }

    fun player(ctx: Context, title: String, text: String, quran: Boolean, playing: Boolean): Notification {
        fun action(act: String, req: Int): PendingIntent =
            PendingIntent.getService(ctx, req, Intent(ctx, PlayerService::class.java).setAction(act), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val b = Notification.Builder(ctx, "player")
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openApp(ctx))
            .setOngoing(true)
        if (quran) {
            b.addAction(Notification.Action.Builder(null, if (playing) tr("pause") else tr("resume"), action(PlayerService.ACTION_TOGGLE, 1)).build())
        }
        b.addAction(Notification.Action.Builder(null, tr("stop"), action(PlayerService.ACTION_STOP, 2)).build())
        return b.build()
    }
}

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val idx = intent.getIntExtra("idx", 0)
        try {
            if (Prefs.azanMode == 0) {
                try {
                    val i = Intent(context, PlayerService::class.java).setAction(PlayerService.ACTION_AZAN).putExtra("idx", idx)
                    context.startForegroundService(i)
                } catch (e: Exception) {
                    Notifier.postAzan(context, idx, "azan_alert")
                }
            } else {
                Notifier.postAzan(context, idx, if (Prefs.azanMode == 1) "azan_alert" else "azan_quiet")
            }
        } finally {
            Alarms.scheduleNext(context)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Alarms.scheduleNext(context)
    }
}
