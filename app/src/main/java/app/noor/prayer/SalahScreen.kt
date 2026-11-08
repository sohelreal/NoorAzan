package app.noor.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Composable
fun SalahScreen() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Header(tr("salah"))
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("prayers_tab", "how_tab", "duas_tab").forEachIndexed { i, k ->
                FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(tr(k)) })
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (tab) {
                0 -> PrayersTab()
                1 -> HowTab()
                else -> DUAS.forEach { DuaCard(it) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrayersTab() {
    var expanded by remember { mutableStateOf(setOf<Int>()) }
    Text(tr("tap_niyat"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    PRAYER_INFO.forEachIndexed { idx, p ->
        val open = idx in expanded
        NoorCard(onClick = { expanded = if (open) expanded - idx else expanded + idx }) {
            Text(p.name.s(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            p.rows.forEach { r ->
                val label = TYPE_NAMES[r.type].s()
                val w = whenLabel(r.whenIdx)
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text(label + (if (w.isNotEmpty()) " ($w)" else ""), modifier = Modifier.weight(1f), fontWeight = if (r.type == 0) FontWeight.Bold else FontWeight.Normal)
                    Text(nd("${r.rakat}") + " " + tr("rakat"), fontWeight = FontWeight.SemiBold)
                }
                if (open) {
                    Text(
                        niyatFor(p, r),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
    Text(tr("madhab_note"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun HowTab() {
    Text(tr("wudu_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    WUDU_STEPS.forEachIndexed { i, s -> StepCard(i + 1, s) }
    Spacer(Modifier.height(6.dp))
    Text(tr("steps_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    SALAH_STEPS.forEachIndexed { i, s -> StepCard(i + 1, s) }
}

@Composable
private fun StepCard(n: Int, s: Step) {
    NoorCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(nd("$n"), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(s.title.s(), fontWeight = FontWeight.SemiBold)
                Text(s.detail.s(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DuaCard(d: Dua) {
    NoorCard {
        Text(d.title.s(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        ArabicText(d.arabic, Prefs.arabicSize * 0.85f)
        Spacer(Modifier.height(8.dp))
        Text(d.translit.s(), style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        Spacer(Modifier.height(6.dp))
        Text(d.meaning.s(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
