package app.noor.prayer

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.abs

@Composable
fun QiblaScreen() {
    val ctx = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }
    val bearing = PrayerCalc.qiblaBearing(Prefs.lat, Prefs.lon).toFloat()

    var accuracy by remember { mutableIntStateOf(3) }

    DisposableEffect(Prefs.lat, Prefs.lon) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rv = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (rv == null && (acc == null || mag == null)) {
            hasSensor = false
            onDispose { }
        } else {
            val decl = GeomagneticField(Prefs.lat.toFloat(), Prefs.lon.toFloat(), 0f, System.currentTimeMillis()).declination
            var smooth = 0f
            var first = true
            val rot = FloatArray(9)
            val tmp = FloatArray(9)
            val ori = FloatArray(3)
            val gravity = FloatArray(3)
            val geo = FloatArray(3)
            var haveG = false
            var haveM = false

            fun update() {
                // Flat on a table: use the standard axes. Held upright: heading of the back of the phone.
                val m = if (abs(rot[8]) > 0.6f) rot else {
                    SensorManager.remapCoordinateSystem(rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, tmp)
                    tmp
                }
                SensorManager.getOrientation(m, ori)
                val az = ((Math.toDegrees(ori[0].toDouble()).toFloat() + decl) + 360f) % 360f
                if (first) {
                    smooth = az
                    first = false
                } else {
                    var d = az - smooth
                    if (d > 180f) d -= 360f
                    if (d < -180f) d += 360f
                    smooth = (smooth + 0.2f * d + 360f) % 360f
                }
                azimuth = smooth
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    when (e.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rot, e.values)
                            update()
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            for (i in 0..2) gravity[i] = if (haveG) gravity[i] * 0.8f + e.values[i] * 0.2f else e.values[i]
                            haveG = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            for (i in 0..2) geo[i] = if (haveM) geo[i] * 0.8f + e.values[i] * 0.2f else e.values[i]
                            haveM = true
                        }
                    }
                    if (rv == null && haveG && haveM && e.sensor.type != Sensor.TYPE_ACCELEROMETER) {
                        if (SensorManager.getRotationMatrix(rot, null, gravity, geo)) update()
                    }
                }

                override fun onAccuracyChanged(s: Sensor?, a: Int) {
                    if (s?.type == Sensor.TYPE_ROTATION_VECTOR || s?.type == Sensor.TYPE_MAGNETIC_FIELD) accuracy = a
                }
            }
            if (rv != null) {
                sm.registerListener(listener, rv, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sm.registerListener(listener, acc, SensorManager.SENSOR_DELAY_GAME)
                sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_GAME)
            }
            onDispose { sm.unregisterListener(listener) }
        }
    }

    val diff = ((bearing - azimuth + 540f) % 360f) - 180f
    val aligned = hasSensor && abs(diff) < 4f
    val ring = MaterialTheme.colorScheme.primary
    val ringSoft = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(Modifier.fillMaxSize()) {
        Header(tr("qibla"))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(tr("qibla_dir"), style = MaterialTheme.typography.titleMedium)
            Text(
                nd("%.1f°".format(java.util.Locale.ENGLISH, bearing)) + " " + tr("from_north"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ring
            )
            Spacer(Modifier.height(16.dp))
            Text("▼", color = if (aligned) ring else MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.titleLarge)
            Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                // The whole dial turns opposite to the phone heading so N always points to true north.
                Box(Modifier.fillMaxSize().rotate(-azimuth)) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(ringSoft, CircleShape)
                            .border(if (aligned) 6.dp else 3.dp, ring, CircleShape)
                    )
                    Text("N", Modifier.align(Alignment.TopCenter).padding(top = 6.dp), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    Text("S", Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), fontWeight = FontWeight.Bold, color = textColor)
                    Text("E", Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), fontWeight = FontWeight.Bold, color = textColor)
                    Text("W", Modifier.align(Alignment.CenterStart).padding(start = 10.dp), fontWeight = FontWeight.Bold, color = textColor)
                    // Qibla marker, placed at the Qibla bearing on the dial.
                    Box(Modifier.fillMaxSize().rotate(bearing)) {
                        Column(Modifier.align(Alignment.TopCenter).padding(top = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🕋", fontSize = 34.sp)
                            Box(Modifier.size(width = 4.dp, height = 90.dp).background(ring, RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (!hasSensor) {
                Text(tr("no_sensor"), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            } else {
                Text(
                    if (aligned) "✓ " + tr("facing") else nd("%.0f°".format(java.util.Locale.ENGLISH, abs(diff))),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (aligned) ring else textColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (hasSensor && accuracy <= 1) {
                Spacer(Modifier.height(8.dp))
                Text(tr("calibrate"), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(tr("keep_flat"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (!Prefs.located) {
                Spacer(Modifier.height(8.dp))
                Text(tr("loc_not_set"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}
