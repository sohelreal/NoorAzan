package app.noor.prayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("azan_alert", "Azan alerts (sound)", NotificationManager.IMPORTANCE_HIGH))
        nm.createNotificationChannel(
            NotificationChannel("azan_quiet", "Azan alerts (silent)", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(false)
            }
        )
        nm.createNotificationChannel(NotificationChannel("player", "Audio player", NotificationManager.IMPORTANCE_LOW))
        Alarms.scheduleNext(this)
        Thread { prefetchAzan() }.start()
    }

    /** Saves the Azan audio on the device once, so it plays offline at prayer time. Retried at each launch until it succeeds. */
    private fun prefetchAzan() {
        val dest = PlayerService.azanFile(this)
        if (dest.exists() && dest.length() > 1000) return
        if (PlayerService.bundledAzan(this, 0) != 0) return // Azan is built into the app
        try {
            val c = java.net.URL(Prefs.azanUrl).openConnection() as java.net.HttpURLConnection
            c.connectTimeout = 15000
            c.readTimeout = 60000
            if (c.responseCode != 200) return
            val tmp = java.io.File(filesDir, "azan.tmp")
            c.inputStream.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
            if (tmp.length() > 1000) {
                dest.delete()
                tmp.renameTo(dest)
            } else {
                tmp.delete()
            }
        } catch (e: Exception) {
            // offline: will retry next launch; playback falls back to streaming / system alarm tone
        }
    }
}
