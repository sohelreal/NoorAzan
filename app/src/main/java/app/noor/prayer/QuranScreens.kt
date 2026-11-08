package app.noor.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch

@Composable
fun QuranListScreen(onOpen: (Int) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var progress by remember { mutableIntStateOf(-1) }
    var failed by remember { mutableStateOf(false) }
    var cached by remember { mutableIntStateOf(QuranRepo.cachedCount(ctx)) }

    val q = query.trim().lowercase()
    val list = if (q.isEmpty()) QuranRepo.surahs else QuranRepo.surahs.filter {
        it.n.toString() == q || it.en.lowercase().contains(q) || it.bn.contains(q) || it.ar.contains(q)
    }

    Column(Modifier.fillMaxSize()) {
        Header(tr("quran"))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(tr("search_surah")) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (progress >= 0) {
                Column(Modifier.weight(1f)) {
                    Text("${tr("downloading")} ${nd("$progress/114")}", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { progress / 114f }, modifier = Modifier.fillMaxWidth())
                }
            } else {
                OutlinedButton(onClick = {
                    failed = false
                    scope.launch {
                        for (n in 1..114) {
                            progress = n
                            if (!QuranRepo.isCached(ctx, n) && QuranRepo.load(ctx, n) == null) {
                                failed = true
                                break
                            }
                            cached = QuranRepo.cachedCount(ctx)
                        }
                        cached = QuranRepo.cachedCount(ctx)
                        progress = -1
                    }
                }, enabled = cached < 114) {
                    Text(tr("download_all"))
                }
                Spacer(Modifier.weight(1f))
                Text("${tr("saved_offline")}: ${nd("$cached/114")}", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (failed) {
            Text(tr("no_internet"), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(list, key = { it.n }) { s -> SurahRow(s) { onOpen(s.n) } }
        }
    }
}

@Composable
private fun SurahRow(s: Surah, onClick: () -> Unit) {
    val primary = when (Prefs.lang) { "bn" -> s.bn; "ar" -> s.ar; else -> s.en }
    val sub = (if (s.meccan) tr("meccan") else tr("medinan")) + " • " + nd("${s.ayahs}") + " " + tr("ayahs")
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(nd("${s.n}"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(if (Prefs.lang == "bn") "${s.en} • $sub" else sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (Prefs.lang != "ar") Text(s.ar, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}

private fun mmss(ms: Int): String {
    val t = ms / 1000
    return nd("%d:%02d".format(java.util.Locale.ENGLISH, t / 60, t % 60))
}

@Composable
fun SurahScreen(n: Int, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val s = QuranRepo.surahs[(n - 1).coerceIn(0, 113)]
    var ayahs by remember(n) { mutableStateOf<List<Ayah>?>(null) }
    var failed by remember(n) { mutableStateOf(false) }
    var reload by remember(n) { mutableIntStateOf(0) }

    LaunchedEffect(n, reload) {
        Prefs.update { lastSurah = n }
        failed = false
        ayahs = QuranRepo.load(ctx, n)
        failed = ayahs == null
    }

    val current = PlayerState.kind == "quran" && PlayerState.surah == n
    val scope = rememberCoroutineScope()
    var saved by remember(n, Prefs.reciter) { mutableStateOf(QuranRepo.audioSaved(ctx, Prefs.reciter, n)) }
    var savingPct by remember(n, Prefs.reciter) { mutableFloatStateOf(-1f) }
    var saveFailed by remember(n, Prefs.reciter) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Header(
            title = when (Prefs.lang) { "bn" -> "${nd("$n")}. ${s.bn}"; "ar" -> s.ar; else -> "${n}. ${s.en}" },
            onBack = onBack
        )
        // Audio controls
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    if (current) PlayerService.toggle(ctx) else PlayerService.playSurah(ctx, n)
                }) {
                    if (current && PlayerState.playing) {
                        PauseIcon(MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(tr("pause"))
                    } else {
                        Text(if (current) tr("resume") else "▶  ${tr("play_surah")}")
                    }
                }
                Spacer(Modifier.width(12.dp))
                if (current && PlayerState.loading) Text(tr("loading"), style = MaterialTheme.typography.bodySmall)
            }
            if (current && PlayerState.dur > 0) {
                var seeking by remember { mutableStateOf(false) }
                var seekVal by remember { mutableFloatStateOf(0f) }
                Slider(
                    value = if (seeking) seekVal else PlayerState.pos.toFloat().coerceAtMost(PlayerState.dur.toFloat()),
                    onValueChange = { seeking = true; seekVal = it },
                    onValueChangeFinished = { PlayerService.seek(ctx, seekVal.toInt()); seeking = false },
                    valueRange = 0f..PlayerState.dur.toFloat()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(mmss(if (seeking) seekVal.toInt() else PlayerState.pos), style = MaterialTheme.typography.bodySmall)
                    Text(mmss(PlayerState.dur), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                if (saved) {
                    Text("✓ " + tr("audio_saved"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                } else if (savingPct >= 0f) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("saving") + " " + nd("${(savingPct * 100).toInt()}%"), style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { savingPct }, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    OutlinedButton(onClick = {
                        saveFailed = false
                        savingPct = 0f
                        val rec = Prefs.reciter
                        scope.launch {
                            val ok = QuranRepo.saveAudio(ctx, rec, n) { savingPct = it }
                            saved = ok && QuranRepo.audioSaved(ctx, rec, n)
                            saveFailed = !ok
                            savingPct = -1f
                        }
                    }) { Text(tr("save_audio")) }
                    if (saveFailed) Text("  " + tr("no_internet"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Text(tr("reciter"), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuranRepo.reciters.forEach { (id, key) ->
                    FilterChip(
                        selected = Prefs.reciter == id,
                        onClick = { Prefs.update { reciter = id } },
                        label = { Text(tr(key)) }
                    )
                }
            }
        }

        val list = ayahs
        when {
            list != null -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (n != 1 && n != 9) {
                    item {
                        Text(
                            "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = (Prefs.arabicSize + 2).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                items(list.size) { i ->
                    val a = list[i]
                    NoorCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(30.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text(nd("${i + 1}"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                        }
                        Spacer(Modifier.height(8.dp))
                        ArabicText(a.ar, Prefs.arabicSize)
                        if (Prefs.showTrans && Prefs.lang != "ar") {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (Prefs.lang == "bn") a.bn else a.en,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            failed -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(tr("no_internet"), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { reload++ }) { Text(tr("retry")) }
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(tr("loading"))
                }
            }
        }
    }
}
