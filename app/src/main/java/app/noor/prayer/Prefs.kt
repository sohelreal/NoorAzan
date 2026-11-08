package app.noor.prayer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import java.util.TimeZone

const val DEFAULT_AZAN_URL = "https://cdn.aladhan.com/audio/adhans/a1.mp3"

/** All user settings. Observable by Compose and persisted in SharedPreferences. */
object Prefs {
    private var sp: SharedPreferences? = null

    var lang by mutableStateOf("en")
    var theme by mutableIntStateOf(0) // 0 system, 1 light, 2 dark
    var method by mutableStateOf("MWL")
    var hanafi by mutableStateOf(false)
    var lat by mutableDoubleStateOf(21.4225)
    var lon by mutableDoubleStateOf(39.8262)
    var place by mutableStateOf("")
    var located by mutableStateOf(false)
    var hijriOffset by mutableIntStateOf(0)
    var azanMode by mutableIntStateOf(0) // 0 azan audio, 1 notification sound, 2 silent
    var use24 by mutableStateOf(false)
    var showTrans by mutableStateOf(true)
    var arabicSize by mutableFloatStateOf(28f)
    var reciter by mutableStateOf("ar.alafasy")
    var lastSurah by mutableIntStateOf(1)
    var azanUrl by mutableStateOf(DEFAULT_AZAN_URL)
    val notify = mutableStateListOf(true, true, true, true, true)
    val adj = mutableStateListOf(0, 0, 0, 0, 0)

    fun init(ctx: Context) {
        if (sp != null) return
        val s = ctx.getSharedPreferences("noor", Context.MODE_PRIVATE)
        sp = s
        val sysLang = Locale.getDefault().language
        lang = s.getString("lang", null) ?: when (sysLang) { "bn" -> "bn"; "ar" -> "ar"; else -> "en" }
        theme = s.getInt("theme", 0)
        val tz = TimeZone.getDefault().id
        val defMethod = when {
            tz.startsWith("America/") || tz.startsWith("US/") || tz.startsWith("Canada/") -> "ISNA"
            tz in listOf("Asia/Dhaka", "Asia/Karachi", "Asia/Kolkata", "Asia/Calcutta") -> "KARACHI"
            tz in listOf("Asia/Riyadh", "Asia/Qatar", "Asia/Kuwait", "Asia/Bahrain", "Asia/Dubai") -> "MAKKAH"
            tz == "Africa/Cairo" -> "EGYPT"
            else -> "MWL"
        }
        method = s.getString("method", null) ?: defMethod
        hanafi = if (s.contains("hanafi")) s.getBoolean("hanafi", false) else tz in listOf("Asia/Dhaka", "Asia/Karachi", "Asia/Kolkata", "Asia/Calcutta")
        lat = s.getFloat("latf", 21.4225f).toDouble().let { if (s.contains("lat")) java.lang.Double.longBitsToDouble(s.getLong("lat", 0)) else it }
        lon = s.getFloat("lonf", 39.8262f).toDouble().let { if (s.contains("lon")) java.lang.Double.longBitsToDouble(s.getLong("lon", 0)) else it }
        place = s.getString("place", "") ?: ""
        located = s.getBoolean("located", false)
        hijriOffset = s.getInt("hijriOffset", 0)
        azanMode = s.getInt("azanMode", 0)
        use24 = s.getBoolean("use24", false)
        showTrans = s.getBoolean("showTrans", true)
        arabicSize = s.getFloat("arabicSize", 28f)
        reciter = s.getString("reciter", "ar.alafasy") ?: "ar.alafasy"
        lastSurah = s.getInt("lastSurah", 1)
        azanUrl = s.getString("azanUrl", DEFAULT_AZAN_URL) ?: DEFAULT_AZAN_URL
        s.getString("notify", null)?.split(",")?.forEachIndexed { i, v -> if (i < 5) notify[i] = v == "1" }
        s.getString("adj", null)?.split(",")?.forEachIndexed { i, v -> if (i < 5) adj[i] = v.toIntOrNull() ?: 0 }
    }

    fun update(block: Prefs.() -> Unit) {
        this.block()
        save()
    }

    fun save() {
        val s = sp ?: return
        s.edit()
            .putString("lang", lang)
            .putInt("theme", theme)
            .putString("method", method)
            .putBoolean("hanafi", hanafi)
            .putLong("lat", java.lang.Double.doubleToLongBits(lat))
            .putLong("lon", java.lang.Double.doubleToLongBits(lon))
            .putString("place", place)
            .putBoolean("located", located)
            .putInt("hijriOffset", hijriOffset)
            .putInt("azanMode", azanMode)
            .putBoolean("use24", use24)
            .putBoolean("showTrans", showTrans)
            .putFloat("arabicSize", arabicSize)
            .putString("reciter", reciter)
            .putInt("lastSurah", lastSurah)
            .putString("azanUrl", azanUrl)
            .putString("notify", notify.joinToString(",") { if (it) "1" else "0" })
            .putString("adj", adj.joinToString(","))
            .apply()
    }
}
