package app.noor.prayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoorTheme { AppRoot() }
        }
    }
}

private val TABS = listOf("home", "quran", "salah", "qibla", "more")

@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    val rtl = if (Prefs.lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        Loc.refresh(ctx)
        Alarms.scheduleNext(ctx)
    }
    val requestPerms: () -> Unit = {
        val list = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) list.add(Manifest.permission.POST_NOTIFICATIONS)
        permLauncher.launch(list.toTypedArray())
    }

    LaunchedEffect(Unit) {
        if (!Loc.hasPermission(ctx) && !Prefs.located) requestPerms() else Loc.refresh(ctx)
    }
    // Re-schedule the next Azan whenever something that affects prayer times or alerts changes.
    LaunchedEffect(Prefs.lat, Prefs.lon, Prefs.method, Prefs.hanafi, Prefs.adj.toList(), Prefs.notify.toList()) {
        Alarms.scheduleNext(ctx)
    }

    var tab by rememberSaveable { mutableStateOf("home") }
    val stack = remember { mutableStateListOf<String>() }

    val navigate: (String) -> Unit = { route ->
        if (route in TABS) {
            stack.clear()
            tab = route
        } else {
            stack.add(route)
        }
    }
    val pop: () -> Unit = { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }

    BackHandler(enabled = stack.isNotEmpty() || tab != "home") {
        if (stack.isNotEmpty()) pop() else tab = "home"
    }

    CompositionLocalProvider(LocalLayoutDirection provides rtl) {
        Scaffold(
            bottomBar = {
                Column {
                    if (PlayerState.kind.isNotEmpty() || PlayerState.error) MiniPlayer()
                    NavigationBar {
                        TABS.forEach { t ->
                            val icon: ImageVector = when (t) {
                                "home" -> Icons.Filled.Home
                                "quran" -> Icons.AutoMirrored.Filled.List
                                "salah" -> Icons.Filled.Check
                                "qibla" -> Icons.Filled.LocationOn
                                else -> Icons.Filled.Menu
                            }
                            NavigationBarItem(
                                selected = tab == t && stack.isEmpty(),
                                onClick = { navigate(t) },
                                icon = { Icon(icon, contentDescription = null) },
                                label = { Text(tr(t), maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                val top = stack.lastOrNull()
                when {
                    top == "calendar" -> CalendarScreen(onBack = pop)
                    top == "ramadan" -> RamadanScreen(onBack = pop)
                    top == "settings" -> SettingsScreen(onBack = pop, requestPerms = requestPerms)
                    top != null && top.startsWith("surah:") -> SurahScreen(top.substringAfter(":").toInt(), onBack = pop)
                    tab == "home" -> HomeScreen(navigate, requestPerms)
                    tab == "quran" -> QuranListScreen(onOpen = { navigate("surah:$it") })
                    tab == "salah" -> SalahScreen()
                    tab == "qibla" -> QiblaScreen()
                    else -> MoreScreen(navigate)
                }
            }
        }
    }
}

@Composable
fun MoreScreen(navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header(tr("more"))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple("calendar", Icons.Filled.DateRange, "calendar"),
                Triple("ramadan", Icons.Filled.Star, "ramadan"),
                Triple("settings", Icons.Filled.Settings, "settings"),
            ).forEach { (route, icon, key) ->
                NoorCard(onClick = { navigate(route) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text(tr(key), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer() {
    val ctx = LocalContext.current
    Surface(color = HeaderGreen, contentColor = Color.White) {
        Column {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (PlayerState.error) tr("audio_error") else PlayerState.title, maxLines = 1, fontWeight = FontWeight.SemiBold)
                    if (PlayerState.loading) Text(tr("loading"), style = MaterialTheme.typography.bodySmall)
                }
                if (PlayerState.kind == "quran") {
                    IconButton(onClick = { PlayerService.toggle(ctx) }) {
                        if (PlayerState.playing) PauseIcon(Color.White) else Icon(Icons.Filled.PlayArrow, contentDescription = tr("resume"))
                    }
                }
                IconButton(onClick = {
                    if (PlayerState.kind.isEmpty()) PlayerState.error = false else PlayerService.stop(ctx)
                }) { Icon(Icons.Filled.Close, contentDescription = tr("stop")) }
            }
            if (PlayerState.kind == "quran" && PlayerState.dur > 0) {
                LinearProgressIndicator(
                    progress = { (PlayerState.pos.toFloat() / PlayerState.dur).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color(0x33FFFFFF)
                )
            }
        }
    }
}
