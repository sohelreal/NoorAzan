package com.noorazan.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noorazan.app.util.PrayerCalc
import com.noorazan.app.util.QiblaSensorManager
import kotlin.math.abs

@Composable
fun QiblaScreen(latitude: Double, longitude: Double) {
    val context = LocalContext.current
    var trueAzimuth by remember { mutableFloatStateOf(0f) }
    var needsCalibration by remember { mutableStateOf(false) }

    val qiblaBearing = remember(latitude, longitude) {
        PrayerCalc.calculateTrueQiblaBearing(latitude, longitude)
    }

    val declination = remember(latitude, longitude) {
        PrayerCalc.getMagneticDeclination(latitude, longitude)
    }

    DisposableEffect(context, declination) {
        val manager = QiblaSensorManager(context) { azimuth, lowAcc ->
            trueAzimuth = azimuth
            needsCalibration = lowAcc
        }
        manager.setDeclination(declination)
        manager.start()
        onDispose { manager.stop() }
    }

    val qiblaPointerAngle = (qiblaBearing - trueAzimuth + 360f) % 360f
    val isAligned = abs(((qiblaPointerAngle + 180f) % 360f) - 180f) < 3f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Qibla Finder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Qibla Angle: ${qiblaBearing.toInt()}° True North",
            style = MaterialTheme.typography.bodyLarge
        )

        if (needsCalibration) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Low Compass Accuracy: Move phone in a figure-8 pattern",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                drawCircle(
                    color = if (isAligned) Color(0xFF2E7D32) else Color.LightGray,
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )

                // Draw North indicator rotated by -trueAzimuth
                rotate(degrees = -trueAzimuth) {
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius + 20f),
                        strokeWidth = 6f
                    )
                }

                // Draw Qibla needle
                rotate(degrees = qiblaPointerAngle) {
                    drawLine(
                        color = if (isAligned) Color(0xFF2E7D32) else Color(0xFF1565C0),
                        start = center,
                        end = Offset(center.x, center.y - radius + 16f),
                        strokeWidth = 12f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isAligned) "Facing Qibla" else "Rotate device toward the blue arrow",
            style = MaterialTheme.typography.titleMedium,
            color = if (isAligned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
        )
    }
}
