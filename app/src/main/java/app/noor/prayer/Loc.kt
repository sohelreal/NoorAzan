package app.noor.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.util.Locale

/** GPS / network location using the platform LocationManager (no Google Play Services needed). */
object Loc {
    fun hasPermission(ctx: Context): Boolean =
        ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private val main = Handler(Looper.getMainLooper())

    private fun apply(ctx: Context, l: Location) {
        Prefs.update {
            lat = l.latitude
            lon = l.longitude
            located = true
        }
        Alarms.scheduleNext(ctx)
        val app = ctx.applicationContext
        Thread {
            val name = try {
                @Suppress("DEPRECATION")
                val list = Geocoder(app, Locale.getDefault()).getFromLocation(l.latitude, l.longitude, 1)
                val a = list?.firstOrNull()
                listOfNotNull(a?.locality ?: a?.subAdminArea, a?.countryName).joinToString(", ")
            } catch (e: Exception) {
                ""
            }
            main.post { Prefs.update { place = name } }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun refresh(ctx: Context) {
        if (!hasPermission(ctx)) return
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var best: Location? = null
        for (p in lm.getProviders(true)) {
            val l = runCatching { lm.getLastKnownLocation(p) }.getOrNull() ?: continue
            if (best == null || l.time > best.time) best = l
        }
        best?.let { apply(ctx, it) }

        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) { apply(ctx, location) }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        runCatching {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }
    }
}
