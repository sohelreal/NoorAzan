package app.noor.prayer

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/** Observable playback state shared with the UI. */
object PlayerState {
    var kind by mutableStateOf("") // "", "quran", "azan"
    var surah by mutableIntStateOf(0)
    var title by mutableStateOf("")
    var loading by mutableStateOf(false)
    var playing by mutableStateOf(false)
    var error by mutableStateOf(false)
    var pos by mutableIntStateOf(0)
    var dur by mutableIntStateOf(0)

    fun reset() {
        kind = ""; surah = 0; title = ""; loading = false; playing = false; pos = 0; dur = 0
    }
}

/** Foreground service that plays full-Surah Quran audio and the Azan, so playback survives screen-off. */
class PlayerService : Service() {
    private var mp: MediaPlayer? = null
    private var generation = 0
    private var azanFallbackTried = false
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            val p = mp
            if (p != null && PlayerState.kind == "quran" && PlayerState.playing) {
                runCatching { PlayerState.pos = p.currentPosition; PlayerState.dur = p.duration }
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        handler.post(ticker)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_QURAN -> startQuran(intent.getIntExtra("surah", 1))
            ACTION_AZAN -> startAzan(intent.getIntExtra("idx", 0))
            ACTION_TOGGLE -> toggle()
            ACTION_STOP -> stopAll()
            ACTION_SEEK -> runCatching { mp?.seekTo(intent.getIntExtra("ms", 0)) }
            else -> if (mp == null) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun foreground(title: String, text: String, quran: Boolean) {
        val n = Notifier.player(this, title, text, quran, true)
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    private fun refreshNotification() {
        val quran = PlayerState.kind == "quran"
        val n = Notifier.player(this, PlayerState.title, if (quran) tr("quran") else tr("prayer_times"), quran, PlayerState.playing)
        runCatching { (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID, n) }
    }

    private fun startQuran(n: Int) {
        val s = QuranRepo.surahs[(n - 1).coerceIn(0, 113)]
        PlayerState.error = false
        PlayerState.kind = "quran"
        PlayerState.surah = s.n
        PlayerState.title = "${s.n}. ${s.en}  ${s.ar}"
        PlayerState.loading = true
        PlayerState.playing = false
        PlayerState.pos = 0
        PlayerState.dur = 0
        foreground(PlayerState.title, tr("quran"), true)
        prepare(QuranRepo.audioUrl(Prefs.reciter, s.n), null, false)
    }

    private fun startAzan(idx: Int) {
        val jumuah = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.FRIDAY
        PlayerState.error = false
        PlayerState.kind = "azan"
        PlayerState.surah = 0
        PlayerState.title = prayerName(idx.coerceIn(0, 4), jumuah)
        PlayerState.loading = true
        PlayerState.playing = false
        azanFallbackTried = false
        foreground(PlayerState.title, tr("prayer_times"), false)
        val f = azanFile(this)
        if (f.exists() && f.length() > 0) prepare(null, f, true)
        else prepare(Prefs.azanUrl, null, true)
    }

    private fun prepare(url: String?, file: File?, alarm: Boolean) {
        release()
        val gen = ++generation
        val p = MediaPlayer()
        mp = p
        try {
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(if (alarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA)
                    .setContentType(if (alarm) AudioAttributes.CONTENT_TYPE_SONIFICATION else AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            p.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            when {
                file != null -> p.setDataSource(file.absolutePath)
                url != null -> p.setDataSource(url)
                else -> p.setDataSource(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            }
            p.setOnPreparedListener {
                if (gen != generation) return@setOnPreparedListener
                it.start()
                PlayerState.loading = false
                PlayerState.playing = true
                PlayerState.dur = runCatching { it.duration }.getOrDefault(0)
                refreshNotification()
            }
            p.setOnCompletionListener { if (gen == generation) onDone() }
            p.setOnErrorListener { _, _, _ ->
                if (gen == generation) onError()
                true
            }
            p.prepareAsync()
        } catch (e: Exception) {
            onError()
        }
    }

    private fun onDone() {
        if (PlayerState.kind == "quran" && PlayerState.surah in 1..113) startQuran(PlayerState.surah + 1) else stopAll()
    }

    private fun onError() {
        if (PlayerState.kind == "azan" && !azanFallbackTried) {
            azanFallbackTried = true
            prepare(null, null, true) // system alarm sound as last resort
        } else {
            PlayerState.error = PlayerState.kind == "quran"
            stopAll(keepError = PlayerState.error)
        }
    }

    private fun toggle() {
        val p = mp ?: return
        runCatching {
            if (p.isPlaying) {
                p.pause(); PlayerState.playing = false
            } else {
                p.start(); PlayerState.playing = true
            }
        }
        refreshNotification()
    }

    private fun release() {
        generation++
        mp?.let { runCatching { it.reset(); it.release() } }
        mp = null
    }

    private fun stopAll(keepError: Boolean = false) {
        release()
        val err = PlayerState.error
        PlayerState.reset()
        PlayerState.error = keepError && err
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        release()
        val e = PlayerState.error
        PlayerState.reset()
        PlayerState.error = e
        super.onDestroy()
    }

    companion object {
        const val ACTION_QURAN = "app.noor.prayer.QURAN"
        const val ACTION_AZAN = "app.noor.prayer.AZAN"
        const val ACTION_TOGGLE = "app.noor.prayer.TOGGLE"
        const val ACTION_STOP = "app.noor.prayer.STOP"
        const val ACTION_SEEK = "app.noor.prayer.SEEK"
        private const val NOTIF_ID = 300

        fun azanFile(ctx: Context) = File(ctx.filesDir, "azan.mp3")

        fun playSurah(ctx: Context, n: Int) {
            ctx.startForegroundService(Intent(ctx, PlayerService::class.java).setAction(ACTION_QURAN).putExtra("surah", n))
        }

        fun testAzan(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, PlayerService::class.java).setAction(ACTION_AZAN).putExtra("idx", 0))
        }

        fun toggle(ctx: Context) {
            ctx.startService(Intent(ctx, PlayerService::class.java).setAction(ACTION_TOGGLE))
        }

        fun stop(ctx: Context) {
            ctx.startService(Intent(ctx, PlayerService::class.java).setAction(ACTION_STOP))
        }

        fun seek(ctx: Context, ms: Int) {
            ctx.startService(Intent(ctx, PlayerService::class.java).setAction(ACTION_SEEK).putExtra("ms", ms))
        }
    }
}
