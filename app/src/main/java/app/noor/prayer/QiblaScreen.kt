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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Qibla compass. The needle always points to the Kaaba relative to the top of the phone:
 * when the needle points straight up, you are facing the Qibla. "N" on the ring marks true north.
 */
@Composable
fun QiblaScreen() {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }
    var gotReading by remember { mutableStateOf(false) }
    var accuracy by remember { mutableIntStateOf(3) }
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 auto (rotation vector), 1 magnetometer + accelerometer
    val bearing = PrayerCalc.qiblaBearing(Prefs.lat, Prefs.lon).toFloat()

    DisposableEffect(Prefs.lat, Prefs.lon, mode) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rvAll = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val useAlt = mode == 1 && acc != null && mag != null
        val rv = if (useAlt) null else rvAll
        if (rv == null && (acc == null || mag == null)) {
            hasSensor = false
            onDispose { }
        } else {
            hasSensor = true
            val decl = GeomagneticField(Prefs.lat.toFloat(), Prefs.lon.toFloat(), 0f, System.currentTimeMillis()).declination
            var sx = 0f
            var sy = 0f
            var first = true
            val rot = FloatArray(9)
            val tmp = FloatArray(9)
            val ori = FloatArray(3)
            val gravity = FloatArray(3)
            val geo = FloatArray(3)
            var haveG = false
            var haveM = false

            fun update() {
                // Flat: standard axes. Held upright: heading of the back of the phone.
                val m = if (abs(rot[8]) > 0.6f) rot else {
                    SensorManager.remapCoordinateSystem(rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, tmp)
                    tmp
                }
                SensorManager.getOrientation(m, ori)
                val az = Math.toDegrees(ori[0].toDouble()).toFloat() + decl + Prefs.compassOffset
                val r = Math.toRadians(az.toDouble())
                val cx = cos(r).toFloat()
                val cy = sin(r).toFloat()
                // Circular low-pass filter: no jump when crossing north.
                if (first) { sx = cx; sy = cy; first = false } else { sx += 0.18f * (cx - sx); sy += 0.18f * (cy - sy) }
                azimuth = ((Math.toDegrees(atan2(sy, sx).toDouble()).toFloat()) + 360f) % 360f
                gotReading = true
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    when (e.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rot, e.values)
                            update()
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            for (i in 0..2) gravity[i] = if (haveG) gravity[i] * 0.85f + e.values[i] * 0.15f else e.values[i]
                            haveG = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            for (i in 0..2) geo[i] = if (haveM) geo[i] * 0.85f + e.values[i] * 0.15f else e.values[i]
                            haveM = true
                            if (rv == null && haveG && SensorManager.getRotationMatrix(rot, null, gravity, geo)) update()
                        }
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

    val needle = bearing - azimuth // degrees the Qibla is clockwise from the top of the phone
    val diff = ((needle + 540f) % 360f + 360f) % 360f - 180f
    val aligned = hasSensor && gotReading && abs(diff) < 4f
    LaunchedEffect(aligned) { if (aligned) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

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
            Spacer(Modifier.height(12.dp))
            Text("▼", color = if (aligned) ring else MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.titleLarge)
            Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxSize()
                        .background(if (aligned) ring.copy(alpha = 0.25f) else ringSoft, CircleShape)
                        .border(if (aligned) 6.dp else 3.dp, ring, CircleShape)
                )
                // True north marker (turns opposite to the phone's heading).
                Box(Modifier.fillMaxSize().rotate(-azimuth)) {
                    Text("N", Modifier.align(Alignment.TopCenter).padding(top = 6.dp), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    Text("S", Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), fontWeight = FontWeight.Bold, color = textColor)
                    Text("E", Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), fontWeight = FontWeight.Bold, color = textColor)
                    Text("W", Modifier.align(Alignment.CenterStart).padding(start = 10.dp), fontWeight = FontWeight.Bold, color = textColor)
                }
                // Qibla needle.
                Box(Modifier.fillMaxSize().rotate(needle)) {
                    Column(Modifier.align(Alignment.TopCenter).padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🕋", fontSize = 36.sp)
                        Box(Modifier.size(width = 6.dp, height = 100.dp).background(ring, RoundedCornerShape(3.dp)))
                    }
                }
                Box(Modifier.size(14.dp).background(ring, CircleShape))
            }
            Spacer(Modifier.height(16.dp))
            if (!hasSensor) {
                Text(tr("no_sensor"), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            } else {
                Text(
                    if (aligned) "✓ " + tr("facing") else nd("%.0f°".format(java.util.Locale.ENGLISH, abs(diff))),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (aligned) ring else textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    tr("heading") + " " + nd("%.0f°".format(java.util.Locale.ENGLISH, azimuth)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hasSensor && accuracy <= 1) {
                Spacer(Modifier.height(8.dp))
                Text(tr("calibrate"), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text(tr("compass_auto")) })
                FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text(tr("compass_alt")) })
            }
            Spacer(Modifier.height(8.dp))
            Text(tr("compass_fix") + ": " + nd((if (Prefs.compassOffset > 0) "+" else "") + "${Prefs.compassOffset}°"), style = MaterialTheme.typography.labelLarge)
            Slider(
                value = Prefs.compassOffset.toFloat(),
                onValueChange = { v -> Prefs.update { compassOffset = v.toInt() } },
                valueRange = -180f..180f
            )
            Text(tr("compass_fix_hint"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(tr("keep_flat"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (!Prefs.located) {
                Spacer(Modifier.height(8.dp))
                Text(tr("loc_not_set"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}
