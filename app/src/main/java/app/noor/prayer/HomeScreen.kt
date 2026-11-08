package app.noor.prayer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun HomeScreen(navigate: (String) -> Unit, requestPerms: () -> Unit) {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val date = LocalDate.now()
    val day = PrayerCalc.day(date)
    val next = PrayerCalc.next(now)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(tr("app_name"))

        // Hero card
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HeaderGreen, HeaderGreenLight)))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    val placeText = when {
                        Prefs.place.isNotEmpty() -> Prefs.place
                        Prefs.located -> nd("%.3f, %.3f".format(java.util.Locale.ENGLISH, Prefs.lat, Prefs.lon))
                        else -> tr("loc_not_set")
                    }
                    Text(placeText, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                    IconButton(onClick = { if (Loc.hasPermission(ctx)) Loc.refresh(ctx) else requestPerms() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = tr("detect"), tint = Color.White)
                    }
                }
                Text(gregorianLong(date), color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(Hijri.format(date), color = Color(0xFFC8E6C9))
                Spacer(Modifier.height(14.dp))
                if (next != null) {
                    val (p, ms) = next
                    Text(tr("next_prayer"), color = Color(0xFFC8E6C9), style = MaterialTheme.typography.labelLarge)
                    Text(prayerName(p, isFriday(ms)), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(fmtTime(ms), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("${tr("remaining")}: ${fmtCountdown(ms - now)}", color = Color.White)
                } else {
                    Text("—", color = Color.White)
                }
            }
        }

        // Banners
        if (!Prefs.located) {
            Banner(tr("loc_not_set"), tr("detect")) { requestPerms() }
        }
        if (Build.VERSION.SDK_INT >= 33 && ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Banner(tr("notif_perm"), tr("allow")) { requestPerms() }
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val am = ctx.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) {
                Banner(tr("exact_alarm"), tr("allow")) {
                    runCatching {
                        ctx.startActivity(Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                    }
                }
            }
        }

        // Prayer list
        SectionTitle(tr("prayer_times"))
        NoorCard(Modifier.padding(horizontal = 16.dp)) {
            val order = listOf(0, -1, 1, 2, 3, 4)
            order.forEachIndexed { i, p ->
                val isNext = next != null && next.first == p
                val ms = if (day == null) null else if (p == -1) day.sunrise else day.prayer(p)
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (p == -1) tr("sunrise") else prayerName(p, p == 1 && date.dayOfWeek == java.time.DayOfWeek.FRIDAY),
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(if (ms != null) fmtTime(ms) else "--:--", fontWeight = FontWeight.SemiBold)
                    if (p >= 0) {
                        Switch(
                            checked = Prefs.notify[p],
                            onCheckedChange = { v -> Prefs.update { notify[p] = v } },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        Spacer(Modifier.width(60.dp))
                    }
                }
                if (i < order.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            }
        }

        // Continue reading
        SectionTitle(tr("quran"))
        val last = QuranRepo.surahs[(Prefs.lastSurah - 1).coerceIn(0, 113)]
        NoorCard(Modifier.padding(horizontal = 16.dp), onClick = { navigate("surah:${last.n}") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("continue_reading"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(nd("${last.n}. ${last.en}"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Text(last.ar, style = MaterialTheme.typography.titleLarge)
            }
        }

        // Quick access
        SectionTitle(tr("quick_access"))
        val tiles: List<Triple<String, ImageVector, String>> = listOf(
            Triple("quran", Icons.AutoMirrored.Filled.List, "quran"),
            Triple("salah", Icons.Filled.Check, "salah"),
            Triple("qibla", Icons.Filled.LocationOn, "qibla"),
            Triple("calendar", Icons.Filled.DateRange, "calendar"),
            Triple("ramadan", Icons.Filled.Star, "ramadan"),
            Triple("settings", Icons.Filled.Settings, "settings"),
        )
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(3).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { (route, icon, key) ->
                        Card(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable { navigate(route) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text(tr(key), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Banner(text: String, button: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Button(onClick = onClick) { Text(button) }
        }
    }
}
