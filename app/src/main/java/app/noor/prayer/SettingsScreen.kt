package app.noor.prayer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SettingsScreen(onBack: () -> Unit, requestPerms: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasAzanFile by remember { mutableStateOf(PlayerService.azanFile(ctx).exists()) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        ctx.contentResolver.openInputStream(uri)!!.use { input ->
                            PlayerService.azanFile(ctx).outputStream().use { input.copyTo(it) }
                        }
                    }.isSuccess
                }
                hasAzanFile = PlayerService.azanFile(ctx).exists()
                message = if (ok) tr("azan_saved") else tr("no_internet")
            }
        }
    }

    var pickIdx by remember { mutableIntStateOf(-1) }
    val custom = remember { mutableStateListOf(*Array(5) { i -> PlayerService.azanFileFor(ctx, i).let { f -> f.exists() && f.length() > 0 } }) }
    val pickerFor = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val idx = pickIdx
        if (uri != null && idx in 0..4) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        ctx.contentResolver.openInputStream(uri)!!.use { input ->
                            PlayerService.azanFileFor(ctx, idx).outputStream().use { input.copyTo(it) }
                        }
                    }.isSuccess
                }
                custom[idx] = ok && PlayerService.azanFileFor(ctx, idx).length() > 0
                message = if (ok) tr("azan_saved") else tr("no_internet")
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Header(tr("settings"), onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {

            SectionTitle(tr("language"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                listOf("en" to "English", "bn" to "বাংলা", "ar" to "العربية").forEach { (code, name) ->
                    RadioRow(name, Prefs.lang == code) { Prefs.update { lang = code } }
                }
            }

            SectionTitle(tr("theme"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                listOf("system", "light", "dark").forEachIndexed { i, k ->
                    RadioRow(tr(k), Prefs.theme == i) { Prefs.update { theme = i } }
                }
            }

            SectionTitle(tr("location"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (Prefs.place.isNotEmpty()) Prefs.place else tr("loc_not_set"),
                    fontWeight = FontWeight.SemiBold
                )
                Text(nd("%.4f, %.4f".format(java.util.Locale.ENGLISH, Prefs.lat, Prefs.lon)), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { if (Loc.hasPermission(ctx)) Loc.refresh(ctx) else requestPerms() }) { Text(tr("detect")) }
                Spacer(Modifier.height(12.dp))
                Text(tr("manual_loc"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                var latText by remember { mutableStateOf("%.4f".format(java.util.Locale.ENGLISH, Prefs.lat)) }
                var lonText by remember { mutableStateOf("%.4f".format(java.util.Locale.ENGLISH, Prefs.lon)) }
                var err by remember { mutableStateOf(false) }
                OutlinedTextField(
                    latText, { latText = it }, label = { Text(tr("latitude")) }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    lonText, { lonText = it }, label = { Text(tr("longitude")) }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (err) Text(tr("invalid"), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val la = latText.replace(',', '.').toDoubleOrNull()
                    val lo = lonText.replace(',', '.').toDoubleOrNull()
                    if (la == null || lo == null || la !in -90.0..90.0 || lo !in -180.0..180.0) {
                        err = true
                    } else {
                        err = false
                        Prefs.update { lat = la; lon = lo; located = true; place = "" }
                    }
                }) { Text(tr("save")) }
            }

            SectionTitle(tr("calc_method"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                PrayerCalc.methods.keys.forEach { k ->
                    RadioRow(tr("method_$k"), Prefs.method == k) { Prefs.update { method = k } }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text(tr("asr_method"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                RadioRow(tr("standard"), !Prefs.hanafi) { Prefs.update { hanafi = false } }
                RadioRow(tr("hanafi"), Prefs.hanafi) { Prefs.update { hanafi = true } }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text(tr("adjust_min"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                PRAYER_KEYS.forEachIndexed { i, k ->
                    Stepper(tr(k), Prefs.adj[i], { v -> Prefs.update { adj[i] = v } }, -30, 30)
                }
            }

            SectionTitle(tr("notifications"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                RadioRow(tr("mode_azan"), Prefs.azanMode == 0) { Prefs.update { azanMode = 0 } }
                RadioRow(tr("mode_notif"), Prefs.azanMode == 1) { Prefs.update { azanMode = 1 } }
                RadioRow(tr("mode_silent"), Prefs.azanMode == 2) { Prefs.update { azanMode = 2 } }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                PRAYER_KEYS.forEachIndexed { i, k ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(tr(k), Modifier.weight(1f))
                        Switch(Prefs.notify[i], { v -> Prefs.update { notify[i] = v } })
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text(if (hasAzanFile) tr("azan_saved") else tr("azan_none"), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                var url by remember { mutableStateOf(Prefs.azanUrl) }
                OutlinedTextField(url, { url = it }, label = { Text(tr("azan_url")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = !busy, onClick = {
                        val u = url.trim()
                        if (!u.startsWith("https://")) {
                            message = tr("invalid")
                        } else {
                            busy = true
                            message = tr("downloading")
                            scope.launch {
                                val ok = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val c = URL(u).openConnection() as HttpURLConnection
                                        c.connectTimeout = 15000
                                        c.readTimeout = 60000
                                        require(c.responseCode == 200)
                                        val tmp = java.io.File(ctx.filesDir, "azan.tmp")
                                        c.inputStream.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
                                        require(tmp.length() > 1000)
                                        val dest = PlayerService.azanFile(ctx)
                                        dest.delete()
                                        tmp.renameTo(dest)
                                    }.getOrDefault(false)
                                }
                                if (ok) Prefs.update { azanUrl = u }
                                hasAzanFile = PlayerService.azanFile(ctx).exists()
                                message = if (ok) tr("azan_saved") else tr("no_internet")
                                busy = false
                            }
                        }
                    }) { Text(tr("download_azan")) }
                    OutlinedButton(onClick = { picker.launch(arrayOf("audio/*")) }) { Text(tr("choose_file")) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { PlayerService.testAzan(ctx) }) { Text(tr("test_azan")) }
                    if (hasAzanFile) {
                        TextButton(onClick = {
                            PlayerService.azanFile(ctx).delete()
                            hasAzanFile = false
                            message = ""
                        }) { Text(tr("remove_azan")) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text(tr("per_prayer_azan"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(tr("per_prayer_hint"), style = MaterialTheme.typography.bodySmall)
                PRAYER_KEYS.forEachIndexed { i, k ->
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tr(k), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(if (custom[i]) tr("custom_azan") else tr("default_azan"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(onClick = { pickIdx = i; pickerFor.launch(arrayOf("audio/*")) }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text(tr("choose_file")) }
                            OutlinedButton(onClick = { PlayerService.testAzan(ctx, i) }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text(tr("test_azan")) }
                            if (custom[i]) {
                                TextButton(onClick = { PlayerService.azanFileFor(ctx, i).delete(); custom[i] = false }) { Text(tr("remove_azan")) }
                            }
                        }
                    }
                }
                if (message.isNotEmpty()) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                val pm = ctx.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(ctx.packageName)) {
                    Text(tr("battery_tip"), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = {
                        runCatching { ctx.startActivity(Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                    }) { Text(tr("allow")) }
                }
            }

            SectionTitle(tr("time_format"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("time_format"), Modifier.weight(1f))
                    Switch(Prefs.use24, { v -> Prefs.update { use24 = v } })
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Stepper(tr("hijri_adj"), Prefs.hijriOffset, { v -> Prefs.update { hijriOffset = v } }, -2, 2)
            }

            SectionTitle(tr("quran_settings"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("show_translation"), Modifier.weight(1f))
                    Switch(Prefs.showTrans, { v -> Prefs.update { showTrans = v } })
                }
                Text(tr("arabic_size"), Modifier.padding(top = 8.dp))
                Slider(
                    value = Prefs.arabicSize,
                    onValueChange = { v -> Prefs.update { arabicSize = v } },
                    valueRange = 20f..44f
                )
            }

            SectionTitle(tr("about"))
            NoorCard(Modifier.padding(horizontal = 16.dp)) {
                Text(tr("about_text"), style = MaterialTheme.typography.bodyMedium)
                Text("v1.0.0", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
