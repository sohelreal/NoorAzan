package app.noor.prayer

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Offline astronomical prayer-time calculation (same well-known algorithm family as PrayTimes.org).
 * Times are computed from latitude/longitude and the device time zone, so no network is needed.
 */
object PrayerCalc {
    class M(val fajr: Double, val isha: Double, val ishaMin: Int = 0)

    val methods = linkedMapOf(
        "MWL" to M(18.0, 17.0),
        "ISNA" to M(15.0, 15.0),
        "EGYPT" to M(19.5, 17.5),
        "MAKKAH" to M(18.5, 0.0, 90),
        "KARACHI" to M(18.0, 18.0),
    )

    /** Index in [Day.t] of each of the five prayers: Fajr, Dhuhr, Asr, Maghrib, Isha. */
    val PIDX = intArrayOf(0, 2, 3, 4, 5)

    /** t = [fajr, sunrise, dhuhr, asr, maghrib, isha] as epoch millis. */
    class Day(val date: LocalDate, val t: LongArray) {
        fun prayer(p: Int): Long = t[PIDX[p]]
        val sunrise: Long get() = t[1]
    }

    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun sinD(d: Double) = sin(rad(d))
    private fun cosD(d: Double) = cos(rad(d))
    private fun tanD(d: Double) = tan(rad(d))
    private fun fix(a: Double, b: Double): Double = a - b * floor(a / b)

    private fun julian(y: Int, m: Int, d: Int): Double {
        var yy = y
        var mm = m
        if (mm <= 2) { yy -= 1; mm += 12 }
        val a = floor(yy / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (yy + 4716)) + floor(30.6001 * (mm + 1)) + d + b - 1524.5
    }

    /** returns [declination (deg), equation of time (hours)] */
    private fun sunPos(jd: Double): DoubleArray {
        val d = jd - 2451545.0
        val g = fix(357.529 + 0.98560028 * d, 360.0)
        val q = fix(280.459 + 0.98564736 * d, 360.0)
        val l = fix(q + 1.915 * sinD(g) + 0.020 * sinD(2 * g), 360.0)
        val e = 23.439 - 0.00000036 * d
        val ra = fix(deg(atan2(cosD(e) * sinD(l), cosD(l))) / 15.0, 24.0)
        val eqt = q / 15.0 - ra
        val decl = deg(asin(sinD(e) * sinD(l)))
        return doubleArrayOf(decl, eqt)
    }

    /** Local-time hours: [fajr, sunrise, dhuhr, asr, maghrib, isha], or null in polar edge cases. */
    fun compute(date: LocalDate, lat: Double, lon: Double, tz: Double, m: M, hanafi: Boolean): DoubleArray? {
        val jd = julian(date.year, date.monthValue, date.dayOfMonth) - lon / (15.0 * 24.0)
        fun mid(t: Double) = fix(12.0 - sunPos(jd + t)[1], 24.0)
        fun angleTime(angle: Double, t: Double, ccw: Boolean): Double {
            val decl = sunPos(jd + t)[0]
            val x = (-sinD(angle) - sinD(decl) * sinD(lat)) / (cosD(decl) * cosD(lat))
            if (x < -1.0 || x > 1.0) return Double.NaN
            val v = deg(acos(x)) / 15.0
            return mid(t) + if (ccw) -v else v
        }
        fun asrTime(factor: Double, t: Double): Double {
            val decl = sunPos(jd + t)[0]
            val ang = -deg(atan(1.0 / (factor + tanD(abs(lat - decl)))))
            return angleTime(ang, t, false)
        }

        val horizon = 0.833
        var fajr = angleTime(m.fajr, 5.0 / 24, true)
        var sunrise = angleTime(horizon, 6.0 / 24, true)
        var dhuhr = mid(12.0 / 24)
        var asr = asrTime(if (hanafi) 2.0 else 1.0, 13.0 / 24)
        var sunset = angleTime(horizon, 18.0 / 24, false)
        var isha = if (m.ishaMin > 0) Double.NaN else angleTime(m.isha, 18.0 / 24, false)

        if (sunrise.isNaN() || sunset.isNaN() || dhuhr.isNaN() || asr.isNaN()) return null

        val adjust = tz - lon / 15.0
        sunrise += adjust; dhuhr += adjust; asr += adjust; sunset += adjust
        if (!fajr.isNaN()) fajr += adjust
        if (!isha.isNaN()) isha += adjust

        val night = fix(sunrise - sunset, 24.0)
        if (fajr.isNaN()) fajr = sunrise - (m.fajr / 60.0) * night
        if (m.ishaMin > 0) isha = sunset + m.ishaMin / 60.0
        else if (isha.isNaN()) isha = sunset + (m.isha / 60.0) * night

        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, isha)
    }

    fun day(date: LocalDate): Day? {
        val zone = ZoneId.systemDefault()
        val tz = zone.rules.getOffset(date.atTime(12, 0).atZone(zone).toInstant()).totalSeconds / 3600.0
        val m = methods[Prefs.method] ?: methods.getValue("MWL")
        val h = compute(date, Prefs.lat, Prefs.lon, tz, m, Prefs.hanafi) ?: return null
        val out = LongArray(6)
        for (i in 0..5) {
            var minutes = Math.round(h[i] * 60.0)
            val p = PIDX.indexOf(i)
            if (p >= 0) minutes += Prefs.adj[p]
            out[i] = LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes).atZone(zone).toInstant().toEpochMilli()
        }
        return Day(date, out)
    }

    /** Next prayer after [now]: (prayer index 0..4, epoch millis). */
    fun next(now: Long): Pair<Int, Long>? {
        val today = LocalDate.now()
        for (d in 0..2) {
            val day = day(today.plusDays(d.toLong())) ?: continue
            for (p in 0..4) {
                val ms = day.prayer(p)
                if (ms > now) return p to ms
            }
        }
        return null
    }

    /** Great-circle bearing from (lat, lon) to the Kaaba, degrees from true north. */
    fun qiblaBearing(lat: Double, lon: Double): Double {
        val kLat = rad(21.4225)
        val kLon = 39.8262
        val p1 = rad(lat)
        val dl = rad(kLon - lon)
        val y = sin(dl) * cos(kLat)
        val x = cos(p1) * sin(kLat) - sin(p1) * cos(kLat) * cos(dl)
        return (deg(atan2(y, x)) + 360.0) % 360.0
    }
}
