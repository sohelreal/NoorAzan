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

    DisposableEffect(Prefs.lat, Prefs.lon) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            hasSensor = false
            onDispose { }
        } else {
            val decl = GeomagneticField(Prefs.lat.toFloat(), Prefs.lon.toFloat(), 0f, System.currentTimeMillis()).declination
            var smooth = 0f
            var first = true
            val rot = FloatArray(9)
            val ori = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rot, e.values)
                    SensorManager.getOrientation(rot, ori)
                    val az = ((Math.toDegrees(ori[0].toDouble()).toFloat() + decl) + 360f) % 360f
                    if (first) {
                        smooth = az
                        first = false
                    } else {
                        var d = az - smooth
                        if (d > 180f) d -= 360f
                        if (d < -180f) d += 360f
                        smooth = (smooth + 0.15f * d + 360f) % 360f
                    }
                    azimuth = smooth
                }

                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
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
            Spacer(Modifier.height(12.dp))
            Text(tr("keep_flat"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (!Prefs.located) {
                Spacer(Modifier.height(8.dp))
                Text(tr("loc_not_set"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}
